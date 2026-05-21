package com.taybeti.app.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taybeti.app.data.repository.NoteRepository
import com.taybeti.app.ui.screens.AboutScreen
import com.taybeti.app.ui.screens.EncryptDecryptScreen
import com.taybeti.app.ui.screens.FileEncryptDecryptScreen
import com.taybeti.app.ui.screens.ExportImportScreen
import com.taybeti.app.ui.screens.EncryptionGuideScreen
import com.taybeti.app.ui.screens.InstructionsScreen
import com.taybeti.app.ui.screens.LanguagePickerDialog
import com.taybeti.app.ui.screens.LanguageScreen
import com.taybeti.app.ui.screens.LoginScreen
import com.taybeti.app.ui.screens.NoteEditorScreen
import com.taybeti.app.ui.screens.NoteListScreen
import com.taybeti.app.ui.screens.SettingsScreen
import com.taybeti.app.ui.screens.SetupScreen
import com.taybeti.app.ui.screens.TrashScreen
import com.taybeti.app.ui.components.PasswordField
import com.taybeti.app.ui.components.KeyboardState
import com.taybeti.app.ui.components.LocalKeyboardState
import com.taybeti.app.ui.components.CustomKeyboard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.taybeti.app.util.LocaleManager
import com.taybeti.app.util.LocalLanguageCode
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.stringsFor
import kotlinx.coroutines.launch

object NavRoutes {
    const val SETUP = "setup"
    const val LOGIN = "login"
    const val LANGUAGE = "language"
    const val NOTES = "notes"
    const val FAVORITES = "favorites"
    const val EDITOR = "editor/{noteId}"
    const val ENCRYPT_DECRYPT = "encrypt_decrypt"
    const val TRASH = "trash"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val EXPORT_IMPORT = "export_import"
    const val INSTRUCTIONS = "instructions"
    const val ENCRYPTION_GUIDE = "encryption_guide"
    const val FILE_ENCRYPT_DECRYPT = "file_encrypt_decrypt"

    fun editorRoute(noteId: String) = "editor/$noteId"
}

