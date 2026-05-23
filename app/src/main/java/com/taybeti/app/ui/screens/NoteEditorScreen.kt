package com.taybeti.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.taybeti.app.data.entities.NoteEntity
import com.taybeti.app.data.repository.NoteRepository
import com.taybeti.app.security.AttachmentManager
import com.taybeti.app.security.AttachmentManager.getAttachmentsList
import com.taybeti.app.security.AttachmentManager.attachmentsToJson
import com.taybeti.app.security.NoteAttachment
import com.taybeti.app.security.SecureMemory
import com.taybeti.app.ui.components.AppTextField
import com.taybeti.app.ui.components.CustomKeyboard
import com.taybeti.app.ui.components.KeyboardHost
import com.taybeti.app.ui.components.KeyboardState
import com.taybeti.app.ui.components.LocalKeyboardState
import com.taybeti.app.ui.components.NoteFormattingToolbar
import com.taybeti.app.ui.components.PasswordField
import com.taybeti.app.ui.components.AttachmentType
import com.taybeti.app.ui.components.toMimePattern
import com.taybeti.app.util.Constants
import com.taybeti.app.util.DecoyEncoder
import com.taybeti.app.util.DecoyPlatform
import com.taybeti.app.util.LocalStrings
import com.taybeti.app.util.generateRandomKey
import kotlinx.coroutines.launch
import java.io.File
import java.util.Base64

data class EditorImage(
    val attachment: NoteAttachment,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 200f,
    var height: Float = 200f,
    var layer: ImageLayer = ImageLayer.INLINE,
    var isSelected: Boolean = false,
    val pageIndex: Int = 0
)

enum class ImageLayer {
    INLINE,
    BEHIND_TEXT,
    IN_FRONT_OF_TEXT,
    INTEGRATED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    repository: NoteRepository,
    isDecoy: Boolean,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
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

