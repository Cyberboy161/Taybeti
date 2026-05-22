package com.taybeti.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taybeti.app.data.entities.NoteEntity
import com.taybeti.app.data.repository.NoteRepository
import com.taybeti.app.security.AttachmentManager
import com.taybeti.app.security.AttachmentManager.getAttachmentsList
import com.taybeti.app.security.AttachmentManager.attachmentsToJson
import com.taybeti.app.security.CryptoUtils
import com.taybeti.app.security.SecureMemory
import com.taybeti.app.ui.components.CustomKeyboard
import com.taybeti.app.util.DecoyEncoder
import com.taybeti.app.util.DecoyPlatform
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.formatTimestamp
import com.taybeti.app.util.generateNoteId
import kotlinx.coroutines.launch
import java.util.Base64
import org.json.JSONObject
import org.json.JSONArray

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
    var searchFieldActive by remember { mutableStateOf(false) }
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(
                                    width = 1.dp,
                                    color = if (searchFieldActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { searchFieldActive = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "Search notes..." else searchQuery,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (searchQuery.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                        searchFieldActive = isSearchActive
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

    if (isSearchActive && searchFieldActive) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CustomKeyboard(
                onKeyPress = { char -> searchQuery += char },
                onDelete = { if (searchQuery.isNotEmpty()) searchQuery = searchQuery.dropLast(1) },
                onDone = { searchFieldActive = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadNoteDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { com.taybeti.app.data.database.AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var title by remember { mutableStateOf("") }
    var encryptedBlob by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var activeField by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Load Encrypted Note") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Paste the encrypted blob or disguised URL, then enter the passphrase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LoadTextField(
                        label = "Note title",
                        value = title,
                        isActive = activeField == "title",
                        onActivate = { activeField = "title" },
                        onValueChange = { title = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LoadTextField(
                                label = "Encrypted blob or URL",
                                value = encryptedBlob,
                                isActive = activeField == "blob",
                                onActivate = { activeField = "blob" },
                                onValueChange = { encryptedBlob = it; error = null },
                                minLines = 2,
                                maxLines = 3
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val clipText = clipboard.getText()?.text
                                if (clipText != null) {
                                    encryptedBlob = clipText.toString()
                                    error = null
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Default.ContentPaste,
                                "Paste",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    LoadTextField(
                        label = "Passphrase",
                        value = passphrase,
                        isActive = activeField == "passphrase",
                        onActivate = { activeField = "passphrase" },
                        onValueChange = { passphrase = it; error = null },
                        isPassword = true
                    )

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
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
                                    val plaintextBytes = CryptoUtils.decrypt(ciphertext, key, iv, tag)
                                    SecureMemory.clear(key)
                                    SecureMemory.clear(passphrase.toCharArray())

                                    val plaintext = String(plaintextBytes, Charsets.UTF_8)
                                    val noteId = generateNoteId()
                                    val now = System.currentTimeMillis()

                                    var noteContent = plaintext
                                    var attachmentsJson = "[]"

                                    try {
                                        val json = JSONObject(plaintext)
                                        if (json.has("content")) {
                                            noteContent = json.getString("content")
                                        }
                                        if (json.has("attachments")) {
                                            attachmentsJson = json.getJSONArray("attachments").toString()
                                        }
                                        if (json.has("images")) {
                                            val imagesArray = json.getJSONArray("images")
                                            val imagesList = mutableListOf<String>()
                                            for (i in 0 until imagesArray.length()) {
                                                val imgObj = imagesArray.getJSONObject(i)
                                                imagesList.add(imgObj.toString())
                                            }
                                            attachmentsJson = "[" + imagesList.joinToString(",") + "]"
                                        }
                                    } catch (_: Exception) {
                                    }

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
                                        modifiedDate = now,
                                        attachments = attachmentsJson
                                    )
                                    db.noteDao().insert(note)

                                    val attachments = getAttachmentsList(attachmentsJson, context, noteId)
                                    if (attachments.isNotEmpty()) {
                                        for (att in attachments) {
                                            if (att.encryptedPath.isNotEmpty()) {
                                                val encFile = java.io.File(att.encryptedPath)
                                                if (encFile.exists()) {
                                                    val destDir = AttachmentManager.getNoteDir(context, noteId)
                                                    val destFile = java.io.File(destDir, encFile.name)
                                                    encFile.copyTo(destFile, overwrite = true)
                                                }
                                            }
                                        }
                                    }

                                    onSuccess()
                                } catch (e: Exception) {
                                    error = "Failed to decrypt: ${e.message ?: "Wrong passphrase"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isLoading) "Loading..." else "Load Note")
                    }
                }

                if (activeField != null) {
                    CustomKeyboard(
                        onKeyPress = { char ->
                            when (activeField) {
                                "title" -> title += char
                                "blob" -> { encryptedBlob += char; error = null }
                                "passphrase" -> { passphrase += char; error = null }
                            }
                        },
                        onDelete = {
                            when (activeField) {
                                "title" -> if (title.isNotEmpty()) title = title.dropLast(1)
                                "blob" -> { if (encryptedBlob.isNotEmpty()) encryptedBlob = encryptedBlob.dropLast(1); error = null }
                                "passphrase" -> { if (passphrase.isNotEmpty()) passphrase = passphrase.dropLast(1); error = null }
                            }
                        },
                        onDone = { activeField = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadTextField(
    label: String,
    value: String,
    isActive: Boolean,
    onActivate: () -> Unit,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (maxLines > 1) Modifier.heightIn(max = (maxLines * 24).dp) else Modifier)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onActivate)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (value.isEmpty()) "Tap to enter..." else if (isPassword) "•".repeat(value.length) else value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface,
                minLines = minLines
            )
        }
    }
}
