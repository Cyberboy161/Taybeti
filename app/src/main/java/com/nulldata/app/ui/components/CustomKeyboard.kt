package com.nulldata.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomKeyboard(
    onKeyPress: (Char) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uppercase by remember { mutableStateOf(false) }
    var showSymbols by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showSymbols) {
                SymbolKeys(
                    onKey = { onKeyPress(it) },
                    onDelete = onDelete,
                    onAbc = { showSymbols = false }
                )
            } else {
                LettersLayout(
                    uppercase = uppercase,
                    onKey = { onKeyPress(it) },
                    onDelete = onDelete,
                    onShiftToggle = { uppercase = !uppercase },
                    onSymbols = { showSymbols = true },
                    onDone = onDone
                )
            }
        }
    }
}

@Composable
private fun LettersLayout(
    uppercase: Boolean,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onShiftToggle: () -> Unit,
    onSymbols: () -> Unit,
    onDone: () -> Unit
) {
    // Row 1: Q W E R T Y U I O P
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        "QWERTYUIOP".forEach { LetterKey(it, uppercase, onKey) }
    }
    // Row 2: A S D F G H J K L + backspace
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        "ASDFGHJKL".forEach { LetterKey(it, uppercase, onKey) }
        ActionKey(weight = 1f, onClick = onDelete) {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", modifier = Modifier.height(18.dp))
        }
    }
    // Row 3: shift + Z X C V B N M + shift
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ActionKey(weight = 1.2f, selected = uppercase, onClick = onShiftToggle) {
            Text("⇧", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        "ZXCVBNM".forEach { LetterKey(it, uppercase, onKey) }
        ActionKey(weight = 1.2f, selected = uppercase, onClick = onShiftToggle) {
            Text("⇧", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
    // Row 4: 123, comma, space, period, done
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ActionKey(weight = 1f, onClick = onSymbols) {
            Text("123", fontSize = 12.sp)
        }
        LetterKey(',', uppercase = false, onKey = onKey)
        SpaceKey { onKey(' ') }
        LetterKey('.', uppercase = false, onKey = onKey)
        ActionKey(weight = 1f, onClick = onDone) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Done", modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SymbolKeys(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onAbc: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        "1234567890".forEach { SymbolKey(it, onKey) }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        "!@#\$%^&*()".forEach { SymbolKey(it, onKey) }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        "-_=+[]{}".forEach { SymbolKey(it, onKey) }
        SymbolKey('<', onKey)
        SymbolKey('>', onKey)
        SymbolKey('|', onKey)
        SymbolKey('~', onKey)
        ActionKey(weight = 1f, onClick = onDelete) {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", modifier = Modifier.height(18.dp))
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ActionKey(weight = 1.2f, onClick = onAbc) {
            Text("ABC", fontSize = 12.sp)
        }
        SymbolKey(',', onKey)
        SpaceKey { onKey(' ') }
        SymbolKey('.', onKey)
    }
}

// ---- Key primitives ----

@Composable
private fun RowScope.LetterKey(c: Char, uppercase: Boolean, onKey: (Char) -> Unit) {
    TextButton(
        onClick = { onKey(if (uppercase) c.uppercaseChar() else c.lowercaseChar()) },
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = if (uppercase) c.uppercaseChar().toString() else c.lowercaseChar().toString(),
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun RowScope.SymbolKey(c: Char, onKey: (Char) -> Unit) {
    TextButton(
        onClick = { onKey(c) },
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .border(0.3.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = c.toString(),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun RowScope.SpaceKey(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .weight(3.5f)
            .height(44.dp)
            .border(0.3.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            "space",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun RowScope.ActionKey(
    weight: Float = 1.0f,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .weight(weight)
            .height(44.dp)
            .border(0.3.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
        shape = MaterialTheme.shapes.small
    ) {
        content()
    }
}
