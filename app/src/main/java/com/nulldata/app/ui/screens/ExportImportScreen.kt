package com.nulldata.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nulldata.app.data.repository.NoteRepository
import com.nulldata.app.ui.components.KeyboardHost
import com.nulldata.app.ui.components.PasswordField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(
    repository: NoteRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var exportKey by remember { mutableStateOf("") }
    var exportResult by remember { mutableStateOf<String?>(null) }
    var exportError by remember { mutableStateOf<String?>(null) }
    var importBlob by remember { mutableStateOf("") }
    var importKey by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf<String?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }

    KeyboardHost {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export / Import") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("✕", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Export section
            Text("Export Encrypted Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "All notes will be encrypted with the export passphrase below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = exportKey,
                onValueChange = { exportKey = it; exportError = null },
                label = "Export passphrase",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (exportKey.isEmpty()) {
                        exportError = "Passphrase required"
                        return@Button
                    }
                    scope.launch {
                        val result = repository.encryptExport(exportKey.toCharArray())
                        exportKey = ""
                        if (result.isSuccess) {
                            exportResult = result.getOrNull()
                            // Save to file
                            try {
                                withContext(Dispatchers.IO) {
                                    val dir = context.getExternalFilesDir(null)
                                    val file = File(dir, "nulldata_backup_${System.currentTimeMillis()}.txt")
                                    FileOutputStream(file).use {
                                        it.write(result.getOrNull()!!.toByteArray(Charsets.UTF_8))
                                    }
                                }
                                Toast.makeText(context, "Backup saved", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                exportError = "Save failed: ${e.message}"
                            }
                        } else {
                            exportError = "Export failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export")
            }
            if (exportError != null) {
                Text(exportError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (exportResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = exportResult!!,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exported data (also saved to file)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            androidx.compose.material3.HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Import section
            Text("Import Encrypted Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = importBlob,
                onValueChange = { importBlob = it; importError = null },
                label = { Text("Paste backup blob") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = importKey,
                onValueChange = { importKey = it; importError = null },
                label = "Export passphrase",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (importBlob.isEmpty() || importKey.isEmpty()) {
                        importError = "Blob and passphrase required"
                        return@Button
                    }
                    scope.launch {
                        val result = repository.importEncrypted(importBlob, importKey.toCharArray())
                        importKey = ""
                        if (result.isSuccess) {
                            importResult = "Imported ${result.getOrNull()} notes successfully"
                            importBlob = ""
                        } else {
                            importError = "Import failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import")
            }
            if (importError != null) {
                Text(importError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (importResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(importResult!!, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
    } // KeyboardHost
}
