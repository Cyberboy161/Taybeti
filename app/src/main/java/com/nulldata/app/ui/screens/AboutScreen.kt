package com.nulldata.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.height(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "NulldData",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "License",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NulldData is open-source software licensed under the MIT License.\n\n" +
                        "Copyright (c) 2024 NulldData\n\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy " +
                        "of this software and associated documentation files (the \"Software\"), to deal " +
                        "in the Software without restriction, including without limitation the rights " +
                        "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell " +
                        "copies of the Software, and to permit persons to whom the Software is " +
                        "furnished to do so, subject to the following conditions:\n\n" +
                        "The above copyright notice and this permission notice shall be included in all " +
                        "copies or substantial portions of the Software.\n\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR " +
                        "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
                        "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Security Architecture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "• Argon2id key derivation (6 iterations, 64MB memory, 4 parallelism)\n" +
                        "• AES-256-GCM encryption for all data\n" +
                        "• Random 32-byte salts, random 12-byte IVs\n" +
                        "• Zero-trust master password (verified via decryption canary)\n" +
                        "• Per-note encryption keys\n" +
                        "• In-app custom keyboard (no system keyboard, no learning)\n" +
                        "• FLAG_SECURE on all screens (no screenshots)\n" +
                        "• No internet permission\n" +
                        "• Char[] zeroing after password/key use\n" +
                        "• No Android Keystore dependency\n" +
                        "• No Google Play Services",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Threat Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NulldData is designed to protect against:\n" +
                        "• Physical device seizure (encrypted-at-rest)\n" +
                        "• Malicious apps on device (no IPC, FLAG_SECURE)\n" +
                        "• Keyboard logging (custom in-app keyboard)\n" +
                        "• Screenshot capture (FLAG_SECURE)\n" +
                        "• Android backup extraction (backup disabled)\n" +
                        "• Cloud sync leaks (no internet permission)\n" +
                        "• Google/OS-level key extraction (no Android Keystore)\n\n" +
                        "Limitations:\n" +
                        "• Does not protect against compromised OS kernel\n" +
                        "• Does not protect against hardware keyloggers\n" +
                        "• RAM can contain plaintext while note is open",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
