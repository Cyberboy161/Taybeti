package com.taybeti.app.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val KEYBOARD_PREFS = "keyboard_prefs"
private val KEY_LANG_INDEX = "lang_index"

private val keyShape = RoundedCornerShape(6.dp)
private const val keyHeight = 46
private val rowSpacing = 4.dp

// ─── Per-language layouts ───

private data class KeyboardLayout(
    val code: String,
    val label: String,
    val letterRows: List<String>,
    val isRtl: Boolean = false,
    val uppercaseOverrides: Map<Char, Char> = emptyMap(),
    val longPressMap: Map<Char, Char> = emptyMap()
)

private val keyboardLanguages = listOf(
    // English — standard QWERTY
    KeyboardLayout("en", "EN", listOf(
        "QWERTYUIOP",
        "ASDFGHJKL",
        "ZXCVBNM"
    )),

    // German — DIN 2137-1 QWERTZ
    KeyboardLayout("de", "DE", listOf(
        "QWERTZUIOP\u00DC",
        "ASDFGHJKL\u00D6\u00C4",
        "YXCVBNM\u1E9E"
    )),

    // Turkish — Turkish Q
    KeyboardLayout("tr", "TR", listOf(
        "QWERTYU\u0131OP\u011E\u00DC",
        "ASDFGHJKL\u015E\u0130",
        "ZXCVBNM\u00D6\u00C7"
    ), uppercaseOverrides = mapOf(
        'i' to '\u0130',
        '\u0131' to 'I'
    )),

    // Kurdish Kurmanji — Latin Hawar
    KeyboardLayout("kmr", "KU", listOf(
        "QWE\u00CARTYU\u00DBIOP",
        "ASDFGHJKL\u015E",
        "ZXC\u00C7VBNM\u00CE"
    )),

    // Kurdish Sorani — Arabic script, RTL
    KeyboardLayout("ckb", "\u06A9", listOf(
        "\u0686\u067E\u0642\u06A4\u0641\u063A\u0639\u06BE\u062E\u062D\u062C",
        "\u0634\u0633\u06CC\u0628\u0644\u0627\u062A\u0646\u0645\u06A9\u06AF",
        "\u062F\u0637\u0698\u0631\u06CE\u0648\u06B5"
    ), isRtl = true, longPressMap = mapOf(
        '\u0648' to '\u06C6',   // و → ۆ
        '\u06BE' to '\u06D5',   // ھ → ە
        '\u0644' to '\u06B5',   // ل → ڵ
        '\u0627' to '\u0626',   // ا → ئ
        '\u06CC' to '\u06CE'    // ی → ێ
    ))
)

// ─── Shared symbol layout ───

// Page 0: Numbers / symbols
private val numbersPage = listOf(
    "1234567890",
    "!@#\$%^&*()",
    "-_=+[]{}<>"
)

// Page 1: Revolutionary symbols — clean 5-6 per row
private val revolutionaryPage = listOf(
    "\u262D\u2605\u270A\u2620\u26D3",         // ☭ ★ ✊ 💀 ⛓
    "\u2691\u2717\u272F\uD83D\uDD25\u2692",   // ⚑ ✗ ✯ 🔥 ⚒
    "\u2694\uD83D\uDC4A\uD83D\uDEAB\u26A0\uD83D\uDDF3" // ⚔ 👊 🚫 ⚠ 🗳
)

// Page 2: Anarchy symbols — clean 5-6 per row
private val anarchyPage = listOf(
    "\u24B6\u2691\u2620\u2692\u2699",         // Ⓐ ⚑ 💀 ⚒ ⚙
    "\uD83D\uDDF1\uD83D\uDCA3\uD83D\uDEAB\u2717", // 🗱 💣 🚫 ✗
    "ACAB\u00001312\u0000REVOLT"             // ACAB 1312 REVOLT
)

