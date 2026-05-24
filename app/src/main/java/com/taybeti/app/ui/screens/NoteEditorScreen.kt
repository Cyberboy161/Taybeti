package com.taybeti.app.ui.screens

import android.content.Intent
import android.graphics.Bitmap

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay

import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64

// ─── Leap 1-5: Rich text data model ───

data class TextSpan(
    val text: String = "",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val fontFamily: String = "Default",
    val fontSize: Int = 16,
    val textColor: String? = null,
    val highlightColor: String? = null
) {
    fun toSpanStyle(): SpanStyle {
        var style = SpanStyle(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = buildTextDecoration(),
            fontSize = fontSize.sp,
            fontFamily = when (fontFamily) {
                "Serif" -> FontFamily.Serif
                "Monospace" -> FontFamily.Monospace
                "SansCondensed" -> FontFamily.SansSerif
                else -> FontFamily.Default
            }
        )
        textColor?.let { style = style.copy(color = Color(android.graphics.Color.parseColor(it))) }
        highlightColor?.let { /* background handled separately */ }
        return style
    }

    private fun buildTextDecoration(): TextDecoration? {
        val decorations = mutableListOf<TextDecoration>()
        if (isUnderline) decorations.add(TextDecoration.Underline)
        if (isStrikethrough) decorations.add(TextDecoration.LineThrough)
        return when (decorations.size) {
            0 -> null
            1 -> decorations[0]
            else -> TextDecoration.combine(decorations)
        }
    }

    fun copyWithToggle(field: String): TextSpan {
        return when (field) {
            "bold" -> copy(isBold = !isBold)
            "italic" -> copy(isItalic = !isItalic)
            "underline" -> copy(isUnderline = !isUnderline)
            "strikethrough" -> copy(isStrikethrough = !isStrikethrough)
            else -> this
        }
    }
}

enum class TextAlignMode { LEFT, CENTER, RIGHT, JUSTIFY }
enum class ListType { NONE, BULLETED, NUMBERED }
enum class MarginPreset { NARROW, NORMAL, WIDE }

data class Paragraph(
    val spans: MutableList<TextSpan> = mutableListOf(TextSpan()),
    var alignment: TextAlignMode = TextAlignMode.LEFT,
    var listType: ListType = ListType.NONE,
    var tableRows: MutableList<TableRow>? = null
)

data class TableRow(
    val cells: List<String>
)

data class FormattedText(
    val paragraphs: MutableList<Paragraph> = mutableListOf(Paragraph()),
    var headerText: String = "",
    var footerText: String = "",
    var showPageNumbers: Boolean = true
) {
    fun toPlainText(): String {
        return paragraphs.joinToString("\n") { paragraph ->
            paragraph.spans.joinToString("") { it.text }
        }
    }

    fun toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            paragraphs.forEachIndexed { paraIndex, paragraph ->
                val prefix = when (paragraph.listType) {
                    ListType.BULLETED -> "• "
                    ListType.NUMBERED -> "${paraIndex + 1}. "
                    ListType.NONE -> ""
                }
                if (prefix.isNotEmpty()) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(prefix)
                    }
                }
                paragraph.spans.forEach { span ->
                    if (span.text.isNotEmpty()) {
                        val spanStyle = span.toSpanStyle()
                        withStyle(spanStyle) {
                            append(span.text)
                        }
                        span.highlightColor?.let { hc ->
                            addStyle(
                                SpanStyle(background = Color(android.graphics.Color.parseColor(hc))),
                                length - span.text.length,
                                length
                            )
                        }
                    }
                }
                val tableRows = paragraph.tableRows
                if (tableRows != null && tableRows.isNotEmpty()) {
                    append("\n")
                    tableRows.forEach { row ->
                        row.cells.forEachIndexed { cellIndex, cell ->
                            withStyle(SpanStyle(background = Color(0xFFE0E0E0))) {
                                append(" $cell ")
                            }
                            if (cellIndex < row.cells.lastIndex) append(" | ")
                        }
                        append("\n")
                    }
                }
                if (paraIndex < paragraphs.lastIndex) append("\n")
            }
        }
    }

    fun getCurrentSpan(): TextSpan {
        val lastPara = paragraphs.lastOrNull() ?: return TextSpan()
        return lastPara.spans.lastOrNull() ?: TextSpan()
    }

    fun applyToCurrentSpan(transform: (TextSpan) -> TextSpan) {
        if (paragraphs.isEmpty()) {
            paragraphs.add(Paragraph())
        }
        val para = paragraphs.last()
        if (para.spans.isEmpty()) {
            para.spans.add(TextSpan())
        }
        val idx = para.spans.lastIndex
        para.spans[idx] = transform(para.spans[idx])
    }

    fun appendText(char: String) {
        if (paragraphs.isEmpty()) paragraphs.add(Paragraph())
        val para = paragraphs.last()
        if (para.spans.isEmpty()) para.spans.add(TextSpan())
        val lastSpan = para.spans.last()
        para.spans[para.spans.lastIndex] = lastSpan.copy(text = lastSpan.text + char)
    }

    fun deleteLastChar() {
        if (paragraphs.isEmpty()) return
        val para = paragraphs.last()
        if (para.spans.isEmpty()) return
        val idx = para.spans.lastIndex
        val span = para.spans[idx]
        if (span.text.isNotEmpty()) {
            para.spans[idx] = span.copy(text = span.text.dropLast(1))
        } else if (para.spans.size > 1) {
            para.spans.removeAt(idx)
        } else if (paragraphs.size > 1) {
            paragraphs.removeAt(paragraphs.lastIndex)
        }
    }

    fun insertNewline() {
        if (paragraphs.isEmpty()) paragraphs.add(Paragraph())
        val currentPara = paragraphs.last()
        val newPara = Paragraph(
            alignment = currentPara.alignment,
            listType = if (currentPara.listType == ListType.NUMBERED) ListType.NUMBERED else ListType.NONE
        )
        paragraphs.add(newPara)
    }

    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{\"paragraphs\":[")
        paragraphs.forEachIndexed { i, para ->
            if (i > 0) sb.append(",")
            sb.append("{\"spans\":[")
            para.spans.forEachIndexed { j, span ->
                if (j > 0) sb.append(",")
                sb.append("{\"text\":\"${escapeJson(span.text)}\",")
                sb.append("\"bold\":${span.isBold},")
                sb.append("\"italic\":${span.isItalic},")
                sb.append("\"underline\":${span.isUnderline},")
                sb.append("\"strikethrough\":${span.isStrikethrough},")
                sb.append("\"fontFamily\":\"${span.fontFamily}\",")
                sb.append("\"fontSize\":${span.fontSize},")
                sb.append("\"textColor\":${span.textColor?.let { "\"$it\"" } ?: "null"},")
                sb.append("\"highlightColor\":${span.highlightColor?.let { "\"$it\"" } ?: "null"}")
                sb.append("}")
            }
            sb.append("],")
            sb.append("\"alignment\":\"${para.alignment.name}\",")
            sb.append("\"listType\":\"${para.listType.name}\"")
            val tableRows = para.tableRows
            if (tableRows != null) {
                sb.append(",\"tableRows\":[")
                tableRows.forEachIndexed { ri, row ->
                    if (ri > 0) sb.append(",")
                    sb.append("{\"cells\":[")
                    row.cells.forEachIndexed { ci, cell ->
                        if (ci > 0) sb.append(",")
                        sb.append("\"${escapeJson(cell)}\"")
                    }
                    sb.append("]}")
                }
                sb.append("]")
            }
            sb.append("}")
        }
        sb.append("],")
        sb.append("\"header\":\"${escapeJson(headerText)}\",")
        sb.append("\"footer\":\"${escapeJson(footerText)}\",")
        sb.append("\"showPageNumbers\":$showPageNumbers")
        sb.append("}")
        return sb.toString()
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    companion object {
        fun fromJson(json: String): FormattedText {
            return try {
                val obj = org.json.JSONObject(json)
                val ft = FormattedText()
                ft.headerText = obj.optString("header", "")
                ft.footerText = obj.optString("footer", "")
                ft.showPageNumbers = obj.optBoolean("showPageNumbers", true)
                val parasArray = obj.optJSONArray("paragraphs")
                if (parasArray != null) {
                    ft.paragraphs.clear()
                    for (i in 0 until parasArray.length()) {
                        val paraObj = parasArray.getJSONObject(i)
                        val para = Paragraph()
                        para.alignment = try {
                            TextAlignMode.valueOf(paraObj.optString("alignment", "LEFT"))
                        } catch (_: Exception) { TextAlignMode.LEFT }
                        para.listType = try {
                            ListType.valueOf(paraObj.optString("listType", "NONE"))
                        } catch (_: Exception) { ListType.NONE }
                        val spansArray = paraObj.optJSONArray("spans")
                        if (spansArray != null) {
                            for (j in 0 until spansArray.length()) {
                                val spanObj = spansArray.getJSONObject(j)
                                val span = TextSpan(
                                    text = spanObj.optString("text", ""),
                                    isBold = spanObj.optBoolean("bold", false),
                                    isItalic = spanObj.optBoolean("italic", false),
                                    isUnderline = spanObj.optBoolean("underline", false),
                                    isStrikethrough = spanObj.optBoolean("strikethrough", false),
                                    fontFamily = spanObj.optString("fontFamily", "Default"),
                                    fontSize = spanObj.optInt("fontSize", 16),
                                    textColor = if (spanObj.isNull("textColor")) null else spanObj.optString("textColor", null),
                                    highlightColor = if (spanObj.isNull("highlightColor")) null else spanObj.optString("highlightColor", null)
                                )
                                para.spans.add(span)
                            }
                        } else {
                            para.spans.add(TextSpan())
                        }
                        if (paraObj.has("tableRows")) {
                            val tableArray = paraObj.getJSONArray("tableRows")
                            val rows = mutableListOf<TableRow>()
                            for (r in 0 until tableArray.length()) {
                                val rowObj = tableArray.getJSONObject(r)
                                val cellsArray = rowObj.getJSONArray("cells")
                                val cells = mutableListOf<String>()
                                for (c in 0 until cellsArray.length()) {
                                    cells.add(cellsArray.getString(c))
                                }
                                rows.add(TableRow(cells))
                            }
                            para.tableRows = rows
                        }
                        ft.paragraphs.add(para)
                    }
                }
                if (ft.paragraphs.isEmpty()) ft.paragraphs.add(Paragraph())
                ft
            } catch (_: Exception) {
                FormattedText()
            }
        }

        fun fromPlainText(text: String): FormattedText {
            val ft = FormattedText()
            ft.paragraphs.clear()
            val lines = text.split("\n")
            lines.forEach { line ->
                ft.paragraphs.add(Paragraph(spans = mutableListOf(TextSpan(text = line))))
            }
            if (ft.paragraphs.isEmpty()) ft.paragraphs.add(Paragraph())
            return ft
        }
    }
}

