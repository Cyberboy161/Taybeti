package com.nulldata.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val keyboardState = LocalKeyboardState.current
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = androidx.compose.ui.text.TextRange(value.length)))
    }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    if (isFocused && keyboardState != null && !readOnly) {
        SideEffect {
            keyboardState.attach(
                onKey = { char ->
                    val newText = value + char
                    textFieldValue = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(newText.length))
                    onValueChange(newText)
                },
                onDel = {
                    if (value.isNotEmpty()) {
                        val newText = value.dropLast(1)
                        textFieldValue = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(newText.length))
                        onValueChange(newText)
                    }
                },
                onDone = { keyboardState.detach() }
            )
        }
    }

    val borderModifier = if (isFocused) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
    } else {
        Modifier
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newVal ->
            textFieldValue = newVal
            onValueChange(newVal.text)
        },
        modifier = modifier
            .then(borderModifier)
            .focusRequester(focusRequester)
            .onFocusChanged { fs ->
                isFocused = fs.isFocused
                if (!fs.isFocused) keyboardState?.detach()
            },
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly || keyboardState != null,
        trailingIcon = trailingIcon,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
