package com.nulldata.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: NoteRepository,
    onLoginSuccess: (isDecoy: Boolean) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var lockedOut by remember { mutableStateOf(false) }
    var remainingSec by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    fun performLogin() {
        if (password.isEmpty() || isLoading || lockedOut) return
        isLoading = true
        error = null
        scope.launch {
            val result = repository.attemptLogin(password.toCharArray())
            password = ""
            isLoading = false
            when {
                result.lockedOut -> {
                    lockedOut = true
                    remainingSec = ((result.remainingMs) / 1000).toInt()
                    delay(result.remainingMs)
                    lockedOut = false
                }
                result.success -> onLoginSuccess(result.isDecoy)
                else -> error = result.error
            }
        }
    }

    KeyboardHost {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Unlock Vault",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your master password",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it; error = null },
                label = "Master Password",
                modifier = Modifier.fillMaxWidth(),
                onDone = {
                    if (password.isNotEmpty() && !isLoading && !lockedOut) {
                        performLogin()
                    }
                }
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (lockedOut) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Too many failed attempts. Wait ${remainingSec}s.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { performLogin() },
                enabled = !isLoading && !lockedOut && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Unlocking..." else "Unlock")
            }
        }
    }
}
}