@Composable
fun AppNavGraph(
    repository: NoteRepository,
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var isDecoy by remember { mutableStateOf(false) }
    var needsSetup by remember { mutableStateOf<Boolean?>(null) }
    var isDecoyEnabled by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Language state
    var currentLanguage by remember {
        mutableStateOf(LocaleManager.getCurrentLanguage(navController.context))
    }

    // First launch detection
    var isFirstLaunch by remember { mutableStateOf<Boolean?>(null) }

    // Initial setup detection
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isFirstLaunch = LocaleManager.isFirstLaunch(navController.context)
        needsSetup = !repository.hasLoginInfo()
        isDecoyEnabled = repository.isDecoyEnabled()
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Taybeti?") },
            text = { Text("Are you sure you want to leave the app?") },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    (navController.context as? android.app.Activity)?.finish()
                }) {
                    Text("Exit")
                }
            }
        )
    }

    val strings = remember(currentLanguage) { stringsFor(currentLanguage) }
    val layoutDir = if (currentLanguage == "ckb") LayoutDirection.Rtl else LayoutDirection.Ltr

    androidx.compose.runtime.CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLanguageCode provides currentLanguage,
        LocalLayoutDirection provides layoutDir
    ) {
    NavHost(
        navController = navController,
        startDestination = when {
            isFirstLaunch == null || needsSetup == null -> NavRoutes.SETUP // loading
            isFirstLaunch == true -> NavRoutes.LANGUAGE
            needsSetup == true -> NavRoutes.SETUP
            else -> NavRoutes.LOGIN
        }
    ) {
        composable(NavRoutes.LANGUAGE) {
            val ctx = LocalContext.current
            LanguageScreen(
                onLanguageSelected = { lang ->
                    currentLanguage = lang
                    LocaleManager.setLanguage(ctx, lang)
                    LocaleManager.markLaunched(ctx)
                    navController.navigate(
                        if (needsSetup == true) NavRoutes.SETUP else NavRoutes.LOGIN
                    ) {
                        popUpTo(NavRoutes.LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SETUP) {
            SetupScreen(
                repository = repository,
                onSetupComplete = {
                    isLoggedIn = true
                    navController.navigate(NavRoutes.NOTES) {
                        popUpTo(NavRoutes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                repository = repository,
                onLoginSuccess = { decoy ->
                    isLoggedIn = true
                    isDecoy = decoy
                    navController.navigate(NavRoutes.NOTES) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.NOTES) {
            MainDrawerScreen(
                repository = repository,
                isDecoy = isDecoy,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onLogout = {
                    isLoggedIn = false
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChangeLanguage = { lang ->
                    currentLanguage = lang
                },
                isDecoyEnabled = isDecoyEnabled,
                onDecoySetupComplete = { isDecoyEnabled = true }
            )
        }
    }
    } // end CompositionLocalProvider
}

@Composable
fun MainDrawerScreen(
    repository: NoteRepository,
    isDecoy: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    isDecoyEnabled: Boolean,
    onDecoySetupComplete: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var showExitDialog by remember { mutableStateOf(false) }

    var showDecoySetupDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val strings = LocalStrings.current
    val ctx = LocalContext.current
    var autoLockMinutes by remember {
        mutableStateOf(ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getInt("auto_lock_minutes", 5))
    }
    var showNoteTitle by remember {
        mutableStateOf(ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("show_note_title", true))
    }
    var showNoteDate by remember {
        mutableStateOf(ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("show_note_date", true))
    }

    // Auto-lock timer: when app goes to background, record timestamp.
    // On resume, if timeout exceeded, log out.
    val lifecycleOwner = ctx as? androidx.lifecycle.LifecycleOwner
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val prefs = ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val observer = object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
                prefs.edit().putLong("last_pause_time", System.currentTimeMillis()).commit()
            }
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                val timeout = prefs.getInt("auto_lock_minutes", 5)
                if (timeout > 0) {
                    val lastPause = prefs.getLong("last_pause_time", 0)
                    val elapsed = System.currentTimeMillis() - lastPause
                    if (lastPause > 0 && elapsed > timeout * 60_000L) {
                        onLogout()
                    }
                }
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    var showLangDialog by remember { mutableStateOf(false) }
    var showInstructionsPrompt by remember {
        mutableStateOf(
            ctx.getSharedPreferences("locale_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("instructions_prompt_shown", false) == false
        )
    }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(strings.exitTitle) },
            text = { Text(strings.exitMessage) },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(strings.stay)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    (navController.context as? android.app.Activity)?.finish()
                }) {
                    Text(strings.exitConfirm)
                }
            }
        )
    }

    if (showDecoySetupDialog) {
        DecoySetupDialog(
            repository = repository,
            onDismiss = { showDecoySetupDialog = false },
            onComplete = {
                showDecoySetupDialog = false
                onDecoySetupComplete()
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            repository = repository,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isInEditor = navBackStackEntry?.destination?.route?.startsWith("editor/") == true

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isInEditor,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Taybeti",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showLangDialog = true }) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = strings.language,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        if (isDecoy) strings.sidebarDecoyVault else strings.sidebarSecureVault,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerItem(Icons.Default.Note, strings.sidebarAllNotes) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.NOTES) {
                            popUpTo(NavRoutes.NOTES) { inclusive = true }
                        }
                    }
                    DrawerItem(Icons.Default.Star, strings.sidebarFavorites) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.FAVORITES) {
                            popUpTo(NavRoutes.NOTES) { inclusive = false }
                        }
                    }
                    DrawerItem(Icons.Default.SwapHoriz, strings.sidebarEncryptDecrypt) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.ENCRYPT_DECRYPT)
                    }
                    DrawerItem(Icons.Default.Lock, strings.sidebarFileEncrypt, iconSize = 32.dp) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.FILE_ENCRYPT_DECRYPT)
                    }
                    DrawerItem(Icons.Default.Delete, strings.sidebarTrash) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.TRASH)
                    }
                    DrawerItem(Icons.Default.Settings, strings.sidebarSettings) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.SETTINGS)
                    }
                    DrawerItem(Icons.Default.Upload, strings.sidebarExportImport) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.EXPORT_IMPORT)
                    }
                    DrawerItem(Icons.Default.Warning, strings.sidebarInstructions) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.INSTRUCTIONS)
                    }
                    DrawerItem(Icons.Default.Info, strings.sidebarAbout) {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.ABOUT)
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    androidx.compose.material3.HorizontalDivider()
                    DrawerItem(Icons.Default.Logout, strings.sidebarLockVault) {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.NOTES
        ) {
            composable(NavRoutes.NOTES) {
                NoteListScreen(
                    isDecoy = isDecoy,
                    showFavorites = false,
                    onNoteClick = { noteId ->
                        navController.navigate(NavRoutes.editorRoute(noteId))
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    showNoteTitle = showNoteTitle,
                    showNoteDate = showNoteDate
                )
            }
            composable(NavRoutes.FAVORITES) {
                NoteListScreen(
                    isDecoy = isDecoy,
                    showFavorites = true,
                    onNoteClick = { noteId ->
                        navController.navigate(NavRoutes.editorRoute(noteId))
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    showNoteTitle = showNoteTitle,
                    showNoteDate = showNoteDate
                )
            }
            composable(NavRoutes.EDITOR) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                NoteEditorScreen(
                    noteId = noteId,
                    repository = repository,
                    isDecoy = isDecoy,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.ENCRYPT_DECRYPT) {
                EncryptDecryptScreen(onOpenDrawer = {
                    scope.launch { drawerState.open() }
                })
            }
            composable(NavRoutes.FILE_ENCRYPT_DECRYPT) {
                FileEncryptDecryptScreen(onOpenDrawer = {
                    scope.launch { drawerState.open() }
                })
            }
            composable(NavRoutes.TRASH) {
                TrashScreen(isDecoy = isDecoy, onOpenDrawer = {
                    scope.launch { drawerState.open() }
                })
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onChangePassword = { showChangePasswordDialog = true },
                    onChangeLanguage = onChangeLanguage,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    isDecoyEnabled = isDecoyEnabled,
                    onSetupDecoy = { showDecoySetupDialog = true },
                    autoLockTimeoutMinutes = autoLockMinutes,
                    onAutoLockTimeoutChange = { mins ->
                        autoLockMinutes = mins
                        ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().putInt("auto_lock_minutes", mins).commit()
                    },
                    showNoteTitle = showNoteTitle,
                    onShowNoteTitleChange = { show ->
                        showNoteTitle = show
                        ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean("show_note_title", show).commit()
                    },
                    showNoteDate = showNoteDate,
                    onShowNoteDateChange = { show ->
                        showNoteDate = show
                        ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean("show_note_date", show).commit()
                    }
                )
            }
            composable(NavRoutes.ABOUT) {
                AboutScreen(onOpenDrawer = {
                    scope.launch { drawerState.open() }
                })
            }
            composable(NavRoutes.INSTRUCTIONS) {
                InstructionsScreen(
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    },
                    onOpenGuide = {
                        navController.navigate(NavRoutes.ENCRYPTION_GUIDE)
                    }
                )
            }
            composable(NavRoutes.ENCRYPTION_GUIDE) {
                EncryptionGuideScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.EXPORT_IMPORT) {
                ExportImportScreen(
                    repository = repository,
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
        }
    }

    // Language picker dialog
    if (showLangDialog) {
        LanguagePickerDialog(
            currentLanguage = LocaleManager.getCurrentLanguage(
                androidx.compose.ui.platform.LocalContext.current
            ),
            onLanguageSelected = { lang ->
                onChangeLanguage(lang)
                showLangDialog = false
            },
            onDismiss = { showLangDialog = false }
        )
    }

    // First-time instructions prompt
    if (showInstructionsPrompt) {
        AlertDialog(
            onDismissRequest = {
                showInstructionsPrompt = false
                markInstructionsPromptShown(ctx)
            },
            title = { Text(strings.instructionsPromptTitle) },
            text = { Text(strings.instructionsPromptMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showInstructionsPrompt = false
                        markInstructionsPromptShown(ctx)
                        navController.navigate(NavRoutes.INSTRUCTIONS)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(strings.instructionsPromptView)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showInstructionsPrompt = false
                        markInstructionsPromptShown(ctx)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(strings.instructionsPromptSkip)
                }
            }
        )
    }
}

