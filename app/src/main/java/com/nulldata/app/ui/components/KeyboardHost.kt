package com.nulldata.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

val LocalKeyboardState = compositionLocalOf<KeyboardState?> { null }

@Stable
class KeyboardState {
    var onKeyPress by mutableStateOf<((Char) -> Unit)?>(null)
        private set
    var onDelete by mutableStateOf<(() -> Unit)?>(null)
        private set
    var onDone by mutableStateOf<(() -> Unit)?>(null)
        private set
    var isVisible by mutableStateOf(false)
        private set

    fun attach(onKey: (Char) -> Unit, onDel: () -> Unit, onDone: () -> Unit) {
        onKeyPress = onKey
        onDelete = onDel
        this.onDone = onDone
        isVisible = true
    }

    fun show() {
        isVisible = true
    }

    fun detach() {
        onKeyPress = null
        onDelete = null
        onDone = null
        isVisible = false
    }
}

@Composable
fun KeyboardHost(content: @Composable () -> Unit) {
    val state = remember { KeyboardState() }

    CompositionLocalProvider(LocalKeyboardState provides state) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            AnimatedVisibility(
                visible = state.isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                CustomKeyboard(
                    onKeyPress = { state.onKeyPress?.invoke(it) },
                    onDelete = { state.onDelete?.invoke() },
                    onDone = { state.onDone?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                )
            }
        }
    }
}
