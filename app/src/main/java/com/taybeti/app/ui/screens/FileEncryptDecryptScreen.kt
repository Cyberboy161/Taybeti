package com.taybeti.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.taybeti.app.security.CryptoUtils
import com.taybeti.app.security.SecureMemory
import com.taybeti.app.ui.components.KeyboardHost
import com.taybeti.app.ui.components.PasswordField
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.generateRandomKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileEncryptDecryptScreen(
    onOpenDrawer: () -> Unit
) {
    val strings = LocalStrings.current
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(strings.fileEncryptTab, strings.fileDecryptTab)

    KeyboardHost {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(strings.fileEncTitle) },
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
                    0 -> FileEncryptTab()
                    1 -> FileDecryptTab()
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun FileEncryptTab() {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var sourceFileName by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var isEncrypting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var encryptFilename by remember { mutableStateOf(true) }
    var encryptExtension by remember { mutableStateOf(true) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            sourceUri = uri
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        sourceFileName = it.getString(nameIdx)
                    }
                }
            }
            error = null
            success = false
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            sourceUri = cameraPhotoUri
            sourceFileName = "camera_photo_${System.currentTimeMillis()}.jpg"
            error = null
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && sourceUri != null) {
            isEncrypting = true
            progress = 0f
            scope.launch {
                try {
                    val tempInput = File(context.cacheDir, "encrypt_input")
                    val tempOutput = File(context.cacheDir, "encrypt_output")
                    try {
                        context.contentResolver.openInputStream(sourceUri!!).use { input ->
                            tempInput.outputStream().use { output ->
                                input?.copyTo(output)
                            }
                        }

                        withContext(Dispatchers.IO) {
                            CryptoUtils.encryptFile(
                                tempInput,
                                tempOutput,
                                passphrase.toCharArray()
                            ) { processed, total ->
                                progress = processed.toFloat() / total.toFloat()
                            }
                        }

                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            tempOutput.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }

                        success = true
                        passphrase = ""
                        confirmPassphrase = ""
                        sourceUri = null
                        sourceFileName = ""
                    } finally {
                        tempInput.delete()
                        tempOutput.delete()
                    }
                } catch (e: Exception) {
                    error = strings.encryptFailed + ": ${e.message}"
                }
                isEncrypting = false
            }
        }
    }

    fun buildOutputName(): String {
        if (sourceFileName.isEmpty()) return "encrypted.taybeti"
        val dotIndex = sourceFileName.lastIndexOf('.')
        val baseName = if (dotIndex > 0) sourceFileName.substring(0, dotIndex) else sourceFileName
        val extension = if (dotIndex > 0) sourceFileName.substring(dotIndex + 1) else ""

        val finalBase = if (encryptFilename) generateRandomHash() else baseName
        val finalExt = if (encryptExtension) "taybeti" else extension

        return if (finalExt.isNotEmpty()) "$finalBase.$finalExt" else finalBase
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    strings.selectSourceFile,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { pickFileLauncher.launch(arrayOf("*/*")) },
                        enabled = !isEncrypting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.selectFile)
                    }
                    Button(
                        onClick = {
                            val photoFile = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
                            cameraPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            takePictureLauncher.launch(cameraPhotoUri!!)
                        },
                        enabled = !isEncrypting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.encryptFromCamera)
                    }
                }

                if (sourceFileName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${strings.selectedFile}: $sourceFileName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${strings.outputFileName}: ${buildOutputName()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    strings.encryptionOptions,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { encryptFilename = !encryptFilename }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = encryptFilename,
                        onCheckedChange = { encryptFilename = it },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(strings.encryptFilename, style = MaterialTheme.typography.bodyMedium)
                        Text(strings.encryptFilenameDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { encryptExtension = !encryptExtension }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = encryptExtension,
                        onCheckedChange = { encryptExtension = it },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(strings.encryptExtension, style = MaterialTheme.typography.bodyMedium)
                        Text(strings.encryptExtensionDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = passphrase,
            onValueChange = { passphrase = it; error = null },
            label = strings.encryptionKey,
            modifier = Modifier.fillMaxWidth()
        )
        if (passphrase.isNotEmpty() && passphrase.length < 8) {
            Text(
                strings.keyTooShort,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = confirmPassphrase,
            onValueChange = { confirmPassphrase = it; error = null },
            label = strings.confirmEncryptionKey,
            modifier = Modifier.fillMaxWidth()
        )
        if (confirmPassphrase.isNotEmpty() && passphrase != confirmPassphrase) {
            Text(
                strings.keysDoNotMatch,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (sourceUri == null) {
                    error = strings.noFileSelected
                    return@Button
                }
                if (passphrase.isEmpty()) {
                    error = strings.keyRequired
                    return@Button
                }
                if (passphrase != confirmPassphrase) {
                    error = strings.keysDoNotMatch
                    return@Button
                }
                saveFileLauncher.launch(buildOutputName())
            },
            enabled = !isEncrypting && sourceUri != null && passphrase.isNotEmpty() && passphrase == confirmPassphrase,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEncrypting) strings.encrypting else strings.encryptFile)
        }

        if (isEncrypting) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (success) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                strings.fileEncryptedSuccess,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FileDecryptTab() {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var sourceFileName by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var isDecrypting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            sourceUri = uri
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        sourceFileName = it.getString(nameIdx)
                    }
                }
            }
            error = null
            success = false
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && sourceUri != null) {
            isDecrypting = true
            progress = 0f
            scope.launch {
                try {
                    val tempInput = File(context.cacheDir, "decrypt_input")
                    val tempOutput = File(context.cacheDir, "decrypt_output")
                    try {
                        context.contentResolver.openInputStream(sourceUri!!).use { input ->
                            tempInput.outputStream().use { output ->
                                input?.copyTo(output)
                            }
                        }

                        withContext(Dispatchers.IO) {
                            CryptoUtils.decryptFile(
                                tempInput,
                                tempOutput,
                                passphrase.toCharArray()
                            ) { processed, total ->
                                progress = processed.toFloat() / total.toFloat()
                            }
                        }

                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            tempOutput.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }

                        success = true
                        passphrase = ""
                        sourceUri = null
                        sourceFileName = ""
                    } finally {
                        tempInput.delete()
                        tempOutput.delete()
                    }
                } catch (e: Exception) {
                    error = strings.decryptFailed
                    SecureMemory.clear(passphrase.toCharArray())
                }
                isDecrypting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    strings.selectEncryptedFile,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { pickFileLauncher.launch(arrayOf("*/*")) },
                        enabled = !isDecrypting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(strings.selectFile)
                    }
                }

                if (sourceFileName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${strings.selectedFile}: $sourceFileName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = passphrase,
            onValueChange = { passphrase = it; error = null },
            label = strings.decryptionKey,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (sourceUri == null) {
                    error = strings.noFileSelected
                    return@Button
                }
                if (passphrase.isEmpty()) {
                    error = strings.keyRequired
                    return@Button
                }
                val outputName = sourceFileName.replace(".taybeti", "")
                saveFileLauncher.launch(outputName)
            },
            enabled = !isDecrypting && sourceUri != null && passphrase.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isDecrypting) strings.decrypting else strings.decryptFile)
        }

        if (isDecrypting) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        if (success) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                strings.fileDecryptedSuccess,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun generateRandomHash(): String {
    val random = SecureRandom()
    val bytes = ByteArray(8)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
