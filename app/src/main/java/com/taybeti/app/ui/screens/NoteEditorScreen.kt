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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.layout.layout
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

enum class ImagePosition {
    INLINE,
    BEHIND_TEXT,
    IN_FRONT_OF_TEXT
}

sealed class ContentBlock {
    data class TextBlock(val content: String) : ContentBlock()
    data class ImageBlock(
        val attachment: NoteAttachment,
        val position: ImagePosition = ImagePosition.INLINE,
        val widthFraction: Float = 0.8f
    ) : ContentBlock()
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

    val blocks = remember { mutableStateListOf<ContentBlock>() }
    var pendingAttachmentType by remember { mutableStateOf<AttachmentType?>(null) }
    var showEncryptAttachmentsDialog by remember { mutableStateOf(false) }
    var showImageEditor by remember { mutableStateOf<Pair<Int, NoteAttachment>?>(null) }

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && pendingAttachmentType != null) {
            scope.launch {
                val result = AttachmentManager.copyAttachment(
                    context, noteId, uri, context.contentResolver
                )
                result.onSuccess { attachment ->
                    blocks.add(
                        ContentBlock.ImageBlock(
                            attachment = attachment,
                            position = ImagePosition.INLINE
                        )
                    )
                }.onFailure {
                    Toast.makeText(context, "Failed to attach file", Toast.LENGTH_SHORT).show()
                }
            }
        }
        pendingAttachmentType = null
    }

    val hasUnsavedChanges = !isLocked && (title.isNotEmpty() || blocks.any { it is ContentBlock.TextBlock && (it as ContentBlock.TextBlock).content.isNotEmpty() })

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
                        blocks.filterIsInstance<ContentBlock.ImageBlock>().forEach {
                            SecureMemory.clear(it.attachment.storedPath.toCharArray())
                        }
                        blocks.clear()
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
                                                val (content, attJson) = parseNoteJson(decryptedStr)
                                                blocks.clear()
                                                blocks.add(ContentBlock.TextBlock(content))
                                                val atts = getAttachmentsList(attJson, context, noteId)
                                                atts.forEach { att ->
                                                    blocks.add(
                                                        ContentBlock.ImageBlock(
                                                            attachment = att,
                                                            position = ImagePosition.INLINE
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
                blocks.filterIsInstance<ContentBlock.TextBlock>().forEach {
                    SecureMemory.clear(it.content.toCharArray())
                }
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
                                                val (content, attJson) = parseNoteJson(decryptedStr)
                                                blocks.clear()
                                                blocks.add(ContentBlock.TextBlock(content))
                                                val atts = getAttachmentsList(attJson, context, noteId)
                                                atts.forEach { att ->
                                                    blocks.add(
                                                        ContentBlock.ImageBlock(
                                                            attachment = att,
                                                            position = ImagePosition.INLINE
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
                                            blocks.clear()
                                            blocks.add(ContentBlock.TextBlock(""))
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

        if (showEncryptAttachmentsDialog) {
            EncryptAttachmentsDialog(
                attachments = blocks.filterIsInstance<ContentBlock.ImageBlock>().map { it.attachment },
                onDismiss = { showEncryptAttachmentsDialog = false },
                onEncrypt = { encryptAttachmentsTogether ->
                    showEncryptAttachmentsDialog = false
                    scope.launch {
                        val noteJson = buildNoteJsonFromBlocks(blocks)
                        val plainBytes = noteJson.toByteArray(Charsets.UTF_8)
                        val result = repository.encryptNoteContent(
                            noteId, title, plainBytes, noteKey.toCharArray(), ""
                        )
                        if (result.isSuccess) {
                            val note = result.getOrNull()!!
                            val b64 = Base64.getEncoder()
                            encryptedOutput = "${b64.encodeToString(note.salt)}::${b64.encodeToString(note.iv)}::${b64.encodeToString(note.tag)}::${b64.encodeToString(note.ciphertext)}"
                            showEncryptedOutput = true
                            blocks.filterIsInstance<ContentBlock.TextBlock>().forEach {
                                SecureMemory.clear(it.content.toCharArray())
                            }
                            blocks.clear()
                            isLocked = true
                            Toast.makeText(context, "\uD83D\uDD12 ${strings.encryptedNote}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        if (showImageEditor != null) {
            val (blockIndex, attachment) = showImageEditor!!
            ImagePositionDialog(
                attachment = attachment,
                currentPosition = (blocks[blockIndex] as? ContentBlock.ImageBlock)?.position ?: ImagePosition.INLINE,
                onDismiss = { showImageEditor = null },
                onSave = { newPosition, newAttachment ->
                    val existingBlock = blocks[blockIndex] as? ContentBlock.ImageBlock
                    if (existingBlock != null) {
                        blocks[blockIndex] = existingBlock.copy(
                            attachment = newAttachment,
                            position = newPosition
                        )
                    }
                    showImageEditor = null
                },
                onRemove = {
                    scope.launch {
                        AttachmentManager.deleteAttachment(context, noteId, attachment)
                    }
                    blocks.removeAt(blockIndex)
                    showImageEditor = null
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
                            val hasImages = blocks.any { it is ContentBlock.ImageBlock }
                            Button(
                                onClick = {
                                    if (hasImages) {
                                        showEncryptAttachmentsDialog = true
                                    } else {
                                        scope.launch {
                                            val noteJson = buildNoteJsonFromBlocks(blocks)
                                            val plainBytes = noteJson.toByteArray(Charsets.UTF_8)
                                            val result = repository.encryptNoteContent(
                                                noteId, title, plainBytes, noteKey.toCharArray(), ""
                                            )
                                            if (result.isSuccess) {
                                                val note = result.getOrNull()!!
                                                val b64 = Base64.getEncoder()
                                                encryptedOutput = "${b64.encodeToString(note.salt)}::${b64.encodeToString(note.iv)}::${b64.encodeToString(note.tag)}::${b64.encodeToString(note.ciphertext)}"
                                                showEncryptedOutput = true
                                                blocks.filterIsInstance<ContentBlock.TextBlock>().forEach {
                                                    SecureMemory.clear(it.content.toCharArray())
                                                }
                                                blocks.clear()
                                                isLocked = true
                                                Toast.makeText(context, "\uD83D\uDD12 ${strings.encryptedNote}", Toast.LENGTH_SHORT).show()
                                            }
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
                    AppTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Title",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    NoteFormattingToolbar(
                        onInsertFormat = { format ->
                            val textBlockIdx = blocks.indexOfLast { it is ContentBlock.TextBlock }
                            if (textBlockIdx >= 0) {
                                val existing = blocks[textBlockIdx] as ContentBlock.TextBlock
                                blocks[textBlockIdx] = ContentBlock.TextBlock(existing.content + format)
                            }
                        },
                        onAddAttachment = { type ->
                            pendingAttachmentType = type
                            attachmentLauncher.launch(type.toMimePattern())
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(blocks.toList(), key = { 
                            when (it) {
                                is ContentBlock.TextBlock -> "text_${it.content.hashCode()}"
                                is ContentBlock.ImageBlock -> "img_${it.attachment.id}"
                            }
                        }) { block ->
                            when (block) {
                                is ContentBlock.TextBlock -> {
                                    val kbState = remember { KeyboardState() }
                                    Column {
                                        CompositionLocalProvider(LocalKeyboardState provides kbState) {
                                            BasicTextField(
                                                value = block.content,
                                                onValueChange = { newText ->
                                                    val idx = blocks.indexOf(block)
                                                    if (idx >= 0) {
                                                        blocks[idx] = ContentBlock.TextBlock(newText)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp),
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { kbState.attach(
                                                                onKey = { char ->
                                                                    val idx = blocks.indexOf(block)
                                                                    if (idx >= 0) {
                                                                        blocks[idx] = ContentBlock.TextBlock(block.content + char)
                                                                    }
                                                                },
                                                                onDel = {
                                                                    val idx = blocks.indexOf(block)
                                                                    if (idx >= 0 && block.content.isNotEmpty()) {
                                                                        blocks[idx] = ContentBlock.TextBlock(block.content.dropLast(1))
                                                                    }
                                                                },
                                                                onDone = { kbState.detach() }
                                                            ) }
                                                            .padding(8.dp)
                                                    ) {
                                                        if (block.content.isEmpty()) {
                                                            Text(
                                                                "Type here...",
                                                                color = Color.Gray,
                                                                style = MaterialTheme.typography.bodyLarge
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                        }
                                        if (kbState.isVisible) {
                                            CustomKeyboard(
                                                onKeyPress = { char ->
                                                    val idx = blocks.indexOf(block)
                                                    if (idx >= 0) {
                                                        blocks[idx] = ContentBlock.TextBlock(block.content + char)
                                                    }
                                                },
                                                onDelete = {
                                                    val idx = blocks.indexOf(block)
                                                    if (idx >= 0 && block.content.isNotEmpty()) {
                                                        blocks[idx] = ContentBlock.TextBlock(block.content.dropLast(1))
                                                    }
                                                },
                                                onDone = { kbState.detach() }
                                            )
                                        }
                                    }
                                }
                                is ContentBlock.ImageBlock -> {
                                    ImageBlockCard(
                                        block = block,
                                        onEdit = {
                                            val idx = blocks.indexOf(block)
                                            if (idx >= 0) {
                                                showImageEditor = idx to block.attachment
                                            }
                                        },
                                        onRemove = {
                                            scope.launch {
                                                AttachmentManager.deleteAttachment(context, noteId, block.attachment)
                                            }
                                            blocks.remove(block)
                                        },
                                        onMoveUp = {
                                            val idx = blocks.indexOf(block)
                                            if (idx > 0) {
                                                blocks.removeAt(idx)
                                                blocks.add(idx - 1, block)
                                            }
                                        },
                                        onMoveDown = {
                                            val idx = blocks.indexOf(block)
                                            if (idx < blocks.size - 1) {
                                                blocks.removeAt(idx)
                                                blocks.add(idx + 1, block)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } // KeyboardHost
}

@Composable
private fun ImageBlockCard(
    block: ContentBlock.ImageBlock,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    block.attachment.originalName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    when (block.position) {
                        ImagePosition.INLINE -> "Inline"
                        ImagePosition.BEHIND_TEXT -> "Behind Text"
                        ImagePosition.IN_FRONT_OF_TEXT -> "In Front"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (block.position == ImagePosition.INLINE) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = block.attachment.storedPath,
                    contentDescription = block.attachment.originalName,
                    modifier = Modifier
                        .fillMaxWidth(block.widthFraction)
                        .height(200.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = block.attachment.storedPath,
                        contentDescription = block.attachment.originalName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        when (block.position) {
                            ImagePosition.BEHIND_TEXT -> "Will appear behind text"
                            ImagePosition.IN_FRONT_OF_TEXT -> "Will appear over text"
                            else -> ""
                        },
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Move Down", modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ImagePositionDialog(
    attachment: NoteAttachment,
    currentPosition: ImagePosition,
    onDismiss: () -> Unit,
    onSave: (ImagePosition, NoteAttachment) -> Unit,
    onRemove: () -> Unit
) {
    var selectedPosition by remember { mutableStateOf(currentPosition) }
    var scale by remember { mutableStateOf(1f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Image Position", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = attachment.storedPath,
                    contentDescription = attachment.originalName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Position:", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                
                ImagePosition.entries.forEach { position ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPosition = position }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (position) {
                                ImagePosition.INLINE -> "Inline with text"
                                ImagePosition.BEHIND_TEXT -> "Behind text (background)"
                                ImagePosition.IN_FRONT_OF_TEXT -> "In front of text (overlay)"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedPosition == position) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Size: ${(scale * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { scale = (scale - 0.1f).coerceIn(0.3f, 2f) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Smaller")
                    }
                    TextButton(onClick = { scale = (scale + 0.1f).coerceIn(0.3f, 2f) }) {
                        Icon(Icons.Default.Add, contentDescription = "Larger")
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onRemove) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = {
                    onSave(selectedPosition, attachment.copy(
                        metadata = attachment.metadata + ("scale" to scale.toString())
                    ))
                }) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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

        // ── Raw encrypted blob ──
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

        // ── Disguise section ──
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

@Composable
private fun EncryptAttachmentsDialog(
    attachments: List<NoteAttachment>,
    onDismiss: () -> Unit,
    onEncrypt: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encrypt Note") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "You have ${attachments.size} image(s) in your note. Choose how to encrypt:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onEncrypt(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Shield, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Encrypt All Together", fontWeight = FontWeight.Bold)
                        Text(
                            "Note content + images in one encrypted blob",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun buildNoteJsonFromBlocks(blocks: List<ContentBlock>): String {
    val json = org.json.JSONObject()
    val blocksArray = org.json.JSONArray()
    
    blocks.forEach { block ->
        when (block) {
            is ContentBlock.TextBlock -> {
                val textObj = org.json.JSONObject()
                textObj.put("type", "text")
                textObj.put("content", block.content)
                blocksArray.put(textObj)
            }
            is ContentBlock.ImageBlock -> {
                val imgObj = org.json.JSONObject()
                imgObj.put("type", "image")
                imgObj.put("attachment", org.json.JSONObject().apply {
                    put("id", block.attachment.id)
                    put("originalName", block.attachment.originalName)
                    put("mimeType", block.attachment.mimeType)
                    put("size", block.attachment.size)
                    put("storedPath", block.attachment.storedPath)
                    put("type", block.attachment.type.name)
                    put("isIntegrated", block.attachment.isIntegrated)
                    put("encryptedPath", block.attachment.encryptedPath)
                    put("position", block.position.name)
                    put("widthFraction", block.widthFraction)
                    block.attachment.metadata.forEach { (k, v) ->
                        put("meta_$k", v)
                    }
                })
                blocksArray.put(imgObj)
            }
        }
    }
    
    json.put("blocks", blocksArray)
    return json.toString()
}

private fun parseNoteJson(plaintext: String): Pair<String, String> {
    return try {
        val json = org.json.JSONObject(plaintext)
        if (json.has("blocks")) {
            val blocksArray = json.getJSONArray("blocks")
            val textContent = StringBuilder()
            val attachmentsList = mutableListOf<NoteAttachment>()
            
            for (i in 0 until blocksArray.length()) {
                val blockObj = blocksArray.getJSONObject(i)
                val type = blockObj.optString("type", "text")
                
                if (type == "text") {
                    textContent.append(blockObj.optString("content", ""))
                    textContent.append("\n")
                } else if (type == "image") {
                    val attObj = blockObj.getJSONObject("attachment")
                    val metadata = mutableMapOf<String, String>()
                    attObj.keys().forEach { key ->
                        if (key.startsWith("meta_")) {
                            metadata[key.removePrefix("meta_")] = attObj.getString(key)
                        }
                    }
                    val position = try {
                        ImagePosition.valueOf(attObj.optString("position", "INLINE"))
                    } catch (_: Exception) {
                        ImagePosition.INLINE
                    }
                    val widthFraction = attObj.optDouble("widthFraction", 0.8).toFloat()
                    
                    val att = NoteAttachment(
                        id = attObj.optString("id", java.util.UUID.randomUUID().toString()),
                        originalName = attObj.optString("originalName", "image"),
                        mimeType = attObj.optString("mimeType", "image/jpeg"),
                        size = attObj.optLong("size", 0),
                        storedPath = attObj.optString("storedPath", ""),
                        type = try {
                            NoteAttachment.AttachmentType.valueOf(attObj.optString("type", "IMAGE"))
                        } catch (_: Exception) {
                            NoteAttachment.AttachmentType.IMAGE
                        },
                        isIntegrated = attObj.optBoolean("isIntegrated", false),
                        encryptedPath = attObj.optString("encryptedPath", ""),
                        metadata = metadata
                    )
                    attachmentsList.add(att)
                }
            }
            
            val content = textContent.toString().trimEnd('\n')
            val attJson = attachmentsToJson(attachmentsList, embedFiles = true)
            content to attJson
        } else {
            val content = json.optString("content", plaintext)
            val attachments = json.optJSONArray("attachments")?.toString() ?: "[]"
            content to attachments
        }
    } catch (_: Exception) {
        plaintext to "[]"
    }
}
