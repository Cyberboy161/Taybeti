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
import androidx.compose.material.icons.filled.Menu
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
import com.nulldata.app.ui.components.SharingTutorialButton
import com.nulldata.app.util.DecoyEncoder
import com.nulldata.app.util.DecoyPlatform
import com.nulldata.app.util.LocalStrings
import com.nulldata.app.util.generateRandomKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptDecryptScreen(
    onOpenDrawer: () -> Unit
) {
    val strings = LocalStrings.current
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(strings.encryptTab, strings.decryptTab, strings.decoyTab)

    DisposableEffect(Unit) {
        onDispose { /* Clear any sensitive data */ }
    }

    KeyboardHost {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.encryptDecryptTitle) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
    var useDisguise by remember { mutableStateOf(false) } // false = raw, true = use platform
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showDisguise by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val strings = LocalStrings.current

    val disguisedUrl = remember(output, selectedPlatform, useDisguise) {
        if (output.isEmpty()) ""
        else if (useDisguise) DecoyEncoder.encode(output, selectedPlatform)
        else output
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
            label = strings.plaintext,
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = key,
            onValueChange = { key = it; error = null },
            label = strings.encryptionKey,
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
                Text(strings.generateKey)
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
                useDisguise = false
                showDisguise = false
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
                            error = strings.encryptFailed + ": ${e.message}"
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) strings.encrypting else strings.encrypt)
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            // ─── Raw encrypted blob ───
            Text(strings.encryptedOutput, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = output,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                label = { Text(strings.encryptedBlob) },
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(output))
                        Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = strings.copy)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Disguise section ───

            if (!showDisguise) {
                Button(
                    onClick = { showDisguise = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.disguiseAsLink)
                }
            } else {
                Text(strings.disguiseAs, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (useDisguise) selectedPlatform.label else "None (keep raw)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.platform) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (keep raw)") },
                            onClick = {
                                useDisguise = false
                                dropdownExpanded = false
                            }
                        )
                        DecoyPlatform.entries.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform.label) },
                                onClick = {
                                    selectedPlatform = platform
                                    useDisguise = true
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = disguisedUrl,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text(if (useDisguise) strings.disguisedUrl else strings.encryptedBlob) },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(disguisedUrl))
                            Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = strings.copy)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SharingTutorialButton(modifier = Modifier.fillMaxWidth())
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
    val strings = LocalStrings.current

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
            label = strings.encryptedBlob,
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
                    Icon(Icons.Default.ContentPaste, contentDescription = strings.paste)
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
                                error = strings.invalidBlob
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
                            error = strings.decryptFailed
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) strings.decrypting else strings.decrypt)
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(strings.decrypted, style = MaterialTheme.typography.titleMedium)
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
                        Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = strings.copy)
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
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            strings.decoyHint,
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
                label = { Text(strings.platform) },
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
            label = { Text(strings.disguisedUrl) },
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
                    Icon(Icons.Default.ContentPaste, contentDescription = strings.paste)
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (urlText.isBlank()) {
                    error = strings.pasteUrlFirst
                    return@Button
                }
                val decoded = DecoyEncoder.decode(urlText.trim(), selectedPlatform)
                if (decoded == null) {
                    error = strings.invalidUrl
                    return@Button
                }
                result = decoded
                error = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.decode)
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(strings.extractedBlob, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = result!!,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text(strings.encryptedBlob) },
                trailingIcon = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(result!!))
                        Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = strings.copy)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                strings.switchToDecrypt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