// ─── Leap 7: Undo/Redo ───

data class EditorState(
    val formattedText: FormattedText,
    val currentPageIndex: Int = 0
)

class UndoRedoManager {
    private val undoStack = mutableListOf<EditorState>()
    private val redoStack = mutableListOf<EditorState>()
    var currentState: EditorState? = null
        private set

    fun pushState(ft: FormattedText, pageIndex: Int) {
        currentState?.let {
            undoStack.add(it)
            if (undoStack.size > 100) undoStack.removeAt(0)
        }
        currentState = EditorState(ft.copy(), pageIndex)
        redoStack.clear()
    }

    fun undo(ft: FormattedText, pageIndex: Int): Pair<FormattedText, Int>? {
        currentState?.let {
            redoStack.add(it)
        }
        return if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            val old = currentState
            currentState = prev
            redoStack.add(old ?: EditorState(ft, pageIndex))
            prev.formattedText to prev.currentPageIndex
        } else null
    }

    fun redo(ft: FormattedText, pageIndex: Int): Pair<FormattedText, Int>? {
        return if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            currentState?.let { undoStack.add(it) }
            currentState = next
            next.formattedText to next.currentPageIndex
        } else null
    }

    fun canUndo() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()
}

// ─── Leap 8: Find and Replace ───

data class SearchResult(
    val paragraphIndex: Int,
    val spanIndex: Int,
    val startIndex: Int,
    val length: Int
)

// ─── Leap 6: Margin settings ───

data class MarginSettings(
    val preset: MarginPreset = MarginPreset.NORMAL
) {
    val horizontal: Int
        get() = when (preset) {
            MarginPreset.NARROW -> 24
            MarginPreset.NORMAL -> 48
            MarginPreset.WIDE -> 72
        }
    val vertical: Int
        get() = when (preset) {
            MarginPreset.NARROW -> 24
            MarginPreset.NORMAL -> 48
            MarginPreset.WIDE -> 72
        }
}

// ─── Existing data classes ───

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

// ─── Leap 10: Table data ───

data class TableInsertData(
    val rows: Int = 3,
    val columns: Int = 3
)