    val images = remember { mutableStateListOf<EditorImage>() }
    var pendingAttachmentType by remember { mutableStateOf<AttachmentType?>(null) }
    var showEncryptDialog by remember { mutableStateOf(false) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf<String?>(null) }
    var pageTheme by remember { mutableStateOf("dark") }
    val pages = remember { mutableStateListOf<String>("") }
    var currentPageIndex by remember { mutableStateOf(0) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val noteJson = buildNoteJson(pages, images)
                    val plainBytes = noteJson.toByteArray(Charsets.UTF_8)
                    val result = repository.encryptNoteContent(
                        noteId, title, plainBytes, noteKey.toCharArray(), ""
                    )
                    if (result.isSuccess) {
                        val note = result.getOrNull()!!
                        val json = org.json.JSONObject()
                        json.put("title", title)
                        json.put("salt", Base64.getEncoder().encodeToString(note.salt))
                        json.put("iv", Base64.getEncoder().encodeToString(note.iv))
                        json.put("tag", Base64.getEncoder().encodeToString(note.tag))
                        json.put("ciphertext", Base64.getEncoder().encodeToString(note.ciphertext))
                        val outputBytes = json.toString().toByteArray(Charsets.UTF_8)
                        context.contentResolver.openOutputStream(uri)?.use { it.write(outputBytes) }
                        SecureMemory.clear(plaintext.toCharArray())
                        plaintext = ""
                        images.clear()
                        isLocked = true
                        Toast.makeText(context, "\uD83D\uDD12 Note encrypted and saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Encryption failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val currentType = pendingAttachmentType
        pendingAttachmentType = null
        if (uri != null && currentType != null) {
            scope.launch {
                val result = AttachmentManager.copyAttachment(
                    context, noteId, uri, context.contentResolver
                )
                result.onSuccess { attachment ->
                    val alreadyExists = images.any { it.attachment.id == attachment.id }
                    if (!alreadyExists) {
                        images.add(
                            EditorImage(
                                attachment = attachment,
                                x = 0f,
                                y = 0f,
                                width = 200f,
                                height = 200f,
                                layer = ImageLayer.IN_FRONT_OF_TEXT,
                                pageIndex = currentPageIndex
                            )
                        )
                    }
                }.onFailure {
                    Toast.makeText(context, "Failed to attach file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val hasUnsavedChanges = !isLocked && (title.isNotEmpty() || plaintext.isNotEmpty() || images.isNotEmpty())

    fun attachKeyboardForTitle(kb: KeyboardState) {
        kb.attach(
            onKey = { char ->
                val toInsert = if (char == 'P' || char == 'p') "PPPPPPPP" else char.toString()
                title += toInsert
            },
            onDel = { if (title.isNotEmpty()) title = title.dropLast(1) },
            onDone = { kb.detach(); activeField = null }
        )
    }

    fun attachKeyboardForContent(kb: KeyboardState) {
        kb.attach(
            onKey = { char ->
                val toInsert = if (char == 'P' || char == 'p') "PPPPPPPP" else char.toString()
                if (currentPageIndex < pages.size) {
                    pages[currentPageIndex] = pages[currentPageIndex] + toInsert
                }
            },
            onDel = {
                if (currentPageIndex < pages.size && pages[currentPageIndex].isNotEmpty()) {
                    pages[currentPageIndex] = pages[currentPageIndex].dropLast(1)
                }
            },
            onDone = { kb.detach(); activeField = null }
        )
    }

    KeyboardHost {
        val kb = LocalKeyboardState.current
        LaunchedEffect(activeField, kb) {
            if (kb != null) {
                if (activeField == "title") attachKeyboardForTitle(kb)
                else if (activeField == "content") attachKeyboardForContent(kb)
            }
        }

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
                title = { Text(strings.discardNoteTitle) },
                text = { Text(strings.discardNoteMsg) },
                confirmButton = {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(strings.stay)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        SecureMemory.clear(plaintext.toCharArray())
                        plaintext = ""
                        images.clear()
                        onBack()
                    }) {
                        Text(strings.discard, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        if (showEditKeyDialog) {
            val dialogKeyboardState = remember { KeyboardState() }
            Dialog(
                onDismissRequest = {
                    showEditKeyDialog = false
                    editKeyError = null
                    editKey = ""
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.enterNoteKey,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(strings.reEnterKeyToUnlock)
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = editKey,
                                onValueChange = { editKey = it; editKeyError = null },
                                label = strings.noteKey,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (editKeyError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(editKeyError!!, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    showEditKeyDialog = false
                                    editKeyError = null
                                    editKey = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.cancel) }
                            Button(
                                onClick = {
                                    if (editKey.isEmpty()) {
                                        editKeyError = strings.keyRequired
                                        return@Button
                                    }
                                    scope.launch {
                                        val entity = noteEntity
                                        if (entity != null) {
                                            val result = repository.decryptNoteContent(entity, editKey.toCharArray())
                                            if (result.isSuccess) {
                                                val decrypted = result.getOrNull()!!
                                                val decryptedStr = String(decrypted, Charsets.UTF_8)
                                                val (loadedPages, attJson) = parseNoteJson(decryptedStr)
                                                pages.clear()
                                                pages.addAll(loadedPages)
                                                images.clear()
                                                val atts = getAttachmentsList(attJson, context, noteId)
                                                atts.forEach { att ->
                                                    images.add(
                                                        EditorImage(
                                                            attachment = att,
                                                            x = att.metadata["x"]?.toFloatOrNull() ?: 0f,
                                                            y = att.metadata["y"]?.toFloatOrNull() ?: 0f,
                                                            width = att.metadata["width"]?.toFloatOrNull() ?: 200f,
                                                            height = att.metadata["height"]?.toFloatOrNull() ?: 200f,
                                                            layer = try {
                                                                ImageLayer.valueOf(att.metadata["layer"] ?: "IN_FRONT_OF_TEXT")
                                                            } catch (_: Exception) {
                                                                ImageLayer.IN_FRONT_OF_TEXT
                                                            },
                                                            pageIndex = att.metadata["pageIndex"]?.toIntOrNull() ?: 0
                                                        )
                                                    )
                                                }
                                                title = entity.title
                                                showEditKeyDialog = false
                                                showEncryptedOutput = false
                                                encryptedOutput = ""
                                                isLocked = false
                                                SecureMemory.clear(editKey.toCharArray())
                                                editKey = ""
                                                editKeyError = null
                                            } else {
                                                editKeyError = strings.wrongKey
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.unlock) }
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
                }
            }
        }

        LaunchedEffect(noteId) {
            scope.launch {
                val db = com.taybeti.app.data.database.AppDatabase.getInstance(context)
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

        if (showKeyDialog) {
            val dialogKeyboardState = remember { KeyboardState() }
            Dialog(
                onDismissRequest = { /* block outside dismiss — user must use Cancel */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.enterNoteKey,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (keyError != null) {
                            Text(keyError!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        CompositionLocalProvider(LocalKeyboardState provides dialogKeyboardState) {
                            PasswordField(
                                value = noteKey,
                                onValueChange = { noteKey = it; keyError = null },
                                label = strings.noteKey,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onBack,
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.cancel) }
                            Button(
                                onClick = {
                                    if (noteKey.isEmpty()) {
                                        keyError = strings.keyRequired
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
                                                val decryptedStr = String(decrypted, Charsets.UTF_8)
                                                val (loadedPages, attJson) = parseNoteJson(decryptedStr)
                                                pages.clear()
                                                pages.addAll(loadedPages)
                                                images.clear()
                                                val atts = getAttachmentsList(attJson, context, noteId)
                                                atts.forEach { att ->
                                                    images.add(
                                                        EditorImage(
                                                            attachment = att,
                                                            x = att.metadata["x"]?.toFloatOrNull() ?: 0f,
                                                            y = att.metadata["y"]?.toFloatOrNull() ?: 0f,
                                                            width = att.metadata["width"]?.toFloatOrNull() ?: 200f,
                                                            height = att.metadata["height"]?.toFloatOrNull() ?: 200f,
                                                            layer = try {
                                                                ImageLayer.valueOf(att.metadata["layer"] ?: "IN_FRONT_OF_TEXT")
                                                            } catch (_: Exception) {
                                                                ImageLayer.IN_FRONT_OF_TEXT
                                                            },
                                                            pageIndex = att.metadata["pageIndex"]?.toIntOrNull() ?: 0
                                                        )
                                                    )
                                                }
                                                title = entity.title
                                                isLocked = false
                                                showKeyDialog = false
                                                SecureMemory.clear(noteKey.toCharArray())
                                                noteKey = ""
                                            } else {
                                                SecureMemory.clear(noteKey.toCharArray())
                                                noteKey = ""
                                                keyError = strings.wrongKey
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.unlock) }
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
                }
            }
        }

        if (showCreateKeyDialog && isNewNote && !showKeyDialog) {
            var newKey by remember { mutableStateOf(generatedKey) }
            var confirmKey by remember { mutableStateOf("") }
            var createKeyError by remember { mutableStateOf<String?>(null) }
            val dialogKb = remember { KeyboardState() }
            val MIN_KEY_LEN = 8

            Dialog(
                onDismissRequest = { /* block outside dismiss */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.createKey,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            strings.minChars,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (newKey.length in 1 until MIN_KEY_LEN)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides dialogKb) {
                            PasswordField(
                                value = newKey,
                                onValueChange = { newKey = it; createKeyError = null },
                                label = strings.noteKey,
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
                        if (dialogKb.isVisible) {
                            Spacer(modifier = Modifier.weight(1f))
                            CustomKeyboard(
                                onKeyPress = { dialogKb.onKeyPress?.invoke(it) },
                                onDelete = { dialogKb.onDelete?.invoke() },
                                onDone = { dialogKb.onDone?.invoke() }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
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
                            Text(strings.generateKey)
                        }
                        if (generatedKey.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Generated: $generatedKey",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = onBack,
                                modifier = Modifier.weight(1f)
                            ) { Text(strings.cancel) }
                            Button(
                                onClick = {
                                    when {
                                        newKey.isEmpty() -> createKeyError = strings.keyRequired
                                        newKey.length < MIN_KEY_LEN -> createKeyError = strings.keyTooShort
                                        newKey != confirmKey -> createKeyError = strings.keysMatch
                                        else -> {
                                            noteKey = newKey
                                            showCreateKeyDialog = false
                                            isLocked = false
                                            title = ""
                                            pages.clear()
                                            pages.add("")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = if (newKey.length < MIN_KEY_LEN && newKey.isNotEmpty())
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                else ButtonDefaults.buttonColors()
                            ) {
                                Text(strings.createNote)
                            }
                        }
                    }
                }
            }
        }

        if (showEncryptDialog) {
            AlertDialog(
                onDismissRequest = { showEncryptDialog = false },
                title = { Text("Encrypt Note", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Choose how to encrypt this note with ${images.size} image(s):")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                showEncryptDialog = false
                                scope.launch {
                    val noteJson = buildNoteJson(pages, images)
                                    val plainBytes = noteJson.toByteArray(Charsets.UTF_8)
                                    val result = repository.encryptNoteContent(
                                        noteId, title, plainBytes, noteKey.toCharArray(), ""
                                    )
                                    if (result.isSuccess) {
                                        val note = result.getOrNull()!!
                                        val b64 = Base64.getEncoder()
                                        encryptedOutput = "${b64.encodeToString(note.salt)}::${b64.encodeToString(note.iv)}::${b64.encodeToString(note.tag)}::${b64.encodeToString(note.ciphertext)}"
                                        showEncryptedOutput = true
                        SecureMemory.clear(plaintext.toCharArray())
                        plaintext = ""
                        pages.clear()
                        pages.add("")
                                        images.clear()
                                        isLocked = true
                                        Toast.makeText(context, "\uD83D\uDD12 ${strings.encryptedNote}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Shield, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Encrypt as Blob", fontWeight = FontWeight.Bold)
                                Text("Single encrypted text blob (copy/share)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                saveFileLauncher.launch("${title.ifEmpty { "note" }}.taybeti")
                                showEncryptDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Encrypt as File", fontWeight = FontWeight.Bold)
                                Text("Save encrypted note as .taybeti file", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEncryptDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isNewNote) strings.newNote else title.ifEmpty { "Note" },
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
                            Icon(Icons.Filled.ArrowBack, contentDescription = strings.back)
                        }
                    },
                    actions = {
                        if (!isLocked) {
                            Button(
                                onClick = { showEncryptDialog = true },
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
                                Text(strings.encrypt, fontWeight = FontWeight.Bold)
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
                            Text(strings.encryptedNote, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(strings.enterKeyToDecrypt, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showKeyDialog = true }) {
                                Text(strings.unlock)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // Title field with shared keyboard
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeField = "title" }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title.ifEmpty { "Title" },
                            style = MaterialTheme.typography.titleMedium,
                            color = if (title.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    NoteFormattingToolbar(
                        onInsertFormat = { format ->
                            if (currentPageIndex < pages.size) {
                                pages[currentPageIndex] = pages[currentPageIndex] + format
                            }
                        },
                        onAddAttachment = { type ->
                            pendingAttachmentType = type
                            attachmentLauncher.launch(type.toMimePattern())
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Theme toggle - both icons side by side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { pageTheme = "light" },
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (pageTheme == "light") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Light mode",
                                tint = if (pageTheme == "light") Color(0xFFFFB300) else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { pageTheme = "dark" },
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (pageTheme == "dark") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Dark mode",
                                tint = if (pageTheme == "dark") Color(0xFF90CAF9) else Color.Gray
                            )
                        }
                    }

                    WordEditorCanvas(
                        pages = pages,
                        images = images,
                        currentPageIndex = currentPageIndex,
                        onPageSelect = { currentPageIndex = it },
                        onImageSelect = { id ->
                            selectedImageId = id
                            showImageOptions = true
                        },
                        onImageUpdate = { updatedImage ->
                            val idx = images.indexOfFirst { it.attachment.id == updatedImage.attachment.id }
                            if (idx >= 0) {
                                images[idx] = updatedImage
                            }
                        },
                        onImageDelete = { id ->
                            val img = images.find { it.attachment.id == id }
                            if (img != null) {
                                scope.launch {
                                    AttachmentManager.deleteAttachment(context, noteId, img.attachment)
                                }
                                images.remove(img)
                            }
                        },
                        activeField = activeField,
                        onFieldActivate = { activeField = it },
                        onFieldDeactivate = { activeField = null },
                        pageTheme = pageTheme
                    )
                }
            }
        }
    } // KeyboardHost
}

@Composable
private fun WordEditorCanvas(
    pages: MutableList<String>,
    images: List<EditorImage>,
    currentPageIndex: Int,
    onPageSelect: (Int) -> Unit,
    onImageSelect: (String) -> Unit,
    onImageUpdate: (EditorImage) -> Unit,
    onImageDelete: (String) -> Unit,
    activeField: String?,
    onFieldActivate: (String) -> Unit,
    onFieldDeactivate: () -> Unit,
    pageTheme: String
) {
    val kbState = LocalKeyboardState.current
    val scrollState = rememberScrollState()
    val isDark = pageTheme == "dark"
    val pageBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val pageBorder = if (isDark) Color(0xFF444444) else Color.LightGray.copy(alpha = 0.3f)
    val canvasBg = if (isDark) Color(0xFF121212) else Color(0xFFF0F0F0)
    val textColor = if (isDark) Color.White else Color.Black
    val placeholderColor = Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp)
        ) {
            pages.forEachIndexed { index, pageText ->
                PageBlock(
                    text = pageText,
                    onTextChange = { pages[index] = it },
                    images = images,
                    pageIndex = index,
                    isSelected = index == currentPageIndex,
                    onPageSelect = { onPageSelect(index); onFieldActivate("content") },
                    onImageSelect = onImageSelect,
                    onImageUpdate = onImageUpdate,
                    onImageDelete = onImageDelete,
                    pageNumber = index + 1,
                    pageBg = pageBg,
                    pageBorder = pageBorder,
                    isDark = isDark,
                    textColor = textColor,
                    placeholderColor = placeholderColor
                )
            }

            // Add page button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .background(pageBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { pages.add("") }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Page", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Add new page", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Tap to insert a blank page below", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Scrollbar
        if (scrollState.maxValue > 0) {
            val scrollProgress = scrollState.value.toFloat() / scrollState.maxValue
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .width(6.dp)
                    .height(60.dp)
                    .offset(y = (scrollProgress * (scrollState.maxValue - 60)).dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun PageBlock(
    text: String,
    onTextChange: (String) -> Unit,
    images: List<EditorImage>,
    pageIndex: Int,
    isSelected: Boolean,
    onPageSelect: () -> Unit,
    onImageSelect: (String) -> Unit,
    onImageUpdate: (EditorImage) -> Unit,
    onImageDelete: (String) -> Unit,
    pageNumber: Int,
    pageBg: Color,
    pageBorder: Color,
    isDark: Boolean,
    textColor: Color,
    placeholderColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(842.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(pageBg, RoundedCornerShape(2.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else pageBorder,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onPageSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Text(
                text = text.ifEmpty { "Start typing here..." },
                color = if (text.isEmpty()) placeholderColor else textColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        images.filter { it.layer == ImageLayer.BEHIND_TEXT && it.pageIndex == pageNumber - 1 }.forEach { img ->
            DraggableImage(
                image = img,
                onSelect = onImageSelect,
                onUpdate = onImageUpdate,
                onDelete = onImageDelete
            )
        }

        val integratedImages = images.filter { it.layer == ImageLayer.INTEGRATED && it.pageIndex == pageNumber - 1 }
        if (integratedImages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp)
            ) {
                Text(
                    text = text.ifEmpty { "Start typing here..." },
                    color = if (text.isEmpty()) placeholderColor else textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(48.dp)) {
                val primaryImage = integratedImages.first()
                val imageTop = primaryImage.y
                val imageBottom = primaryImage.y + primaryImage.height
                val imageLeft = primaryImage.x
                val imageRight = primaryImage.x + primaryImage.width
                val lineHeight = 24f
                val linesAbove = (imageTop / lineHeight).toInt().coerceAtLeast(0)
                val linesThrough = ((imageBottom - imageTop) / lineHeight).toInt().coerceAtLeast(1)
                val charsPerLine = 35
                val charsAbove = (linesAbove * charsPerLine).coerceAtMost(text.length)
                val textAbove = text.take(charsAbove)
                val textRemaining = text.drop(charsAbove)
                val charsThrough = (linesThrough * charsPerLine).coerceAtMost(textRemaining.length)
                val textThrough = textRemaining.take(charsThrough)
                val textAfter = textRemaining.drop(charsThrough)
                Column(modifier = Modifier.fillMaxSize()) {
                    if (textAbove.isNotEmpty()) {
                        Text(
                            text = textAbove,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (textThrough.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val leftWidth = (imageLeft - 48f).coerceAtLeast(0f)
                            Text(
                                text = textThrough,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.width(leftWidth.dp)
                            )
                            Spacer(modifier = Modifier.width(primaryImage.width.dp))
                        }
                    }
                    if (textAfter.isNotEmpty()) {
                        Text(
                            text = textAfter,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (text.isEmpty()) {
                        Text(
                            text = "Start typing here...",
                            color = placeholderColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                DraggableImage(
                    image = primaryImage,
                    onSelect = onImageSelect,
                    onUpdate = onImageUpdate,
                    onDelete = onImageDelete
                )
                integratedImages.drop(1).forEach { img ->
                    DraggableImage(
                        image = img,
                        onSelect = onImageSelect,
                        onUpdate = onImageUpdate,
                        onDelete = onImageDelete
                    )
                }
            }
        }

        images.filter { it.layer == ImageLayer.IN_FRONT_OF_TEXT && it.pageIndex == pageNumber - 1 }.forEach { img ->
            DraggableImage(
                image = img,
                onSelect = onImageSelect,
                onUpdate = onImageUpdate,
                onDelete = onImageDelete
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Page $pageNumber",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DraggableImage(
    image: EditorImage,
    onSelect: (String) -> Unit,
    onUpdate: (EditorImage) -> Unit,
    onDelete: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(image.x) }
    var offsetY by remember { mutableStateOf(image.y) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .size(width = image.width.dp, height = image.height.dp)
            .clickable { onSelect(image.attachment.id) }
            .border(
                width = if (image.isSelected) 2.dp else 0.dp,
                color = if (image.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .pointerInput(image.attachment.id) {
                detectDragGestures(
                    onDragStart = {
                        onSelect(image.attachment.id)
                        if (!image.isSelected) {
                            onUpdate(image.copy(isSelected = true))
                        }
                    },
                    onDragEnd = {
                        onUpdate(image.copy(x = offsetX, y = offsetY, isSelected = false))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onUpdate(
                            image.copy(
                                x = offsetX,
                                y = offsetY,
                                isSelected = true
                            )
                        )
                    }
                )
            }
    ) {
        AsyncImage(
            model = image.attachment.storedPath,
            contentDescription = image.attachment.originalName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Fit
        )

        if (image.isSelected) {
            ResizeHandle(
                alignment = Alignment.BottomEnd,
                onResize = { dx, dy ->
                    onUpdate(
                        image.copy(
                            width = (image.width + dx).coerceAtLeast(50f),
                            height = (image.height + dy).coerceAtLeast(50f)
                        )
                    )
                }
            )
            ResizeHandle(
                alignment = Alignment.BottomStart,
                onResize = { dx, dy ->
                    onUpdate(
                        image.copy(
                            width = (image.width - dx).coerceAtLeast(50f),
                            height = (image.height + dy).coerceAtLeast(50f)
                        )
                    )
                }
            )
            ResizeHandle(
                alignment = Alignment.TopEnd,
                onResize = { dx, dy ->
                    onUpdate(
                        image.copy(
                            width = (image.width + dx).coerceAtLeast(50f),
                            height = (image.height - dy).coerceAtLeast(50f)
                        )
                    )
                }
            )
            ResizeHandle(
                alignment = Alignment.TopStart,
                onResize = { dx, dy ->
                    onUpdate(
                        image.copy(
                            width = (image.width - dx).coerceAtLeast(50f),
                            height = (image.height - dy).coerceAtLeast(50f)
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun ResizeHandle(
    alignment: Alignment,
    onResize: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            .clickable { }
    )
}

@Composable
private fun ImageOptionsDialog(
    image: EditorImage,
    onDismiss: () -> Unit,
    onUpdate: (EditorImage) -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image Options", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                AsyncImage(
                    model = image.attachment.storedPath,
                    contentDescription = image.attachment.originalName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Layer:", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))

                ImageLayer.entries.forEach { layer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(image.copy(layer = layer))
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (layer) {
                                ImageLayer.INLINE -> "Inline with text"
                                ImageLayer.BEHIND_TEXT -> "Behind text (background)"
                                ImageLayer.IN_FRONT_OF_TEXT -> "In front of text"
                                ImageLayer.INTEGRATED -> "Integrated (text flows around)"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (image.layer == layer) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        onUpdate(image.copy(
                            width = (image.width * 0.8f).coerceAtLeast(50f),
                            height = (image.height * 0.8f).coerceAtLeast(50f)
                        ))
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Shrink")
                        Text("Smaller")
                    }
                    TextButton(onClick = {
                        onUpdate(image.copy(
                            width = (image.width * 1.2f),
                            height = (image.height * 1.2f)
                        ))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Enlarge")
                        Text("Larger")
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    onDelete()
                    onDismiss()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    )
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
    var useDisguise by remember { mutableStateOf(false) }
    var showDisguise by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val strings = LocalStrings.current

    val disguisedUrl = remember(encryptedBlob, selectedPlatform, useDisguise) {
        if (useDisguise) DecoyEncoder.encode(encryptedBlob, selectedPlatform)
        else encryptedBlob
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            strings.encryptedMessage,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = encryptedBlob,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            label = { Text(strings.encryptedBlob) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(encryptedBlob))
                    Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = strings.copy)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(encryptedBlob))
                    Toast.makeText(context, strings.copied, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.copy)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, encryptedBlob)
                        putExtra(Intent.EXTRA_SUBJECT, "Encrypted Note")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Encrypted Note"))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.edit)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!showDisguise) {
            Button(
                onClick = { showDisguise = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.disguiseAsLink)
            }
        } else {
            Text(
                strings.disguiseAs,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
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
}

private fun buildNoteJson(pages: List<String>, images: List<EditorImage>): String {
    val json = org.json.JSONObject()
    val pagesArray = org.json.JSONArray()
    pages.forEach { pagesArray.put(it) }
    json.put("pages", pagesArray)
    
    val imagesArray = org.json.JSONArray()
    images.forEach { img ->
        val imgObj = org.json.JSONObject()
        imgObj.put("id", img.attachment.id)
        imgObj.put("originalName", img.attachment.originalName)
        imgObj.put("mimeType", img.attachment.mimeType)
        imgObj.put("size", img.attachment.size)
        imgObj.put("storedPath", img.attachment.storedPath)
        imgObj.put("type", img.attachment.type.name)
        imgObj.put("x", img.x)
        imgObj.put("y", img.y)
        imgObj.put("width", img.width)
        imgObj.put("height", img.height)
        imgObj.put("layer", img.layer.name)
        imgObj.put("pageIndex", img.pageIndex)
        imagesArray.put(imgObj)
    }
    
    json.put("images", imagesArray)
    return json.toString()
}

private fun parseNoteJson(plaintext: String): Pair<List<String>, String> {
    return try {
        val json = org.json.JSONObject(plaintext)
        if (json.has("images")) {
            val pagesList = mutableListOf<String>()
            if (json.has("pages")) {
                val pagesArray = json.getJSONArray("pages")
                for (i in 0 until pagesArray.length()) {
                    pagesList.add(pagesArray.getString(i))
                }
            } else {
                pagesList.add(json.optString("content", ""))
            }
            val imagesArray = json.getJSONArray("images")
            val attachmentsList = mutableListOf<NoteAttachment>()
            
            for (i in 0 until imagesArray.length()) {
                val imgObj = imagesArray.getJSONObject(i)
                val att = NoteAttachment(
                    id = imgObj.optString("id", java.util.UUID.randomUUID().toString()),
                    originalName = imgObj.optString("originalName", "image"),
                    mimeType = imgObj.optString("mimeType", "image/jpeg"),
                    size = imgObj.optLong("size", 0),
                    storedPath = imgObj.optString("storedPath", ""),
                    type = try {
                        NoteAttachment.AttachmentType.valueOf(imgObj.optString("type", "IMAGE"))
                    } catch (_: Exception) {
                        NoteAttachment.AttachmentType.IMAGE
                    },
                    isIntegrated = imgObj.optBoolean("isIntegrated", false),
                    encryptedPath = imgObj.optString("encryptedPath", ""),
                    metadata = mapOf(
                        "x" to imgObj.optDouble("x", 0.0).toString(),
                        "y" to imgObj.optDouble("y", 0.0).toString(),
                        "width" to imgObj.optDouble("width", 200.0).toString(),
                        "height" to imgObj.optDouble("height", 200.0).toString(),
                        "layer" to imgObj.optString("layer", "IN_FRONT_OF_TEXT"),
                        "pageIndex" to imgObj.optInt("pageIndex", 0).toString()
                    )
                )
                attachmentsList.add(att)
            }
            
            pagesList to attachmentsToJson(attachmentsList, embedFiles = true)
        } else {
            val content = json.optString("content", plaintext)
            val attachments = json.optJSONArray("attachments")?.toString() ?: "[]"
            listOf(content) to attachments
        }
    } catch (_: Exception) {
        listOf(plaintext) to "[]"
    }
}