@Composable
private fun DecoySetupDialog(
    repository: NoteRepository,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val strings = LocalStrings.current
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dialogKeyboardState = remember { KeyboardState() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        strings.settingsDecoySetup,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        strings.settingsDecoySubtitle,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                        PasswordField(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = strings.setupDecoyPassword,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                        PasswordField(
                            value = confirm,
                            onValueChange = { confirm = it; error = null },
                            label = strings.setupConfirmPassword,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) { Text(strings.cancel) }
                        Button(
                            onClick = {
                                when {
                                    password.length < 8 -> error = "Password must be at least 8 characters"
                                    password != confirm -> error = "Passwords do not match"
                                    else -> {
                                        loading = true
                                        scope.launch {
                                            val result = repository.setupDecoyPassword(password.toCharArray())
                                            password = ""; confirm = ""
                                            loading = false
                                            if (result.isSuccess) onComplete()
                                            else error = "Failed: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                }
                            },
                            enabled = !loading && password.isNotEmpty() && confirm.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) { Text(strings.confirm) }
                    }
                }
                AnimatedVisibility(
                    visible = dialogKeyboardState.isVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    CustomKeyboard(
                        onKeyPress = { dialogKeyboardState.onKeyPress?.invoke(it) },
                        onDelete = { dialogKeyboardState.onDelete?.invoke() },
                        onDone = { dialogKeyboardState.onDone?.invoke() },
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    )
                }
            }
        }
    }
}

