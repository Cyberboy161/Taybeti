package com.nulldata.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    val keyboardState = LocalKeyboardState.current
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = androidx.compose.ui.text.TextRange(value.length)))
    }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (isFocused && keyboardState != null) {
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
                onDone = {
                    onDone()
                    keyboardState.detach()
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardState?.detach()
        }
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 4.dp)
    )

    val borderModifier = if (isFocused) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .focusRequester(focusRequester)
            .onFocusChanged { fs ->
                isFocused = fs.isFocused
            },
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newVal ->
                textFieldValue = newVal
                onValueChange(newVal.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            visualTransformation = PasswordVisualTransformation(),
            readOnly = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        "●".repeat(8),
                        style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
                    )
                }
                innerTextField()
            }
        )
    }
}
