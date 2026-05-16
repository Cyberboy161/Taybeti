package com.nulldata.app.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nulldata.app.data.repository.NoteRepository
import com.nulldata.app.ui.screens.AboutScreen
import com.nulldata.app.ui.screens.EncryptDecryptScreen
import com.nulldata.app.ui.screens.ExportImportScreen
import com.nulldata.app.ui.screens.LoginScreen
import com.nulldata.app.ui.screens.NoteEditorScreen
import com.nulldata.app.ui.screens.NoteListScreen
import com.nulldata.app.ui.screens.SettingsScreen
import com.nulldata.app.ui.screens.SetupScreen
import com.nulldata.app.ui.screens.TrashScreen
import kotlinx.coroutines.launch

object NavRoutes {
    const val SETUP = "setup"
    const val LOGIN = "login"
    const val NOTES = "notes"
    const val FAVORITES = "favorites"
    const val EDITOR = "editor/{noteId}"
    const val ENCRYPT_DECRYPT = "encrypt_decrypt"
    const val TRASH = "trash"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val EXPORT_IMPORT = "export_import"

    fun editorRoute(noteId: String) = "editor/$noteId"
}

@Composable
fun AppNavGraph(repository: NoteRepository) {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var isDecoy by remember { mutableStateOf(false) }
    var needsSetup by remember { mutableStateOf<Boolean?>(null) }
    var isDarkTheme by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Initial setup detection
    androidx.compose.runtime.LaunchedEffect(Unit) {
        needsSetup = !repository.hasLoginInfo()
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

    NavHost(
        navController = navController,
        startDestination = when (needsSetup) {
            true -> NavRoutes.SETUP
            false -> NavRoutes.LOGIN
            null -> NavRoutes.SETUP // loading
        }
    ) {
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
                onToggleTheme = { isDarkTheme = it },
                onLogout = {
                    isLoggedIn = false
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainDrawerScreen(
    repository: NoteRepository,
    isDecoy: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
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
                    // Finish the activity
                    (navController.context as? android.app.Activity)?.finish()
                }) {
                    Text("Exit")
                }
            }
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
                    Text(
                        "Taybeti",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        if (isDecoy) "Decoy Vault" else "Secure Vault",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerItem(Icons.Default.Note, "All Notes") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.NOTES) {
                            popUpTo(NavRoutes.NOTES) { inclusive = true }
                        }
                    }
                    DrawerItem(Icons.Default.Star, "Favorites") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.FAVORITES) {
                            popUpTo(NavRoutes.NOTES) { inclusive = false }
                        }
                    }
                    DrawerItem(Icons.Default.SwapHoriz, "Encrypt/Decrypt") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.ENCRYPT_DECRYPT)
                    }
                    DrawerItem(Icons.Default.Delete, "Trash") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.TRASH)
                    }
                    DrawerItem(Icons.Default.Settings, "Settings") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.SETTINGS)
                    }
                    DrawerItem(Icons.Default.Upload, "Export/Import") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.EXPORT_IMPORT)
                    }
                    DrawerItem(Icons.Default.Info, "About") {
                        scope.launch { drawerState.close() }
                        navController.navigate(NavRoutes.ABOUT)
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    androidx.compose.material3.HorizontalDivider()
                    DrawerItem(Icons.Default.Logout, "Lock Vault") {
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
                    }
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
                    }
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
                EncryptDecryptScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.TRASH) {
                TrashScreen(isDecoy = isDecoy, onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(NavRoutes.EXPORT_IMPORT) {
                ExportImportScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