// Page 3: Queer symbols — clean 4-5 per row
private val queerPage = listOf(
    "\uD83C\uDFF3\uFE0F\u200D\uD83C\uDF08\uD83C\uDFF3\uFE0F\u200D\u26A7\uFE0F", // 🏳️‍🌈 🏳️‍⚧️
    "\u26A7\u2640\u2642\u26A2\u26A4",         // ⚧ ♀ ♂ ⚢ ⚤
    "\uD83D\uDC69\u200D\u2764\uD83D\uDC69\uD83D\uDC68\u200D\u2764\uD83D\uDC68\uD83E\uDDD1\u200D\uD83E\uDD1D\uD83E\uDDD1", // 👩‍❤‍👩 👨‍❤‍👨 🧑‍🤝‍🧑
    "LGBTQ+\u0000PRIDE\u0000QUEER\u0000ALLY" // LGBTQ+ PRIDE QUEER ALLY
)

private val allSymbolPages = listOf(numbersPage, revolutionaryPage, anarchyPage, queerPage)
private val symbolPageLabels = listOf(null, "Revolutionary", "Anarchy", "Queer")

const val PAGE_LETTERS = -1
const val PAGE_NUMBERS = 0
const val PAGE_REVOLUTIONARY = 1
const val PAGE_ANARCHY = 2
const val PAGE_QUEER = 3

// ─── Persistence ───

private fun loadKeyboardIndex(ctx: Context): Int {
    return ctx.getSharedPreferences(KEYBOARD_PREFS, Context.MODE_PRIVATE)
        .getInt(KEY_LANG_INDEX, 0)
        .coerceIn(0, keyboardLanguages.size - 1)
}

private fun saveKeyboardIndex(ctx: Context, index: Int) {
    ctx.getSharedPreferences(KEYBOARD_PREFS, Context.MODE_PRIVATE)
        .edit().putInt(KEY_LANG_INDEX, index).apply()
}

// ─── Main composable ───

@Composable
fun CustomKeyboard(
    onKeyPress: (Char) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var uppercase by remember { mutableStateOf(false) }
    var symbolPage by remember { mutableStateOf(-1) } // -1 = letters, 0=numbers, 1=rev, 2=anarchy, 3=queer
    var langIndex by remember { mutableStateOf(loadKeyboardIndex(ctx)) }
    var showLangPicker by remember { mutableStateOf(false) }
    val currentLang = keyboardLanguages[langIndex]

    val selectLang: (Int) -> Unit = { idx ->
        langIndex = idx
        saveKeyboardIndex(ctx, idx)
        showLangPicker = false
    }

    val bg = MaterialTheme.colorScheme.surfaceVariant
    val keyBg = MaterialTheme.colorScheme.surface
    val dir = if (currentLang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides dir) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (symbolPage == -1) {
                LetterLayout(
                    lang = currentLang,
                    uppercase = uppercase,
                    onKey = onKeyPress,
                    onDelete = onDelete,
                    onShiftToggle = { uppercase = !uppercase },
                    onSym = { symbolPage = if (symbolPage < 3) symbolPage + 1 else -1 },
                    onSymLongPress = { symbolPage = PAGE_REVOLUTIONARY },
                    onDone = onDone,
                    onNewline = { onKeyPress('\n') },
                    langLabel = currentLang.label,
                    onLangTap = { showLangPicker = !showLangPicker },
                    showLangPicker = showLangPicker,
                    selectLang = selectLang,
                    keyBg = keyBg
                )
            } else if (symbolPage == 0) {
                NumberLayout(
                    onKey = onKeyPress,
                    onDelete = onDelete,
                    onAbc = { symbolPage = -1 },
                    onNext = {
                        if (symbolPage < 3) symbolPage++
                        else symbolPage = -1
                    },
                    langLabel = currentLang.label,
                    onLangTap = { showLangPicker = !showLangPicker },
                    showLangPicker = showLangPicker,
                    selectLang = selectLang,
                    keyBg = keyBg,
                    isRtl = currentLang.isRtl
                )
            } else {
                // Symbol pages 1..3 (Revolutionary, Anarchy, Queer)
                val pageIdx = symbolPage - 1 // 0-based into allSymbolPages[1..3]
                val page = allSymbolPages[symbolPage]
                val nextPage = if (symbolPage < 3) symbolPage + 1 else -1
                SymPageLayout(
                    page = page,
                    onKey = onKeyPress,
                    onDelete = onDelete,
                    onAbc = { symbolPage = -1 },
                    onNext = { symbolPage = nextPage },
                    langLabel = currentLang.label,
                    onLangTap = { showLangPicker = !showLangPicker },
                    showLangPicker = showLangPicker,
                    selectLang = selectLang,
                    keyBg = keyBg,
                    isRtl = currentLang.isRtl,
                    pageName = symbolPageLabels[symbolPage] ?: ""
                )
            }
        }
    }
}

