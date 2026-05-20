package com.taybeti.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taybeti.app.util.LocaleManager
import com.taybeti.app.util.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onChangePassword: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    isDecoyEnabled: Boolean = false,
    onSetupDecoy: (() -> Unit)? = null,
    autoLockTimeoutMinutes: Int = 5,
    onAutoLockTimeoutChange: (Int) -> Unit = {},
    showNoteTitle: Boolean = true,
    onShowNoteTitleChange: (Boolean) -> Unit = {},
    showNoteDate: Boolean = true,
    onShowNoteDateChange: (Boolean) -> Unit = {}
) {
    val strings = LocalStrings.current
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showLockTimerDropdown by remember { mutableStateOf(false) }
    var rootDetectionEnabled by remember { mutableStateOf(true) }

    val lockOptions = remember {
        listOf(
            0 to strings.autoLockOff,
            1 to strings.autoLock1min,
            5 to strings.autoLock5min,
            15 to strings.autoLock15min,
            30 to strings.autoLock30min,
            60 to strings.autoLock1hr
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = strings.cancel)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsToggle(
                title = strings.darkTheme,
                subtitle = strings.darkThemeSubtitle,
                checked = isDarkTheme,
                onCheckedChange = onToggleTheme
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsToggle(
                title = strings.rootDetectionTitle,
                subtitle = strings.rootDetectionSubtitle,
                checked = rootDetectionEnabled,
                onCheckedChange = { rootDetectionEnabled = it }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsRow(
                title = strings.changePassword,
                subtitle = strings.changePassword
            ) {
                onChangePassword()
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            if (!isDecoyEnabled && onSetupDecoy != null) {
                SettingsRow(
                    title = strings.settingsDecoySetup,
                    subtitle = strings.settingsDecoySubtitle
                ) {
                    onSetupDecoy()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            SettingsRow(
                title = strings.language,
                subtitle = strings.selectLanguage
            ) {
                showLanguagePicker = true
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    title = strings.securityLevel,
                    subtitle = lockOptions.firstOrNull { it.first == autoLockTimeoutMinutes }?.second ?: strings.autoLock5min
                ) {
                    showLockTimerDropdown = true
                }

                DropdownMenu(
                    expanded = showLockTimerDropdown,
                    onDismissRequest = { showLockTimerDropdown = false }
                ) {
                    lockOptions.forEach { (mins, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    label,
                                    fontWeight = if (mins == autoLockTimeoutMinutes) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onAutoLockTimeoutChange(mins)
                                showLockTimerDropdown = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsToggle(
                title = strings.showNoteTitle,
                subtitle = strings.showNoteTitle,
                checked = showNoteTitle,
                onCheckedChange = onShowNoteTitleChange
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsToggle(
                title = strings.showNoteDate,
                subtitle = strings.showNoteDate,
                checked = showNoteDate,
                onCheckedChange = onShowNoteDateChange
            )
        }
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = LocaleManager.getCurrentLanguage(LocalContext.current),
            onLanguageSelected = { lang ->
                onChangeLanguage(lang)
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false }
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun LanguagePickerDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalStrings.current.selectLanguage) },
        text = {
            Column {
                LocaleManager.languages.forEach { language ->
                    val isSelected = language.code == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                LocaleManager.setLanguage(context, language.code)
                                onLanguageSelected(language.code)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (language.flagEmoji != null) {
                            Text(language.flagEmoji, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                        } else {
                            KurdistanFlag(modifier = Modifier.size(36.dp, 26.dp))
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(
                            language.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(LocalStrings.current.confirm)
            }
        }
    )
}
