package com.taybeti.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taybeti.app.data.entities.NoteEntity
import com.taybeti.app.data.repository.NoteRepository
import com.taybeti.app.security.CryptoUtils
import com.taybeti.app.util.DecoyEncoder
import com.taybeti.app.util.DecoyPlatform
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.formatTimestamp
import com.taybeti.app.util.generateNoteId
import kotlinx.coroutines.launch
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    isDecoy: Boolean,
    showFavorites: Boolean = false,
    onNoteClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    showNoteTitle: Boolean = true,
    showNoteDate: Boolean = true
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var notes by remember { mutableStateOf<List<NoteEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showLoadNoteDialog by remember { mutableStateOf(false) }
    val db = remember { com.taybeti.app.data.database.AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            notes = when {
                showFavorites -> db.noteDao().getFavorites(isDecoy)
                else -> db.noteDao().getAllActive(isDecoy)
            }
        }
    }

    LaunchedEffect(showFavorites, isDecoy) { refresh() }

    val filteredNotes = remember(notes, searchQuery, isSearchActive) {
        if (!isSearchActive || searchQuery.isBlank()) {
            notes
        } else {
            val query = searchQuery.lowercase()
            notes.filter { note ->
                note.title.lowercase().contains(query)
            }
        }
    }

    val title = when {
        showFavorites -> strings.favorites
        isDecoy -> strings.decoyNotes
        else -> strings.allNotes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search notes...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showLoadNoteDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Load Note")
                }
                FloatingActionButton(onClick = {
                    val newId = generateNoteId()
                    onNoteClick(newId)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "New Note")
                }
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        showFavorites -> strings.noFavorites
                        isDecoy -> strings.noDecoyNotes
                        else -> strings.noNotes
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteListItem(
                        note = note,
                        strings = strings,
                        db = db,
                        onClick = { onNoteClick(note.id) },
                        onChanged = { refresh() },
                        showNoteTitle = showNoteTitle,
                        showNoteDate = showNoteDate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showLoadNoteDialog) {
        LoadNoteDialog(
            onDismiss = { showLoadNoteDialog = false },
            onSuccess = {
                showLoadNoteDialog = false
                refresh()
            }
        )
    }
}

@Composable
private fun NoteListItem(
    note: NoteEntity,
    strings: com.taybeti.app.util.AppStrings,
    db: com.taybeti.app.data.database.AppDatabase,
    onClick: () -> Unit,
    onChanged: () -> Unit,
    showNoteTitle: Boolean = true,
    showNoteDate: Boolean = true
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (showNoteTitle) {
                    Text(
                        text = note.title.ifEmpty { "Untitled" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showNoteDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = strings.encryptedNote,
                            modifier = Modifier.height(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatTimestamp(note.modifiedDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = {
                    scope.launch {
                        db.noteDao().update(note.copy(
                            isFavorite = !note.isFavorite,
                            modifiedDate = System.currentTimeMillis()
                        ))
                        onChanged()
                    }
                }) {
                    Icon(
                        if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (note.isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        db.noteDao().update(note.copy(
                            isDeleted = true,
                            modifiedDate = System.currentTimeMillis()
                        ))
                        onChanged()
                    }
                }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadNoteDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { com.taybeti.app.data.database.AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var encryptedBlob by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Load Encrypted Note") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Paste the encrypted blob or disguised URL, then enter the passphrase to decrypt and load the note.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = encryptedBlob,
                    onValueChange = { encryptedBlob = it; error = null },
                    label = { Text("Encrypted blob or disguised URL") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it; error = null },
                    label = { Text("Passphrase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Title is required"
                        return@Button
                    }
                    if (encryptedBlob.isBlank()) {
                        error = "Encrypted blob is required"
                        return@Button
                    }
                    if (passphrase.isBlank()) {
                        error = "Passphrase is required"
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            var rawBlob = encryptedBlob.trim()

                            for (platform in DecoyPlatform.entries) {
                                val decoded = DecoyEncoder.decode(rawBlob, platform)
                                if (decoded != null) {
                                    rawBlob = decoded
                                    break
                                }
                            }

                            val parts = rawBlob.split("::")
                            if (parts.size != 4) {
                                error = "Invalid blob format. Expected salt::iv::tag::ciphertext"
                                isLoading = false
                                return@launch
                            }

                            val b64 = Base64.getDecoder()
                            val salt = b64.decode(parts[0])
                            val iv = b64.decode(parts[1])
                            val tag = b64.decode(parts[2])
                            val ciphertext = b64.decode(parts[3])

                            val key = CryptoUtils.deriveKey(passphrase.toCharArray(), salt)
                            val plaintext = CryptoUtils.decrypt(ciphertext, key, iv, tag)
                            com.taybeti.app.security.SecureMemory.clear(key)
                            com.taybeti.app.security.SecureMemory.clear(passphrase.toCharArray())

                            val noteId = generateNoteId()
                            val now = System.currentTimeMillis()
                            val note = NoteEntity(
                                id = noteId,
                                title = title,
                                salt = salt,
                                iv = iv,
                                ciphertext = ciphertext,
                                tag = tag,
                                isEncrypted = true,
                                isFavorite = false,
                                isDeleted = false,
                                isDecoyNote = false,
                                createdDate = now,
                                modifiedDate = now
                            )
                            db.noteDao().insert(note)
                            onSuccess()
                        } catch (e: Exception) {
                            error = "Failed to decrypt: ${e.message ?: "Wrong passphrase"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Loading..." else "Load Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