// ─── Lang picker dropdown ───

@Composable
private fun LangPickerDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selectLang: (Int) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        keyboardLanguages.forEachIndexed { idx, kl ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, Modifier.height(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            kl.label + " (" + when (kl.code) {
                                "en" -> "English"
                                "de" -> "Deutsch"
                                "tr" -> "Türkçe"
                                "kmr" -> "Kurmancî"
                                "ckb" -> "سۆرانی"
                                else -> kl.code
                            } + ")",
                            fontSize = 14.sp
                        )
                    }
                },
                onClick = { selectLang(idx) }
            )
        }
    }
}

// ─── Letter layout ───

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LetterLayout(
    lang: KeyboardLayout,
    uppercase: Boolean,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onShiftToggle: () -> Unit,
    onSym: () -> Unit,
    onSymLongPress: () -> Unit,
    onDone: () -> Unit,
    onNewline: () -> Unit,
    langLabel: String,
    onLangTap: () -> Unit,
    showLangPicker: Boolean,
    selectLang: (Int) -> Unit,
    keyBg: Color
) {
    val shiftCaps = lang.uppercaseOverrides
    val longPress = lang.longPressMap

    // Row 1 — top letter row
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        lang.letterRows[0].forEach { c -> LetterKey(c, uppercase, onKey, keyBg, 1f, shiftCaps, longPress) }
    }

    // Row 2 — middle + backspace
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        Spacer(modifier = Modifier.weight(0.5f))
        lang.letterRows[1].forEach { c -> LetterKey(c, uppercase, onKey, keyBg, 1f, shiftCaps, longPress) }
        DeleteKey(weight = 1.5f, keyBg = keyBg, onDelete = onDelete)
    }

    // Row 3 — bottom + shift (left only) + newline (right)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        ActionKey(weight = 1.5f, selected = uppercase, keyBg = keyBg, onClick = onShiftToggle) {
            Icon(Icons.Default.KeyboardCapslock, "Shift", modifier = Modifier.height(18.dp),
                tint = if (uppercase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
        lang.letterRows[2].forEach { c -> LetterKey(c, uppercase, onKey, keyBg, 1f, shiftCaps, longPress) }
        ActionKey(weight = 1.5f, selected = false, keyBg = keyBg, onClick = onNewline) {
            Icon(Icons.Default.KeyboardReturn, "New line", modifier = Modifier.height(18.dp),
                tint = MaterialTheme.colorScheme.onSurface)
        }
    }

    // Row 4 — bottom bar
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        // 123+ key with long press to jump to Revolutionary
        Box(
            modifier = Modifier
                .weight(1f)
                .height(keyHeight.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(keyBg)
                .border(1.dp, keyBg.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .combinedClickable(
                    onClick = onSym,
                    onLongClick = onSymLongPress
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("123+", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (lang.isRtl) {
            LetterKey('.', uppercase = false, onKey, keyBg, 1.2f, longPressMap = mapOf('.' to '\u060C')) // tap . , hold-press ،
        } else {
            LetterKey('.', uppercase = false, onKey, keyBg, 1.2f, longPressMap = mapOf('.' to ',')) // tap . , hold-press ,
        }
        // Language button next to space
        Box(modifier = Modifier.weight(1.4f).height(keyHeight.dp), contentAlignment = Alignment.Center) {
            FlatActionKey(keyBg = keyBg, onClick = onLangTap) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(3.dp))
                    Text(langLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            LangPickerDropdown(showLangPicker, onDismiss = { onLangTap() }, selectLang)
        }
        if (lang.isRtl) {
            SpaceKey(keyBg) { onKey(' ') }
            LetterKey('؟', uppercase = false, onKey, keyBg, 1f, emptyMap())
        } else {
            SpaceKey(keyBg) { onKey(' ') }
            LetterKey('?', uppercase = false, onKey, keyBg, 1f, emptyMap())
        }
        ActionKey(weight = 1.2f, keyBg = keyBg.copy(alpha = 0.6f), onClick = onDone) {
            Icon(Icons.Default.KeyboardArrowDown, "Done", modifier = Modifier.height(20.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ─── Number/symbol layout (page 0) ───

@Composable
private fun NumberLayout(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onAbc: () -> Unit,
    onNext: () -> Unit,
    langLabel: String,
    onLangTap: () -> Unit,
    showLangPicker: Boolean,
    selectLang: (Int) -> Unit,
    keyBg: Color,
    isRtl: Boolean
) {
    // Number row
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        numbersPage[0].forEach { SymbolKey(it, onKey, keyBg) }
    }
    // Symbol row 1
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        numbersPage[1].forEach { SymbolKey(it, onKey, keyBg) }
    }
    // Symbol row 2 + backspace
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        numbersPage[2].forEach { SymbolKey(it, onKey, keyBg) }
        DeleteKey(weight = 1.5f, keyBg = keyBg, onDelete = onDelete)
    }
    // Bottom bar
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        ActionKey(weight = 1.2f, keyBg = keyBg, onClick = onAbc) {
            Text("ABC", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        ActionKey(weight = 1f, keyBg = keyBg, onClick = onNext) {
            Text("SYM\u00BB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isRtl) {
            SymbolKey('.', onKey, keyBg)
        } else {
            SymbolKey(',', onKey, keyBg)
        }
        Box(modifier = Modifier.weight(1.4f).height(keyHeight.dp), contentAlignment = Alignment.Center) {
            FlatActionKey(keyBg = keyBg, onClick = onLangTap) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(3.dp))
                    Text(langLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            LangPickerDropdown(showLangPicker, onDismiss = { onLangTap() }, selectLang)
        }
        SpaceKey(keyBg) { onKey(' ') }
        SymbolKey('.', onKey, keyBg)
    }
}

// ─── Symbol page layout (Revolutionary, Anarchy, Queer) ───

@Composable
private fun SymPageLayout(
    page: List<String>,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onAbc: () -> Unit,
    onNext: () -> Unit,
    langLabel: String,
    onLangTap: () -> Unit,
    showLangPicker: Boolean,
    selectLang: (Int) -> Unit,
    keyBg: Color,
    isRtl: Boolean,
    pageName: String
) {
    Text(
        pageName,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
    page.forEachIndexed { idx, row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            row.forEach { c ->
                if (c != '\u0000') SymPageKey(c, onKey, keyBg)
            }
            if (idx == page.lastIndex) {
                DeleteKey(weight = 1.5f, keyBg = keyBg, onDelete = onDelete)
            }
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(rowSpacing)) {
        ActionKey(weight = 1.2f, keyBg = keyBg, onClick = onAbc) {
            Text("ABC", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        ActionKey(weight = 1f, keyBg = keyBg, onClick = onNext) {
            Text("SYM\u00BB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Box(modifier = Modifier.weight(1.4f).height(keyHeight.dp), contentAlignment = Alignment.Center) {
            FlatActionKey(keyBg = keyBg, onClick = onLangTap) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, Modifier.height(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(3.dp))
                    Text(langLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            LangPickerDropdown(showLangPicker, onDismiss = { onLangTap() }, selectLang)
        }
        SpaceKey(keyBg) { onKey(' ') }
    }
}

@Composable
private fun RowScope.SymPageKey(c: Char, onKey: (Char) -> Unit, bg: Color) {
    Box(
        modifier = rowKeyModifier(1f, bg).clickable { onKey(c) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            c.toString(),
            fontSize = if (c.code > 0xFF) 18.sp else 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Key primitives ───

@Composable
private fun RowScope.rowKeyModifier(weight: Float, bg: Color): Modifier {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    return Modifier
        .weight(weight)
        .height(keyHeight.dp)
        .border(1.5.dp, borderColor, keyShape)
        .clip(keyShape)
        .background(bg)
}

@Composable
private fun flatKeyModifier(bg: Color): Modifier {
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    return Modifier
        .fillMaxWidth()
        .height(keyHeight.dp)
        .border(1.5.dp, borderColor, keyShape)
        .clip(keyShape)
        .background(bg)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.LetterKey(
    c: Char,
    uppercase: Boolean,
    onKey: (Char) -> Unit,
    bg: Color,
    weight: Float = 1f,
    upperMap: Map<Char, Char> = emptyMap(),
    longPressMap: Map<Char, Char> = emptyMap()
) {
    val display = if (uppercase) upperMap[c] ?: c.uppercaseChar() else c.lowercaseChar()
    val longPressChar = longPressMap[c]
    val isPKey = display == 'P' || display == 'p'
    Box(
        modifier = rowKeyModifier(weight, bg)
            .then(
                if (longPressChar != null) {
                    Modifier.combinedClickable(
                        onClick = { onKey(display) },
                        onLongClick = { onKey(longPressChar) }
                    )
                } else if (isPKey) {
                    Modifier.combinedClickable(
                        onClick = {
                            repeat(8) { onKey(display) }
                        },
                        onLongClick = {
                            repeat(8) { onKey(display) }
                        }
                    )
                } else {
                    Modifier.clickable { onKey(display) }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (longPressChar != null) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    longPressChar.toString(),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    display.toString(),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        } else {
            Text(
                display.toString(),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RowScope.SymbolKey(c: Char, onKey: (Char) -> Unit, bg: Color) {
    Box(
        modifier = rowKeyModifier(1f, bg).clickable { onKey(c) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            c.toString(),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RowScope.SpaceKey(bg: Color, onClick: () -> Unit) {
    Box(
        modifier = rowKeyModifier(4f, bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "space",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        )
    }
}

// ─── Delete key with long-press acceleration ───

@Composable
private fun RowScope.DeleteKey(
    weight: Float = 1.5f,
    keyBg: Color,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = rowKeyModifier(weight, keyBg)
            .pointerInput(Unit) {
                while (true) {
                    // Wait for a finger down
                    awaitPointerEventScope {
                        awaitFirstDown()
                    }
                    // Delete one character immediately
                    onDelete()

                    // Start repeat loop using compose's coroutine scope
                    val repeatJob = scope.launch {
                        var delayMs = 400L
                        while (isActive) {
                            delay(delayMs)
                            onDelete()
                            delayMs = (delayMs * 3L / 4L).coerceAtLeast(30L)
                        }
                    }

                    // Wait for the finger to lift
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                            if (event.changes.any { !it.pressed }) break
                        }
                    }

                    // Stop repeating
                    repeatJob.cancel()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Filled.Backspace, "Delete", modifier = Modifier.height(20.dp), tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RowScope.ActionKey(
    weight: Float = 1f,
    selected: Boolean = false,
    keyBg: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else keyBg
    Box(
        modifier = rowKeyModifier(weight, bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun FlatActionKey(
    selected: Boolean = false,
    keyBg: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else keyBg
    Box(
        modifier = flatKeyModifier(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
