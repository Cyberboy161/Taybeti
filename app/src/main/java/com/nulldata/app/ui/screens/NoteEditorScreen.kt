package com.nulldata.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nulldata.app.data.entities.NoteEntity
import com.nulldata.app.data.repository.NoteRepository
import com.nulldata.app.security.SecureMemory
import com.nulldata.app.ui.components.AppTextField
import com.nulldata.app.ui.components.CustomKeyboard
import com.nulldata.app.ui.components.KeyboardHost
import com.nulldata.app.ui.components.KeyboardState
import com.nulldata.app.ui.components.LocalKeyboardState
import com.nulldata.app.ui.components.PasswordField
import com.nulldata.app.util.Constants
import com.nulldata.app.util.DecoyEncoder
import com.nulldata.app.util.DecoyPlatform
import com.nulldata.app.util.generateRandomKey
import kotlinx.coroutines.launch
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    repository: NoteRepository,
    isDecoy: Boolean,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var noteEntity by remember { mutableStateOf<NoteEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var plaintext by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(true) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var showCreateKeyDialog by remember { mutableStateOf(true) }
    var noteKey by remember { mutableStateOf("") }
    var keyError by remember { mutableStateOf<String?>(null) }
    var generatedKey by remember { mutableStateOf("") }
    var isNewNote by remember { mutableStateOf(true) }
    var encryptedOutput by remember { mutableStateOf("") }
    var showEncryptedOutput by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showEditKeyDialog by remember { mutableStateOf(false) }
    var editKey by remember { mutableStateOf("") }
    var editKeyError by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current

    val hasUnsavedChanges = !isLocked && (title.isNotEmpty() || plaintext.isNotEmpty())

    KeyboardHost {
        BackHandler {
            if (hasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                onBack()
            }
        }

        if (showUnsavedDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedDialog = false },
                title = { Text("Discard note?") },
                text = { Text("You have unsaved content. Encrypt first to save it, or discard your changes.") },
                confirmButton = {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text("Stay")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        SecureMemory.clear(plaintext.toCharArray())
                        plaintext = ""
                        onBack()
                    }) {
                        Text("Discard", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        // Edit key verification dialog
        if (showEditKeyDialog) {
            val dialogKeyboardState = remember { KeyboardState() }
            AlertDialog(
                onDismissRequest = {
                    showEditKeyDialog = false
                    editKeyError = null
                    editKey = ""
                },
                title = { Text("Enter Note Key") },
                text = {
                    Column {
                        Text("Re-enter the key to unlock and edit this note.")
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = editKey,
                                onValueChange = { editKey = it; editKeyError = null },
                                label = "Note key",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (dialogKeyboardState.isVisible) {
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomKeyboard(
                                onKeyPress = { dialogKeyboardState.onKeyPress?.invoke(it) },
                                onDelete = { dialogKeyboardState.onDelete?.invoke() },
                                onDone = { dialogKeyboardState.onDone?.invoke() }
                            )
                        }
                        if (editKeyError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(editKeyError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editKey.isEmpty()) {
                            editKeyError = "Key is required"
                            return@Button
                        }
                        scope.launch {
                            val entity = noteEntity
                            if (entity != null) {
                                val result = repository.decryptNoteContent(entity, editKey.toCharArray())
                                if (result.isSuccess) {
                                    val decrypted = result.getOrNull()!!
                                    plaintext = String(decrypted, Charsets.UTF_8)
                                    title = entity.title
                                    showEditKeyDialog = false
                                    showEncryptedOutput = false
                                    encryptedOutput = ""
                                    isLocked = false
                                    SecureMemory.clear(editKey.toCharArray())
                                    editKey = ""
                                    editKeyError = null
                                } else {
                                    editKeyError = "Wrong key"
                                }
                            }
                        }
                    }) { Text("Unlock") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showEditKeyDialog = false
                        editKeyError = null
                        editKey = ""
                    }) { Text("Cancel") }
                }
            )
        }

        LaunchedEffect(noteId) {
            scope.launch {
                val db = com.nulldata.app.data.database.AppDatabase.getInstance(context)
                val existing = db.noteDao().getById(noteId)
                if (existing != null) {
                    noteEntity = existing
                    isNewNote = false
                    showCreateKeyDialog = false
                    showKeyDialog = true
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                SecureMemory.clear(plaintext.toCharArray())
                SecureMemory.clear(noteKey.toCharArray())
            }
        }

        // Unlock key dialog for existing notes
        if (showKeyDialog) {
            val dialogKeyboardState = remember { KeyboardState() }
            AlertDialog(
                onDismissRequest = { onBack() },
                title = { Text("Enter Note Key") },
                text = {
                    Column {
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = noteKey,
                                onValueChange = { noteKey = it; keyError = null },
                                label = "Encryption key for this note",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (dialogKeyboardState.isVisible) {
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomKeyboard(
                                onKeyPress = { dialogKeyboardState.onKeyPress?.invoke(it) },
                                onDelete = { dialogKeyboardState.onDelete?.invoke() },
                                onDone = { dialogKeyboardState.onDone?.invoke() }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (noteKey.isEmpty()) {
                            keyError = "Key is required"
                            return@Button
                        }
                        scope.launch {
                            val entity = noteEntity
                            if (entity != null) {
                                val result = repository.decryptNoteContent(
                                    entity, noteKey.toCharArray()
                                )
                                if (result.isSuccess) {
                                    val decrypted = result.getOrNull()!!
                                    plaintext = String(decrypted, Charsets.UTF_8)
                                    title = entity.title
                                    isLocked = false
                                    showKeyDialog = false
                                    SecureMemory.clear(noteKey.toCharArray())
                                    noteKey = ""
                                } else {
                                    SecureMemory.clear(noteKey.toCharArray())
                                    noteKey = ""
                                    keyError = "Wrong key"
                                }
                            }
                        }
                    }) { Text("Unlock") }
                },
                dismissButton = {
                    TextButton(onClick = onBack) { Text("Cancel") }
                }
            )
            if (keyError != null) {
                Text(keyError!!, color = MaterialTheme.colorScheme.error)
            }
        }

        // Create key dialog for new notes
        if (showCreateKeyDialog && isNewNote && !showKeyDialog) {
            var newKey by remember { mutableStateOf(generatedKey) }
            var confirmKey by remember { mutableStateOf("") }
            var createKeyError by remember { mutableStateOf<String?>(null) }
            val dialogKeyboardState = remember { KeyboardState() }
            val MIN_KEY_LEN = 8

            AlertDialog(
                onDismissRequest = { onBack() },
                title = { Text("Set Encryption Key") },
                text = {
                    Column {
                        Text(
                            "Minimum $MIN_KEY_LEN characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (newKey.length in 1 until MIN_KEY_LEN)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = newKey,
                                onValueChange = { newKey = it; createKeyError = null },
                                label = "Note encryption key",
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            PasswordField(
                                value = confirmKey,
                                onValueChange = { confirmKey = it; createKeyError = null },
                                label = "Confirm key",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (dialogKeyboardState.isVisible) {
                            Spacer(modifier = Modifier.height(4.dp))
                            CustomKeyboard(
                                onKeyPress = { dialogKeyboardState.onKeyPress?.invoke(it) },
                                onDelete = { dialogKeyboardState.onDelete?.invoke() },
                                onDone = { dialogKeyboardState.onDone?.invoke() }
                            )
                        }
                        if (createKeyError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(createKeyError!!, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                generatedKey = generateRandomKey(Constants.RECOMMENDED_NOTE_KEY_LENGTH)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Generate Secure Key")
                        }
                        if (generatedKey.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Generated: $generatedKey",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                newKey.isEmpty() -> createKeyError = "Key is required"
                                newKey.length < MIN_KEY_LEN -> createKeyError = "Key must be at least $MIN_KEY_LEN characters"
                                newKey != confirmKey -> createKeyError = "Keys do not match"
                                else -> {
                                    noteKey = newKey
                                    showCreateKeyDialog = false
                                    isLocked = false
                                    title = ""
                                    plaintext = ""
                                }
                            }
                        },
                        colors = if (newKey.length < MIN_KEY_LEN && newKey.isNotEmpty())
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        else ButtonDefaults.buttonColors()
                    ) {
                        Text("Create Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) { Text("Cancel") }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isNewNote) "New Note" else title.ifEmpty { "Note" },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (hasUnsavedChanges) {
                                showUnsavedDialog = true
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!isLocked) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val plainBytes = plaintext.toByteArray(Charsets.UTF_8)
                                        val result = repository.encryptNoteContent(
                                            noteId, title, plainBytes, noteKey.toCharArray()
                                        )
                                        if (result.isSuccess) {
                                            val note = result.getOrNull()!!
                                            val b64 = Base64.getEncoder()
                                            encryptedOutput = "${b64.encodeToString(note.salt)}::${b64.encodeToString(note.iv)}::${b64.encodeToString(note.tag)}::${b64.encodeToString(note.ciphertext)}"
                                            showEncryptedOutput = true
                                            SecureMemory.clear(plaintext.toCharArray())
                                            plaintext = ""
                                            isLocked = true
                                            Toast.makeText(context, "\uD83D\uDD12 Encrypted", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.height(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Encrypt", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            if (isLocked) {
                if (showEncryptedOutput && encryptedOutput.isNotEmpty()) {
                    DecoyEncryptedView(
                        encryptedBlob = encryptedOutput,
                        padding = padding,
                        onEdit = { showEditKeyDialog = true },
                        onBack = onBack
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.height(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("\uD83D\uDD12 Encrypted", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Enter the note key to decrypt", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showKeyDialog = true }) {
                                Text("Unlock")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    AppTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Title",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppTextField(
                        value = plaintext,
                        onValueChange = { plaintext = it },
                        label = "Note content",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        singleLine = false,
                        minLines = 1
                    )
                }
            }
        }
    } // KeyboardHost
}

// ── Decoy encrypted view (Copy / Edit / Disguise dropdown) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecoyEncryptedView(
    encryptedBlob: String,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(DecoyPlatform.YOUTUBE) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val disguisedUrl = remember(encryptedBlob, selectedPlatform) {
        DecoyEncoder.encode(encryptedBlob, selectedPlatform)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(
            "Encrypted Message:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = disguisedUrl,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
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

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(disguisedUrl))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Disguise as",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedPlatform.label,
                onValueChange = {},
                readOnly = true,
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
