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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nulldata.app.ui.components.KeyboardHost
import com.nulldata.app.util.DecoyEncoder
import com.nulldata.app.util.DecoyPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoyDecryptScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedPlatform by remember { mutableStateOf(DecoyPlatform.YOUTUBE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var urlText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    KeyboardHost {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Decoy Decrypt", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                    label = { Text("Paste disguised URL") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (urlText.isBlank()) {
                            error = "Please paste a URL"
                            return@Button
                        }
                        val decoded = DecoyEncoder.decode(urlText.trim(), selectedPlatform)
                        if (decoded == null) {
                            error = "Invalid URL for selected platform"
                            return@Button
                        }
                        result = decoded
                        error = null
                        Toast.makeText(context, "Decoded", Toast.LENGTH_SHORT).show()
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
                    Text(
                        "Decoded encrypted blob:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Copy this blob and use the Encrypt/Decrypt tool to decrypt with your key.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
