package com.nulldata.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nulldata.app.data.repository.NoteRepository
import com.nulldata.app.ui.components.KeyboardHost
import com.nulldata.app.ui.components.PasswordField
import com.nulldata.app.util.Constants
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(
    repository: NoteRepository,
    onSetupComplete: () -> Unit
) {
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var decoyPassword by remember { mutableStateOf("") }
    var confirmDecoy by remember { mutableStateOf("") }
    var enableDecoy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    KeyboardHost {
        Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Set Master Password",
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
                label = "Master Password (min ${Constants.MIN_MASTER_PASSWORD_LENGTH} chars)",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
                label = "Confirm Master Password",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Decoy option
            androidx.compose.material3.HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable decoy password", style = MaterialTheme.typography.titleMedium)
                Switch(checked = enableDecoy, onCheckedChange = {
                    enableDecoy = it
                    if (!it) { decoyPassword = ""; confirmDecoy = "" }
                })
            }
            Text(
                "A decoy password unlocks a separate set of notes. Use it for plausible deniability.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            if (enableDecoy) {
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField(
                    value = decoyPassword,
                    onValueChange = { decoyPassword = it; error = null },
                    label = "Decoy Password (min ${Constants.MIN_MASTER_PASSWORD_LENGTH} chars)",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField(
                    value = confirmDecoy,
                    onValueChange = { confirmDecoy = it; error = null },
                    label = "Confirm Decoy Password",
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

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    when {
                        masterPassword.length < Constants.MIN_MASTER_PASSWORD_LENGTH ->
                            error = "Master password must be at least ${Constants.MIN_MASTER_PASSWORD_LENGTH} characters"
                        masterPassword != confirmPassword ->
                            error = "Passwords do not match"
                        enableDecoy && decoyPassword.length < Constants.MIN_MASTER_PASSWORD_LENGTH ->
                            error = "Decoy password must be at least ${Constants.MIN_MASTER_PASSWORD_LENGTH} characters"
                        enableDecoy && decoyPassword != confirmDecoy ->
                            error = "Decoy passwords do not match"
                        enableDecoy && decoyPassword == masterPassword ->
                            error = "Decoy password must differ from master password"
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
                                if (result.isSuccess) onSetupComplete()
                                else error = "Setup failed: ${result.exceptionOrNull()?.message}"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Setting up..." else "Create Vault")
            }
        }
    }
}
}
