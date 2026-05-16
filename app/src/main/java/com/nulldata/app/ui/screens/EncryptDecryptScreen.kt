package com.nulldata.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.nulldata.app.security.CryptoUtils
import com.nulldata.app.security.SecureMemory
import com.nulldata.app.ui.components.AppTextField
import com.nulldata.app.ui.components.KeyboardHost
import com.nulldata.app.ui.components.PasswordField
import com.nulldata.app.util.DecoyEncoder
import com.nulldata.app.util.DecoyPlatform
import com.nulldata.app.util.generateRandomKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptDecryptScreen(
    onBack: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Encrypt", "Decrypt", "Decoy")

    DisposableEffect(Unit) {
        onDispose { /* Clear any sensitive data */ }
    }

    KeyboardHost {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Encrypt / Decrypt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("✕", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (tabIndex) {
                0 -> EncryptTab()
                1 -> DecryptTab()
                2 -> DecoyTab()
            }
        }
    }
    } // KeyboardHost
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EncryptTab() {
    var plaintext by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf(DecoyPlatform.YOUTUBE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val disguisedUrl = remember(output, selectedPlatform) {
        if (output.isNotEmpty()) DecoyEncoder.encode(output, selectedPlatform) else ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppTextField(
            value = plaintext,
            onValueChange = { plaintext = it; error = null },
            label = "Plaintext",
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = key,
            onValueChange = { key = it; error = null },
            label = "Encryption Key",
            modifier = Modifier.fillMaxWidth()
        )
        if (key.isNotEmpty() && key.length < 8) {
            Text(
                "Warning: key is shorter than 8 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                key = generateRandomKey(20)
            }) {
                Text("Generate 20-char key")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (plaintext.isEmpty() || key.isEmpty()) {
                    error = "Plaintext and key are required"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val salt = CryptoUtils.generateSalt()
                            val derivedKey = CryptoUtils.deriveKey(key.toCharArray(), salt)
                            SecureMemory.clear(key.toCharArray())
                            key = ""

                            val result = CryptoUtils.encrypt(
                                plaintext.toByteArray(Charsets.UTF_8),
                                derivedKey
                            )
                            SecureMemory.clear(derivedKey)

                            val b64 = Base64.getEncoder()
                            output = "${b64.encodeToString(salt)}::${b64.encodeToString(result.iv)}::${b64.encodeToString(result.tag)}::${b64.encodeToString(result.ciphertext)}"
                            error = null
                        } catch (e: Exception) {
                            error = "Encryption failed: ${e.message}"
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Encrypting..." else "Encrypt")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Encrypted Output:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = disguisedUrl,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text("Disguised URL") },
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(disguisedUrl))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPlatform.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Disguise as") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DecoyPlatform.entries.forEach { platform ->
                        DropdownMenuItem(
                            text = { Text(platform.label) },
                            onClick = {
                                selectedPlatform = platform
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DecryptTab() {
    var blob by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            SecureMemory.clear(output.toCharArray())
            output = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppTextField(
            value = blob,
            onValueChange = { blob = it; error = null },
            label = "Encrypted blob",
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            trailingIcon = {
                IconButton(onClick = {
                    val clip = clipboard.getText()
                    if (clip != null) {
                        blob = clip.text
                        error = null
                    }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = key,
            onValueChange = { key = it; error = null },
            label = "Decryption Key",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (blob.isEmpty() || key.isEmpty()) {
                    error = "Blob and key are required"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val parts = blob.split("::")
                            if (parts.size != 4) {
                                error = "Invalid blob format"
                                isLoading = false
                                return@withContext
                            }
                            val b64 = Base64.getDecoder()
                            val salt = b64.decode(parts[0])
                            val iv = b64.decode(parts[1])
                            val tag = b64.decode(parts[2])
                            val ciphertext = b64.decode(parts[3])

                            val derivedKey = CryptoUtils.deriveKey(key.toCharArray(), salt)
                            SecureMemory.clear(key.toCharArray())
                            key = ""

                            val plaintext = CryptoUtils.decrypt(ciphertext, derivedKey, iv, tag)
                            SecureMemory.clear(derivedKey)
                            output = String(plaintext, Charsets.UTF_8)
                            error = null
                        } catch (e: Exception) {
                            error = "Decryption failed (wrong key or corrupted data)"
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Decrypting..." else "Decrypt")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Decrypted:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = output,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(output))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecoyTab() {
    var selectedPlatform by remember { mutableStateOf(DecoyPlatform.YOUTUBE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Paste a disguised URL to extract the encrypted message.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedPlatform.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Platform") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                DecoyPlatform.entries.forEach { platform ->
                    DropdownMenuItem(
                        text = { Text(platform.label) },
                        onClick = {
                            selectedPlatform = platform
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it; error = null; result = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Disguised URL") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    val clip = clipboard.getText()
                    if (clip != null) {
                        urlText = clip.text
                        error = null
                        result = null
                    }
                }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (urlText.isBlank()) {
                    error = "Paste a URL first"
                    return@Button
                }
                val decoded = DecoyEncoder.decode(urlText.trim(), selectedPlatform)
                if (decoded == null) {
                    error = "Invalid URL for selected platform"
                    return@Button
                }
                result = decoded
                error = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Decode")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Extracted encrypted blob:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = result!!,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Encrypted blob") },
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(result!!))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Switch to the Decrypt tab, paste this blob, and enter your key to decrypt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