private fun markInstructionsPromptShown(context: android.content.Context) {
    context.getSharedPreferences("locale_prefs", android.content.Context.MODE_PRIVATE)
        .edit().putBoolean("instructions_prompt_shown", true).commit()
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, iconSize: androidx.compose.ui.unit.Dp = 24.dp, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ChangePasswordDialog(
    repository: NoteRepository,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    var current by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dialogKeyboardState = remember { KeyboardState() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        strings.changePassword,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    if (success) {
                        Text(strings.passwordChanged, style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(strings.passwordChangedSubtitle, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text(strings.ok)
                        }
                    } else {
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = current,
                                onValueChange = { current = it; error = null },
                                label = strings.currentPassword,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = newPw,
                                onValueChange = { newPw = it; error = null },
                                label = strings.newPassword,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = confirm,
                                onValueChange = { confirm = it; error = null },
                                label = strings.confirmNewPassword,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (error != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(error!!, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.cancel) }
                            Button(
                                onClick = {
                                    when {
                                        newPw.length < 8 -> error = "New password must be at least 8 characters"
                                        newPw != confirm -> error = "New passwords do not match"
                                        else -> {
                                            loading = true
                                            scope.launch {
                                                val result = repository.changePassword(
                                                    current.toCharArray(),
                                                    newPw.toCharArray()
                                                )
                                                current = ""; newPw = ""; confirm = ""
                                                loading = false
                                                if (result.isSuccess) success = true
                                                else error = "Failed: ${result.exceptionOrNull()?.message}"
                                            }
                                        }
                                    }
                                },
                                enabled = !loading && current.isNotEmpty() && newPw.isNotEmpty() && confirm.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.confirm) }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = dialogKeyboardState.isVisible,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    CustomKeyboard(
                        onKeyPress = { dialogKeyboardState.onKeyPress?.invoke(it) },
                        onDelete = { dialogKeyboardState.onDelete?.invoke() },
                        onDone = { dialogKeyboardState.onDone?.invoke() },
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    )
                }
            }
        }
    }
}
