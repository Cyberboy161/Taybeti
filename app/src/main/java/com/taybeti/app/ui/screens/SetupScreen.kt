package com.taybeti.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taybeti.app.data.repository.NoteRepository
import com.taybeti.app.ui.components.KeyboardHost
import com.taybeti.app.ui.components.NoteEncryptionTutorialDialog
import com.taybeti.app.ui.components.PasswordField
import com.taybeti.app.util.Constants
import com.taybeti.app.util.InlineTranslations
import com.taybeti.app.util.LocalLanguageCode
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.LocaleManager
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    repository: NoteRepository,
    onSetupComplete: () -> Unit,
    onChangeLanguage: (String) -> Unit = {}
) {
    val strings = LocalStrings.current
    val lang = LocalLanguageCode.current
    val context = LocalContext.current
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var decoyPassword by remember { mutableStateOf("") }
    var confirmDecoy by remember { mutableStateOf("") }
    var enableDecoy by remember { mutableStateOf(false) }
    var showDecoyInfo by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    KeyboardHost {
        Column(modifier = Modifier.fillMaxSize()) {
        // Language globe button
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = { showLanguageMenu = true }) {
                    Icon(Icons.Default.Language, InlineTranslations.t("change_lang", lang), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                    LocaleManager.languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.label) },
                            onClick = {
                                showLanguageMenu = false
                                LocaleManager.setLanguage(context, language.code)
                                onChangeLanguage(language.code)
                            }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                strings.setupCreatePassword,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This password protects all your notes. It cannot be recovered if lost.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            PasswordField(
                value = masterPassword,
                onValueChange = { masterPassword = it; error = null },
                label = "${strings.setupCreatePassword} (min ${Constants.MIN_MASTER_PASSWORD_LENGTH} chars)",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
                label = strings.setupConfirmPassword,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Decoy option
            androidx.compose.material3.HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.setupEnableDecoy,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enableDecoy) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
                IconButton(onClick = { showDecoyInfo = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Why enable decoy?",
                        tint = if (enableDecoy) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
                Switch(
                    checked = enableDecoy,
                    onCheckedChange = {
                        enableDecoy = it
                        if (!it) { decoyPassword = ""; confirmDecoy = "" }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.error,
                        uncheckedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                )
            }
            Text(
                strings.setupDecoyHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Decoy info dialog
            if (showDecoyInfo) {
                AlertDialog(
                    onDismissRequest = { showDecoyInfo = false },
                    title = { Text(strings.setupEnableDecoy) },
                    text = {
                        Text(
                            "A decoy vault protects you in situations where you are forced to " +
                            "unlock your app. If you enter the decoy password, the app opens a " +
                            "separate vault with harmless notes — your real notes stay hidden.\n\n" +
                            "Without a decoy vault, an attacker who forces you to unlock the app " +
                            "will see all your real notes. The decoy vault is your last line of " +
                            "defense against physical coercion."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                enableDecoy = true
                                showDecoyInfo = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(InlineTranslations.t("enable_decoy", lang))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDecoyInfo = false }) {
                            Text(InlineTranslations.t("not_now", lang))
                        }
                    }
                )
            }

            if (enableDecoy) {
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField(
                    value = decoyPassword,
                    onValueChange = { decoyPassword = it; error = null },
                    label = "${strings.setupDecoyPassword} (min ${Constants.MIN_MASTER_PASSWORD_LENGTH} chars)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField(
                    value = confirmDecoy,
                    onValueChange = { confirmDecoy = it; error = null },
                    label = InlineTranslations.t("confirm_decoy", lang),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        } // end scrollable inner Column

        // Button always visible above keyboard
        Button(
            onClick = {
                when {
                    masterPassword.length < Constants.MIN_MASTER_PASSWORD_LENGTH ->
                        error = InlineTranslations.t("master_pass_min", lang).replace("{n}", "${Constants.MIN_MASTER_PASSWORD_LENGTH}")
                    masterPassword != confirmPassword ->
                        error = strings.setupPasswordMismatch
                    enableDecoy && decoyPassword.length < Constants.MIN_MASTER_PASSWORD_LENGTH ->
                        error = InlineTranslations.t("decoy_pass_min", lang).replace("{n}", "${Constants.MIN_MASTER_PASSWORD_LENGTH}")
                    enableDecoy && decoyPassword != confirmDecoy ->
                        error = InlineTranslations.t("decoy_no_match", lang)
                    enableDecoy && decoyPassword == masterPassword ->
                        error = InlineTranslations.t("decoy_differ", lang)
                    else -> {
                        isLoading = true
                        scope.launch {
                            val decoy = if (enableDecoy) decoyPassword.toCharArray() else null
                            val result = repository.setupMasterPassword(
                                masterPassword.toCharArray(),
                                decoy
                            )
                            isLoading = false
                            masterPassword = ""
                            confirmPassword = ""
                            decoyPassword = ""
                            confirmDecoy = ""
                            if (result.isSuccess) {
                                showTutorial = true
                            } else error = InlineTranslations.t("setup_failed", lang).replace("{msg}", "${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 12.dp)
        ) {
            Text(if (isLoading) "Setting up..." else strings.setupCreateBtn)
        }
    } // end outer Column
    }

    if (showTutorial) {
        NoteEncryptionTutorialDialog(
            onDismiss = {
                showTutorial = false
                onSetupComplete()
            }
        )
    }
}