// ─── Main Screen ───

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

    // Leap 1: Rich text pages
    val pages = remember { mutableStateListOf<FormattedText>(FormattedText()) }
    var currentPageIndex by remember { mutableStateOf(0) }

    // Leap 2: Font settings
    var currentFontFamily by remember { mutableStateOf("Default") }
    var currentFontSize by remember { mutableStateOf(16) }

    // Leap 3: Alignment
    var currentAlignment by remember { mutableStateOf(TextAlignMode.LEFT) }

    // Leap 4: Color settings
    var showColorPicker by remember { mutableStateOf(false) }
    var colorPickerMode by remember { mutableStateOf("text") } // "text" or "highlight"
    var currentTextColor by remember { mutableStateOf<String?>(null) }
    var currentHighlightColor by remember { mutableStateOf<String?>(null) }

    // Leap 5: List settings
    var currentListType by remember { mutableStateOf(ListType.NONE) }

    // Leap 6: Margins
    var marginSettings by remember { mutableStateOf(MarginSettings()) }

    // Leap 7: Undo/Redo
    val undoRedoManager = remember { UndoRedoManager() }

    // Leap 8: Find and Replace
    var showFindReplace by remember { mutableStateOf(false) }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var currentSearchResultIndex by remember { mutableStateOf(-1) }

    // Leap 9: Header/Footer (stored per FormattedText)

    // Leap 10: Table
    var showTableDialog by remember { mutableStateOf(false) }
    var tableInsertData by remember { mutableStateOf(TableInsertData()) }



    val PAGE_WIDTH_DP = 560
    val PAGE_HEIGHT_DP = 792
    var lastUndoSaveTime by remember { mutableStateOf(0L) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showPageColorPicker by remember { mutableStateOf(false) }
    var pageBackgroundColor by remember { mutableStateOf(Color.White) }
    var showDrawingPanel by remember { mutableStateOf(false) }
    var editingTableCell by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
    var tableCellText by remember { mutableStateOf("") }

    fun saveUndoState() {
        val now = System.currentTimeMillis()
        if (now - lastUndoSaveTime > 2000L) {
            if (currentPageIndex < pages.size) {
                undoRedoManager.pushState(pages[currentPageIndex].copy(), currentPageIndex)
            }
            lastUndoSaveTime = now
        }
    }

    fun saveUndoStateImmediate() {
        if (currentPageIndex < pages.size) {
            undoRedoManager.pushState(pages[currentPageIndex].copy(), currentPageIndex)
        }
    }

    fun performUndo() {
        val result = undoRedoManager.undo(pages.getOrNull(currentPageIndex) ?: FormattedText(), currentPageIndex)
        result?.let { (ft, idx) ->
            if (idx < pages.size) {
                pages[idx] = ft
            }
            currentPageIndex = idx
        }
    }

    fun performRedo() {
        val result = undoRedoManager.redo(pages.getOrNull(currentPageIndex) ?: FormattedText(), currentPageIndex)
        result?.let { (ft, idx) ->
            if (idx < pages.size) {
                pages[idx] = ft
            }
            currentPageIndex = idx
        }
    }

    fun performFindReplace() {
        if (findText.isEmpty()) return
        searchResults = mutableListOf()
        pages.forEachIndexed { pageIdx, ft ->
            ft.paragraphs.forEachIndexed { paraIdx, para ->
                para.spans.forEachIndexed { spanIdx, span ->
                    var idx = span.text.indexOf(findText, ignoreCase = true)
                    while (idx >= 0) {
                        (searchResults as MutableList).add(
                            SearchResult(pageIdx, spanIdx, idx, findText.length)
                        )
                        idx = span.text.indexOf(findText, idx + 1, ignoreCase = true)
                    }
                }
            }
        }
        currentSearchResultIndex = if (searchResults.isNotEmpty()) 0 else -1
    }

    fun replaceCurrent() {
        if (currentSearchResultIndex < 0 || searchResults.isEmpty()) return
        val result = searchResults[currentSearchResultIndex]
        val ft = pages[result.paragraphIndex]
        val para = ft.paragraphs[result.paragraphIndex]
        val span = para.spans[result.spanIndex]
        val newText = span.text.replaceRange(result.startIndex, result.startIndex + result.length, replaceText)
        para.spans[result.spanIndex] = span.copy(text = newText)
        pages[result.paragraphIndex] = ft
        performFindReplace()
    }

    fun replaceAll() {
        if (findText.isEmpty()) return
        pages.forEachIndexed { pageIdx, ft ->
            ft.paragraphs.forEach { para ->
                para.spans.forEachIndexed { spanIdx, span ->
                    if (span.text.contains(findText, ignoreCase = true)) {
                        para.spans[spanIdx] = span.copy(
                            text = span.text.replace(findText, replaceText, ignoreCase = true)
                        )
                    }
                }
            }
        }
        searchResults = emptyList()
        currentSearchResultIndex = -1
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val noteJson = buildNoteJsonRich(pages, images, marginSettings)
                    val plainBytes = noteJson.toByteArray(Charsets.UTF_8)
                    if (plainBytes.isEmpty()) {
                        Toast.makeText(context, "Error: note content is empty", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
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
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(outputBytes)
                            stream.flush()
                        }
                        SecureMemory.clear(plaintext.toCharArray())
                        SecureMemory.clear(noteKey.toCharArray())
                        plaintext = ""
                        noteKey = ""
                        pages.clear()
                        pages.add(FormattedText())
                        images.clear()
                        isLocked = true
                        Toast.makeText(context, "Note encrypted and saved", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Encryption failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
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

    val hasUnsavedChanges = !isLocked && (title.isNotEmpty() || pages.any { it.toPlainText().isNotEmpty() } || images.isNotEmpty())

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
                saveUndoState()
                if (currentPageIndex < pages.size) {
                    pages[currentPageIndex].appendText(char.toString())
                }
            },
            onDel = {
                saveUndoState()
                if (currentPageIndex < pages.size) {
                    pages[currentPageIndex].deleteLastChar()
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
                                                val (loadedPages, attJson) = parseNoteJsonRich(decryptedStr)
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
                                                val (loadedPages, attJson) = parseNoteJsonRich(decryptedStr)
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
                                            pages.add(FormattedText())
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
                                    val noteJson = buildNoteJsonRich(pages, images, marginSettings)
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
                                        pages.add(FormattedText())
                                        images.clear()
                                        isLocked = true
                                        Toast.makeText(context, "🔒 ${strings.encryptedNote}", Toast.LENGTH_SHORT).show()
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

        if (showFindReplace) {
            val findKbState = remember { KeyboardState() }
            var findField by remember { mutableStateOf("find") }
            LaunchedEffect(findField, findKbState) {
                findKbState.attach(
                    onKey = { char ->
                        if (findField == "find") findText += char
                        else replaceText += char
                    },
                    onDel = {
                        if (findField == "find") { if (findText.isNotEmpty()) findText = findText.dropLast(1) }
                        else { if (replaceText.isNotEmpty()) replaceText = replaceText.dropLast(1) }
                    },
                    onDone = { findKbState.detach() }
                )
            }
            Dialog(
                onDismissRequest = { showFindReplace = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Find and Replace", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides findKbState) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (findField == "find") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .border(
                                            1.dp,
                                            if (findField == "find") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { findField = "find" }
                                        .padding(12.dp)
                                ) {
                                    Text(if (findText.isEmpty()) "Find..." else findText, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { performFindReplace() }) {
                                    Icon(Icons.Default.Search, "Find")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (findField == "replace") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        1.dp,
                                        if (findField == "replace") MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { findField = "replace" }
                                    .padding(12.dp)
                            ) {
                                Text(if (replaceText.isEmpty()) "Replace with..." else replaceText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Found ${searchResults.size} matches", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                replaceCurrent()
                                Toast.makeText(context, "Replaced", Toast.LENGTH_SHORT).show()
                                showFindReplace = false
                            }, modifier = Modifier.weight(1f)) { Text("Replace", fontSize = 12.sp) }
                            Button(onClick = {
                                val count = searchResults.size
                                replaceAll()
                                Toast.makeText(context, "Replaced $count matches", Toast.LENGTH_SHORT).show()
                                showFindReplace = false
                            }, modifier = Modifier.weight(1f)) { Text("All", fontSize = 12.sp) }
                            TextButton(onClick = { showFindReplace = false }) { Text("Close", fontSize = 12.sp) }
                        }
                        if (findKbState.isVisible) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomKeyboard(
                                onKeyPress = { findKbState.onKeyPress?.invoke(it) },
                                onDelete = { findKbState.onDelete?.invoke() },
                                onDone = { findKbState.onDone?.invoke() }
                            )
                        }
                    }
                }
            }
        }

        // Leap 4: Color picker dialog
        if (showColorPicker) {
            val presetColors = listOf(
                "#000000", "#FFFFFF", "#FF0000", "#00FF00", "#0000FF",
                "#FFFF00", "#FF00FF", "#00FFFF", "#FF8800", "#8800FF",
                "#FF4444", "#44FF44", "#4444FF", "#888888", "#CCCCCC",
                "#FFEBEE", "#E8F5E9", "#E3F2FD", "#FFF3E0", "#F3E5F5"
            )
            Dialog(onDismissRequest = { showColorPicker = false }) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (colorPickerMode == "text") "Text Color" else "Highlight Color",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Color grid
                        val rows = presetColors.chunked(5)
                        rows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(android.graphics.Color.parseColor(color)),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if ((colorPickerMode == "text" && currentTextColor == color) ||
                                                    (colorPickerMode == "highlight" && currentHighlightColor == color))
                                                    MaterialTheme.colorScheme.primary else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                saveUndoState()
                                                if (colorPickerMode == "text") {
                                                    currentTextColor = if (currentTextColor == color) null else color
                                                    pages.getOrNull(currentPageIndex)?.applyToCurrentSpan {
                                                        it.copy(textColor = currentTextColor)
                                                    }
                                                } else {
                                                    currentHighlightColor = if (currentHighlightColor == color) null else color
                                                    pages.getOrNull(currentPageIndex)?.applyToCurrentSpan {
                                                        it.copy(highlightColor = currentHighlightColor)
                                                    }
                                                }
                                                showColorPicker = false
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                saveUndoState()
                                if (colorPickerMode == "text") {
                                    currentTextColor = null
                                    pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(textColor = null) }
                                } else {
                                    currentHighlightColor = null
                                    pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(highlightColor = null) }
                                }
                                showColorPicker = false
                            }, modifier = Modifier.weight(1f)) {
                                Text("Clear")
                            }
                            TextButton(onClick = { showColorPicker = false }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        if (showTableDialog) {
            AlertDialog(
                onDismissRequest = { showTableDialog = false },
                title = { Text("Insert Table") },
                text = {
                    Column {
                        Text("Rows: ${tableInsertData.rows}", fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (tableInsertData.rows > 1) tableInsertData = tableInsertData.copy(rows = tableInsertData.rows - 1) }) { Text("-") }
                            Button(onClick = { if (tableInsertData.rows < 20) tableInsertData = tableInsertData.copy(rows = tableInsertData.rows + 1) }) { Text("+") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Columns: ${tableInsertData.columns}", fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { if (tableInsertData.columns > 1) tableInsertData = tableInsertData.copy(columns = tableInsertData.columns - 1) }) { Text("-") }
                            Button(onClick = { if (tableInsertData.columns < 10) tableInsertData = tableInsertData.copy(columns = tableInsertData.columns + 1) }) { Text("+") }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        saveUndoStateImmediate()
                        val ft = pages.getOrNull(currentPageIndex)
                        if (ft != null) {
                            val rows = (0 until tableInsertData.rows).map {
                                TableRow((0 until tableInsertData.columns).map { "" }.toMutableList())
                            }.toMutableList()
                            ft.paragraphs.add(Paragraph(tableRows = rows))
                        }
                        showTableDialog = false
                    }) { Text("Insert") }
                },
                dismissButton = {
                    TextButton(onClick = { showTableDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showTemplatePicker) {
            AlertDialog(
                onDismissRequest = { showTemplatePicker = false },
                title = { Text("Page Templates") },
                text = {
                    Column {
                        listOf(
                            "Blank" to "Empty page",
                            "Lined" to "Horizontal lines for writing",
                            "Grid" to "Grid pattern",
                            "Dotted" to "Dot grid pattern",
                            "Cornell" to "Cornell note-taking layout"
                        ).forEach { (name, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val ft = pages.getOrNull(currentPageIndex)
                                        if (ft != null) {
                                            ft.headerText = when (name) {
                                                "Cornell" -> "Topic: ___________"
                                                else -> ""
                                            }
                                            ft.footerText = when (name) {
                                                "Cornell" -> "Summary: ___________"
                                                else -> ""
                                            }
                                        }
                                        showTemplatePicker = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Medium)
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTemplatePicker = false }) { Text("Cancel") }
                }
            )
        }

        if (showPageColorPicker) {
            val colors = listOf(
                Color.White, Color(0xFFF5F5DC), Color(0xFFF0F8FF),
                Color(0xFFF5FFFA), Color(0xFFFFF8DC), Color(0xFFF0FFF0),
                Color(0xFFE6E6FA), Color(0xFFFFE4E1), Color(0xFFF0E68C),
                Color(0xFF1E1E1E), Color(0xFF2C2C2C), Color(0xFF0D1117)
            )
            AlertDialog(
                onDismissRequest = { showPageColorPicker = false },
                title = { Text("Page Background") },
                text = {
                    Column {
                        colors.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(color, RoundedCornerShape(8.dp))
                                            .border(
                                                2.dp,
                                                if (pageBackgroundColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                pageBackgroundColor = color
                                                showPageColorPicker = false
                                            }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPageColorPicker = false }) { Text("Cancel") }
                }
            )
        }

        if (showDrawingPanel) {
            DrawingPanelDialog(
                onDismiss = { showDrawingPanel = false },
                onSave = { bitmap ->
                    scope.launch {
                        val file = File(context.filesDir, "drawing_${System.currentTimeMillis()}.png")
                        file.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        val attachment = NoteAttachment(
                            originalName = file.name,
                            mimeType = "image/png",
                            size = file.length(),
                            storedPath = file.absolutePath,
                            type = NoteAttachment.AttachmentType.IMAGE
                        )
                        images.add(
                            EditorImage(
                                attachment = attachment,
                                x = 50f,
                                y = 50f,
                                width = 300f,
                                height = 300f,
                                layer = ImageLayer.IN_FRONT_OF_TEXT,
                                pageIndex = currentPageIndex
                            )
                        )
                    }
                }
            )
        }

        if (editingTableCell != null) {
            val (paraIdx, rowIdx, cellIdx) = editingTableCell!!
            val cellKbState = remember { KeyboardState() }
            LaunchedEffect(Unit) {
                val ft = pages.getOrNull(currentPageIndex)
                val para = ft?.paragraphs?.getOrNull(paraIdx)
                val cell = para?.tableRows?.getOrNull(rowIdx)?.cells?.getOrNull(cellIdx)
                tableCellText = cell ?: ""
                cellKbState.attach(
                    onKey = { char -> tableCellText += char },
                    onDel = { if (tableCellText.isNotEmpty()) tableCellText = tableCellText.dropLast(1) },
                    onDone = {
                        if (paraIdx < pages.size && pages[paraIdx].paragraphs.size > paraIdx) {
                            val p = pages[currentPageIndex].paragraphs[paraIdx]
                            val rows = p.tableRows
                            if (rows != null && rowIdx < rows.size && cellIdx < rows[rowIdx].cells.size) {
                                val mutableCells = rows[rowIdx].cells.toMutableList()
                                mutableCells[cellIdx] = tableCellText
                                rows[rowIdx] = TableRow(mutableCells)
                            }
                        }
                        editingTableCell = null
                        cellKbState.detach()
                    }
                )
            }
            Dialog(
                onDismissRequest = { editingTableCell = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Edit Cell", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalKeyboardState provides cellKbState) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(tableCellText.ifEmpty { "Type here..." }, color = if (tableCellText.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val ft = pages.getOrNull(currentPageIndex)
                                val para = ft?.paragraphs?.getOrNull(paraIdx)
                                val rows = para?.tableRows
                                if (rows != null && rowIdx < rows.size && cellIdx < rows[rowIdx].cells.size) {
                                    val mutableCells = rows[rowIdx].cells.toMutableList()
                                    mutableCells[cellIdx] = tableCellText
                                    rows[rowIdx] = TableRow(mutableCells)
                                }
                                editingTableCell = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save") }
                        if (cellKbState.isVisible) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomKeyboard(
                                onKeyPress = { cellKbState.onKeyPress?.invoke(it) },
                                onDelete = { cellKbState.onDelete?.invoke() },
                                onDone = { cellKbState.onDone?.invoke() }
                            )
                        }
                    }
                }
            }
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
                            IconButton(onClick = { performUndo() }, enabled = undoRedoManager.canUndo()) {
                                Icon(Icons.Default.Undo, "Undo", tint = if (undoRedoManager.canUndo()) MaterialTheme.colorScheme.onSurface else Color.Gray)
                            }
                            IconButton(onClick = { performRedo() }, enabled = undoRedoManager.canRedo()) {
                                Icon(Icons.Default.Redo, "Redo", tint = if (undoRedoManager.canRedo()) MaterialTheme.colorScheme.onSurface else Color.Gray)
                            }
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

                    // Leap 1-5: Rich text formatting toolbar
                    RichTextToolbar(
                        onToggleBold = {
                            saveUndoState()
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(isBold = !it.isBold) }
                        },
                        onToggleItalic = {
                            saveUndoState()
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(isItalic = !it.isItalic) }
                        },
                        onToggleUnderline = {
                            saveUndoState()
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(isUnderline = !it.isUnderline) }
                        },
                        onToggleStrikethrough = {
                            saveUndoState()
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(isStrikethrough = !it.isStrikethrough) }
                        },
                        currentFontFamily = currentFontFamily,
                        onFontFamilyChange = { newFamily ->
                            saveUndoState()
                            currentFontFamily = newFamily
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(fontFamily = newFamily) }
                        },
                        currentFontSize = currentFontSize,
                        onFontSizeChange = { newSize ->
                            saveUndoState()
                            currentFontSize = newSize
                            pages.getOrNull(currentPageIndex)?.applyToCurrentSpan { it.copy(fontSize = newSize) }
                        },
                        currentAlignment = currentAlignment,
                        onAlignmentChange = {
                            saveUndoState()
                            currentAlignment = it
                            pages.getOrNull(currentPageIndex)?.let { ft ->
                                if (ft.paragraphs.isNotEmpty()) {
                                    val paraIdx = ft.paragraphs.lastIndex
                                    ft.paragraphs[paraIdx] = ft.paragraphs[paraIdx].copy(alignment = it)
                                }
                            }
                        },
                        onTextColorClick = {
                            colorPickerMode = "text"
                            showColorPicker = true
                        },
                        onHighlightColorClick = {
                            colorPickerMode = "highlight"
                            showColorPicker = true
                        },
                        currentListType = currentListType,
                        onListTypeChange = {
                            saveUndoState()
                            currentListType = it
                            pages.getOrNull(currentPageIndex)?.let { ft ->
                                if (ft.paragraphs.isNotEmpty()) {
                                    val paraIdx = ft.paragraphs.lastIndex
                                    ft.paragraphs[paraIdx] = ft.paragraphs[paraIdx].copy(listType = it)
                                }
                            }
                        },
                        currentMargin = marginSettings.preset,
                        onMarginChange = {
                            marginSettings = marginSettings.copy(preset = it)
                        },
                        onAddAttachment = { type ->
                            pendingAttachmentType = type
                            attachmentLauncher.launch(type.toMimePattern())
                        },
                        onInsertNewline = {
                            saveUndoState()
                            pages.getOrNull(currentPageIndex)?.insertNewline()
                        },
                        onInsertTable = {
                            showTableDialog = true
                        },
                        onFindReplace = {
                            showFindReplace = true
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Theme toggle + page options
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = if (pageTheme == "light") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .clickable { pageTheme = "light" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Light mode",
                                tint = if (pageTheme == "light") Color(0xFFFFB300) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = if (pageTheme == "dark") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(3.dp)
                                )
                                .clickable { pageTheme = "dark" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Dark mode",
                                tint = if (pageTheme == "dark") Color(0xFF90CAF9) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(pageBackgroundColor, RoundedCornerShape(3.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                .clickable { showPageColorPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ColorLens, "Page Color", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                .clickable { showTemplatePicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GridOn, "Template", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                .clickable { showDrawingPanel = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, "Draw", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    if (showImageOptions) {
                        val selectedImg = images.find { it.attachment.id == selectedImageId }
                        if (selectedImg != null) {
                            ImageOptionsDialog(
                                image = selectedImg,
                                onDismiss = { showImageOptions = false },
                                onUpdate = { updated ->
                                    val idx = images.indexOfFirst { it.attachment.id == updated.attachment.id }
                                    if (idx >= 0) images[idx] = updated
                                },
                                onDelete = {
                                    val img = images.find { it.attachment.id == selectedImageId }
                                    if (img != null) {
                                        scope.launch {
                                            AttachmentManager.deleteAttachment(context, noteId, img.attachment)
                                        }
                                        images.remove(img)
                                    }
                                }
                            )
                        } else {
                            showImageOptions = false
                        }
                    }

                    WordEditorCanvasRich(
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
                        pageTheme = pageTheme,
                        marginSettings = marginSettings,
                        pageWidth = PAGE_WIDTH_DP,
                        pageHeight = PAGE_HEIGHT_DP,
                        pageBackgroundColor = pageBackgroundColor,
                        onTableCellTap = { paraIdx, rowIdx, cellIdx ->
                            editingTableCell = Triple(paraIdx, rowIdx, cellIdx)
                        }
                    )
                }
            }
        }
    } // KeyboardHost
}

// ─── Leap 1-5: Rich Text Toolbar ───

@Composable
private fun RichTextToolbar(
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleUnderline: () -> Unit,
    onToggleStrikethrough: () -> Unit,
    currentFontFamily: String,
    onFontFamilyChange: (String) -> Unit,
    currentFontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    currentAlignment: TextAlignMode,
    onAlignmentChange: (TextAlignMode) -> Unit,
    onTextColorClick: () -> Unit,
    onHighlightColorClick: () -> Unit,
    currentListType: ListType,
    onListTypeChange: (ListType) -> Unit,
    currentMargin: MarginPreset,
    onMarginChange: (MarginPreset) -> Unit,
    onAddAttachment: (AttachmentType) -> Unit,
    onInsertNewline: () -> Unit,
    onInsertTable: () -> Unit,
    onFindReplace: () -> Unit
) {
    var showFontMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }
    var showMarginMenu by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Row 1: Formatting buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FormatToggleIcon(Icons.Default.FormatBold, "Bold") { onToggleBold() }
            FormatToggleIcon(Icons.Default.FormatItalic, "Italic") { onToggleItalic() }
            FormatToggleIcon(Icons.Default.FormatUnderlined, "Underline") { onToggleUnderline() }
            FormatToggleIcon(Icons.Default.FormatStrikethrough, "Strikethrough") { onToggleStrikethrough() }

            Spacer(modifier = Modifier.width(4.dp))

            // Font family dropdown
            Box {
                TextButton(onClick = { showFontMenu = true }) {
                    Text(currentFontFamily, fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = showFontMenu,
                    onDismissRequest = { showFontMenu = false }
                ) {
                    listOf("Default", "Serif", "Monospace", "SansCondensed").forEach { font ->
                        DropdownMenuItem(
                            text = { Text(font) },
                            onClick = {
                                onFontFamilyChange(font)
                                showFontMenu = false
                            }
                        )
                    }
                }
            }

            // Font size dropdown
            Box {
                TextButton(onClick = { showSizeMenu = true }) {
                    Text("${currentFontSize}sp", fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = showSizeMenu,
                    onDismissRequest = { showSizeMenu = false }
                ) {
                    listOf(12, 14, 16, 18, 20, 24, 28, 32).forEach { size ->
                        DropdownMenuItem(
                            text = { Text("${size}sp") },
                            onClick = {
                                onFontSizeChange(size)
                                showSizeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Alignment buttons
            FormatToggleIcon(Icons.Default.FormatAlignLeft, "Left", active = currentAlignment == TextAlignMode.LEFT) {
                onAlignmentChange(TextAlignMode.LEFT)
            }
            FormatToggleIcon(Icons.Default.FormatAlignCenter, "Center", active = currentAlignment == TextAlignMode.CENTER) {
                onAlignmentChange(TextAlignMode.CENTER)
            }
            FormatToggleIcon(Icons.Default.FormatAlignRight, "Right", active = currentAlignment == TextAlignMode.RIGHT) {
                onAlignmentChange(TextAlignMode.RIGHT)
            }
            FormatToggleIcon(Icons.Default.FormatAlignJustify, "Justify", active = currentAlignment == TextAlignMode.JUSTIFY) {
                onAlignmentChange(TextAlignMode.JUSTIFY)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Color pickers
            IconButton(onClick = onTextColorClick) {
                Icon(Icons.Default.FormatColorText, "Text Color", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onHighlightColorClick) {
                Icon(Icons.Default.FormatColorFill, "Highlight Color", tint = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // List buttons
            FormatToggleIcon(Icons.Default.FormatListBulleted, "Bulleted List", active = currentListType == ListType.BULLETED) {
                onListTypeChange(if (currentListType == ListType.BULLETED) ListType.NONE else ListType.BULLETED)
            }
            FormatToggleIcon(Icons.Default.FormatListNumbered, "Numbered List", active = currentListType == ListType.NUMBERED) {
                onListTypeChange(if (currentListType == ListType.NUMBERED) ListType.NONE else ListType.NUMBERED)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Margin preset
            Box {
                TextButton(onClick = { showMarginMenu = true }) {
                    Text(
                        when (currentMargin) {
                            MarginPreset.NARROW -> "Narrow"
                            MarginPreset.NORMAL -> "Normal"
                            MarginPreset.WIDE -> "Wide"
                        },
                        fontSize = 12.sp
                    )
                }
                DropdownMenu(
                    expanded = showMarginMenu,
                    onDismissRequest = { showMarginMenu = false }
                ) {
                    MarginPreset.entries.forEach { preset ->
                        DropdownMenuItem(
                            text = { Text(preset.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onMarginChange(preset)
                                showMarginMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Table insert
            IconButton(onClick = onInsertTable) {
                Icon(Icons.Default.GridOn, "Insert Table")
            }

            // Find and Replace
            IconButton(onClick = onFindReplace) {
                Icon(Icons.Default.FindReplace, "Find and Replace")
            }

            // Attach
            Box {
                IconButton(onClick = { showAttachMenu = true }) {
                    Icon(Icons.Default.AttachFile, "Attach")
                }
                DropdownMenu(
                    expanded = showAttachMenu,
                    onDismissRequest = { showAttachMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Image") },
                        onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.IMAGE) }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Audio") },
                        onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.AUDIO) }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.VideoFile, null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Video") },
                        onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.VIDEO) }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Document") },
                        onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.DOCUMENT) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatToggleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
    ) {
        Icon(icon, label, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

// ─── Leap 1-11: Rich Word Editor Canvas ───

@Composable
private fun WordEditorCanvasRich(
    pages: MutableList<FormattedText>,
    images: List<EditorImage>,
    currentPageIndex: Int,
    onPageSelect: (Int) -> Unit,
    onImageSelect: (String) -> Unit,
    onImageUpdate: (EditorImage) -> Unit,
    onImageDelete: (String) -> Unit,
    activeField: String?,
    onFieldActivate: (String) -> Unit,
    onFieldDeactivate: () -> Unit,
    pageTheme: String,
    marginSettings: MarginSettings,
    pageWidth: Int,
    pageHeight: Int,
    pageBackgroundColor: Color,
    onTableCellTap: (Int, Int, Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = pageTheme == "dark"
    val pageBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val pageBorder = if (isDark) Color(0xFF444444) else Color.LightGray.copy(alpha = 0.3f)
    val canvasBg = if (isDark) Color(0xFF121212) else Color(0xFFF0F0F0)
    val textColor = if (isDark) Color.White else Color.Black
    val placeholderColor = Color.Gray
    var zoomScale by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBg)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3f)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp)
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                }
        ) {
            pages.forEachIndexed { index, ft ->
                PageBlockRich(
                    formattedText = ft,
                    images = images,
                    pageIndex = index,
                    isSelected = index == currentPageIndex,
                    onPageSelect = { onPageSelect(index); onFieldActivate("content") },
                    onImageSelect = onImageSelect,
                    onImageUpdate = onImageUpdate,
                    onImageDelete = onImageDelete,
                    pageNumber = index + 1,
                    pageBg = pageBackgroundColor,
                    pageBorder = pageBorder,
                    isDark = isDark,
                    textColor = textColor,
                    placeholderColor = placeholderColor,
                    marginSettings = marginSettings,
                    pageWidth = pageWidth,
                    pageHeight = pageHeight,
                    onTableCellTap = { rowIdx, cellIdx -> onTableCellTap(index, rowIdx, cellIdx) }
                )
            }

            // Add page button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(pageWidth.dp)
                        .height(80.dp)
                        .background(pageBg, RoundedCornerShape(12.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { pages.add(FormattedText()) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "Add Page", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Add new page", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Tap to insert a blank page below", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                        }
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

// ─── Leap 1-11: Rich Page Block ───

@Composable
private fun PageBlockRich(
    formattedText: FormattedText,
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
    placeholderColor: Color,
    marginSettings: MarginSettings,
    pageWidth: Int,
    pageHeight: Int,
    onTableCellTap: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
    Box(
        modifier = Modifier
            .width(pageWidth.dp)
            .height(pageHeight.dp)
            .background(pageBg, RoundedCornerShape(2.dp))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else pageBorder,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable { onPageSelect() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = marginSettings.horizontal.dp, vertical = marginSettings.vertical.dp)
        ) {
            // Leap 9: Header
            if (formattedText.headerText.isNotEmpty()) {
                Text(
                    text = formattedText.headerText,
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                formattedText.paragraphs.forEachIndexed { paraIdx, paragraph ->
                    val textAlign = when (paragraph.alignment) {
                        TextAlignMode.LEFT -> TextAlign.Start
                        TextAlignMode.CENTER -> TextAlign.Center
                        TextAlignMode.RIGHT -> TextAlign.End
                        TextAlignMode.JUSTIFY -> TextAlign.Justify
                    }

                    val prefix = when (paragraph.listType) {
                        ListType.BULLETED -> "• "
                        ListType.NUMBERED -> "${paraIdx + 1}. "
                        ListType.NONE -> ""
                    }

                    val paraAnnotated = buildAnnotatedString {
                        paragraph.spans.forEach { span ->
                            if (span.text.isNotEmpty()) {
                                withStyle(span.toSpanStyle()) { append(span.text) }
                            }
                        }
                    }

                    if (paraAnnotated.isNotEmpty() || paragraph.tableRows == null) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (prefix.isNotEmpty()) {
                                Text(
                                    text = prefix,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = paraAnnotated,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = textAlign
                            )
                        }
                    }

                    val pTableRows = paragraph.tableRows
                    if (pTableRows != null && pTableRows.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isDark) Color(0xFF555555) else Color.Gray, RoundedCornerShape(4.dp))
                        ) {
                            pTableRows.forEachIndexed { rowIdx, row ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    row.cells.forEachIndexed { cellIdx, cell ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(0.5.dp, if (isDark) Color(0xFF444444) else Color.Gray.copy(alpha = 0.5f))
                                                .clickable { onTableCellTap(rowIdx, cellIdx) }
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = cell.ifEmpty { " " },
                                                color = textColor,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (formattedText.toPlainText().isEmpty() && formattedText.paragraphs.all { it.tableRows == null }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Start typing here...",
                            color = placeholderColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (isSelected) {
                            val cursorAlpha = remember { Animatable(1f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    cursorAlpha.animateTo(0f, animationSpec = tween(500))
                                    cursorAlpha.animateTo(1f, animationSpec = tween(500))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(textColor.copy(alpha = cursorAlpha.value))
                            )
                        }
                    }
                } else if (isSelected && formattedText.paragraphs.isNotEmpty()) {
                    val lastPara = formattedText.paragraphs.last()
                    if (lastPara.tableRows == null) {
                        val lastText = lastPara.spans.joinToString("") { it.text }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lastText,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val cursorAlpha = remember { Animatable(1f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    cursorAlpha.animateTo(0f, animationSpec = tween(500))
                                    cursorAlpha.animateTo(1f, animationSpec = tween(500))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(textColor.copy(alpha = cursorAlpha.value))
                            )
                        }
                    }
                }
            }

            // Leap 9: Footer
            if (formattedText.footerText.isNotEmpty() || formattedText.showPageNumbers) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedText.footerText,
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (formattedText.showPageNumbers) {
                        Text(
                            text = "Page $pageNumber",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Images behind text
        images.filter { it.layer == ImageLayer.BEHIND_TEXT && it.pageIndex == pageNumber - 1 }.forEach { img ->
            DraggableImage(
                image = img,
                onSelect = onImageSelect,
                onUpdate = onImageUpdate,
                onDelete = onImageDelete
            )
        }

        // Integrated images
        val integratedImages = images.filter { it.layer == ImageLayer.INTEGRATED && it.pageIndex == pageNumber - 1 }
        if (integratedImages.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(marginSettings.horizontal.dp, marginSettings.vertical.dp)) {
                val primaryImage = integratedImages.first()
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

        // Images in front of text
        images.filter { it.layer == ImageLayer.IN_FRONT_OF_TEXT && it.pageIndex == pageNumber - 1 }.forEach { img ->
            DraggableImage(
                image = img,
                onSelect = onImageSelect,
                onUpdate = onImageUpdate,
                onDelete = onImageDelete
            )
        }
    }
    }
}

// ─── Existing PageBlock (backward compat) ───

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
            .height(750.dp)
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

// ─── DraggableImage (unchanged) ───

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

// ─── ImageOptionsDialog (unchanged) ───

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

                listOf(ImageLayer.IN_FRONT_OF_TEXT, ImageLayer.BEHIND_TEXT, ImageLayer.INTEGRATED).forEach { layer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(image.copy(layer = layer))
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (layer) {
                                ImageLayer.IN_FRONT_OF_TEXT -> Icons.Default.Image
                                ImageLayer.BEHIND_TEXT -> Icons.Default.FormatColorFill
                                ImageLayer.INTEGRATED -> Icons.Default.FormatAlignLeft
                                else -> Icons.Default.Image
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (image.layer == layer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (layer) {
                                    ImageLayer.IN_FRONT_OF_TEXT -> "In Front of Text"
                                    ImageLayer.BEHIND_TEXT -> "As Background"
                                    ImageLayer.INTEGRATED -> "Fit in Text"
                                    else -> ""
                                },
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (layer) {
                                    ImageLayer.IN_FRONT_OF_TEXT -> "Picture covers the text area"
                                    ImageLayer.BEHIND_TEXT -> "Text appears in front of picture"
                                    ImageLayer.INTEGRATED -> "Text wraps around the picture"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (image.layer == layer) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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

// ─── Leap 1-11: Build Note JSON with rich text ───

private fun buildNoteJsonRich(
    pages: List<FormattedText>,
    images: List<EditorImage>,
    marginSettings: MarginSettings
): String {
    val json = org.json.JSONObject()
    val pagesArray = org.json.JSONArray()
    pages.forEach { ft ->
        pagesArray.put(ft.toJson())
    }
    json.put("pages", pagesArray)
    json.put("version", 2)
    json.put("marginPreset", marginSettings.preset.name)

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

// ─── Leap 1-11: Parse Note JSON with rich text ───

private fun parseNoteJsonRich(plaintext: String): Pair<List<FormattedText>, String> {
    return try {
        val json = org.json.JSONObject(plaintext)
        val pagesList = mutableListOf<FormattedText>()

        if (json.has("pages")) {
            val pagesArray = json.getJSONArray("pages")
            for (i in 0 until pagesArray.length()) {
                val pageStr = pagesArray.getString(i)
                pagesList.add(FormattedText.fromJson(pageStr))
            }
        } else if (json.has("content")) {
            pagesList.add(FormattedText.fromPlainText(json.getString("content")))
        }

        if (pagesList.isEmpty()) pagesList.add(FormattedText())

        val imagesArray = json.optJSONArray("images")
        val attachmentsList = mutableListOf<NoteAttachment>()

        if (imagesArray != null) {
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
        }

        pagesList to attachmentsToJson(attachmentsList, embedFiles = true)
    } catch (_: Exception) {
        listOf(FormattedText()) to "[]"
    }
}

// ─── Backward compat: buildNoteJson and parseNoteJson ───

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

@Composable
private fun DrawingPanelDialog(
    onDismiss: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    var strokeColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(5f) }
    var isEraser by remember { mutableStateOf(false) }
    var showColorWheel by remember { mutableStateOf(false) }
    val paths = remember { mutableStateListOf<Pair<androidx.compose.ui.graphics.Path, Pair<Color, Float>>>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drawing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { isEraser = !isEraser }) {
                            Icon(
                                Icons.Default.FormatClear,
                                "Eraser",
                                tint = if (isEraser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showColorWheel = !showColorWheel }) {
                            Icon(Icons.Default.ColorLens, "Color", tint = strokeColor)
                        }
                        IconButton(onClick = { strokeWidth = (strokeWidth + 2f).coerceAtMost(20f) }) {
                            Icon(Icons.Default.Add, "Thicker")
                        }
                        IconButton(onClick = { strokeWidth = (strokeWidth - 2f).coerceAtLeast(1f) }) {
                            Icon(Icons.Default.Remove, "Thinner")
                        }
                        IconButton(onClick = { if (paths.isNotEmpty()) paths.removeAt(paths.lastIndex) }) {
                            Icon(Icons.Default.Undo, "Undo")
                        }
                        IconButton(onClick = { paths.clear() }) {
                            Icon(Icons.Default.Delete, "Clear")
                        }
                    }
                }

                if (showColorWheel) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Color.Black, Color.Red, Color.Blue, Color.Green,
                            Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF795548),
                            Color(0xFF607D8B), Color(0xFFE91E63), Color(0xFF00BCD4)
                        ).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, RoundedCornerShape(4.dp))
                                    .border(
                                        2.dp,
                                        if (strokeColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        strokeColor = color
                                        isEraser = false
                                        showColorWheel = false
                                    }
                            )
                        }
                    }
                }

                Text("Size: ${strokeWidth.toInt()}px", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .pointerInput(strokeColor, strokeWidth, isEraser) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDragEnd = {
                                    currentPath?.let { path ->
                                        val color = if (isEraser) Color.White else strokeColor
                                        paths.add(path to (color to strokeWidth))
                                    }
                                    currentPath = null
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { (path, colorWidth) ->
                            val (color, width) = colorWidth
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = width,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        currentPath?.let { path ->
                            val color = if (isEraser) Color.White else strokeColor
                            drawPath(
                                path = path,
                                color = color,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
                            val androidCanvas = android.graphics.Canvas(bitmap)
                            androidCanvas.drawColor(android.graphics.Color.WHITE)
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                style = android.graphics.Paint.Style.STROKE
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                            }
                            val composeCanvas = androidx.compose.ui.graphics.Canvas(androidCanvas)
                            val composePaint = androidx.compose.ui.graphics.Paint().apply {
                                isAntiAlias = true
                                style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                strokeCap = StrokeCap.Round
                                strokeJoin = StrokeJoin.Round
                            }
                            paths.forEach { (path, colorWidth) ->
                                val (color, width) = colorWidth
                                composePaint.color = color
                                composePaint.strokeWidth = width
                                composeCanvas.drawPath(path, composePaint)
                            }
                            onSave(bitmap)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save Drawing") }
                }
            }
        }
    }
}
