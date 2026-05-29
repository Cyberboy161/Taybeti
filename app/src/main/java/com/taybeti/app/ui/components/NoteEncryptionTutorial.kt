package com.taybeti.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.taybeti.app.util.InlineTranslations
import com.taybeti.app.util.LocalLanguageCode

@Composable
fun NoteEncryptionTutorialDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocalLanguageCode.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 6
    val progress by animateFloatAsState(
        targetValue = (step + 1).toFloat() / totalSteps,
        animationSpec = tween(400)
    )

    var userMessage by remember { mutableStateOf("This is my secret message!") }
    var userPassphrase by remember { mutableStateOf("") }
    var confirmPassphrase by remember { mutableStateOf("") }
    var showPassphrase by remember { mutableStateOf(false) }
    var isEncrypting by remember { mutableStateOf(false) }
    var encryptedResult by remember { mutableStateOf("") }
    var decryptPassphrase by remember { mutableStateOf("") }
    var decryptedResult by remember { mutableStateOf("") }
    var isDecrypting by remember { mutableStateOf(false) }
    var passphrasesMatch by remember { mutableStateOf<Boolean?>(null) }

    var activeField by remember { mutableStateOf<String?>(null) }
    val isKeyboardVisible = activeField != null

    LaunchedEffect(userPassphrase, confirmPassphrase) {
        if (userPassphrase.isNotEmpty() && confirmPassphrase.isNotEmpty()) {
            passphrasesMatch = userPassphrase == confirmPassphrase
        } else {
            passphrasesMatch = null
        }
    }

    fun simulateEncrypt() {
        if (userMessage.isBlank() || userPassphrase.length < 4) return
        isEncrypting = true
        encryptedResult = ""
        val fakeEncrypted = "AES256GCM:${userPassphrase.hashCode().toString(16)}:${System.currentTimeMillis().toString(16)}:" +
            userMessage.map { c -> ((c.code + userPassphrase.hashCode()) % 95 + 32).toChar() }.joinToString("") +
            ":${userMessage.length}"
        encryptedResult = fakeEncrypted
        isEncrypting = false
    }

    fun simulateDecrypt() {
        if (decryptPassphrase.isBlank()) return
        isDecrypting = true
        decryptedResult = ""
        if (decryptPassphrase == userPassphrase) {
            decryptedResult = userMessage
        } else {
            decryptedResult = InlineTranslations.t("tutorial_error", lang)
        }
        isDecrypting = false
    }

    fun handleKeyPress(key: Char) {
        when (activeField) {
            "message" -> userMessage += key
            "passphrase" -> userPassphrase += key
            "confirmPassphrase" -> confirmPassphrase += key
            "decryptPassphrase" -> decryptPassphrase += key
        }
    }

    fun handleDelete() {
        when (activeField) {
            "message" -> if (userMessage.isNotEmpty()) userMessage = userMessage.dropLast(1)
            "passphrase" -> if (userPassphrase.isNotEmpty()) userPassphrase = userPassphrase.dropLast(1)
            "confirmPassphrase" -> if (confirmPassphrase.isNotEmpty()) confirmPassphrase = confirmPassphrase.dropLast(1)
            "decryptPassphrase" -> if (decryptPassphrase.isNotEmpty()) decryptPassphrase = decryptPassphrase.dropLast(1)
        }
    }

    fun handleCopy() {
        val textToCopy = when (activeField) {
            "message" -> userMessage
            "passphrase" -> userPassphrase
            "confirmPassphrase" -> confirmPassphrase
            "decryptPassphrase" -> decryptPassphrase
            else -> return
        }
        if (textToCopy.isNotEmpty()) {
            clipboard.setPrimaryClip(ClipData.newPlainText("tutorial", textToCopy))
        }
    }

    fun handlePaste() {
        if (!clipboard.hasPrimaryClip()) return
        val clip = clipboard.primaryClip ?: return
        if (clip.itemCount > 0) {
            val pasteText = clip.getItemAt(0).text.toString()
            when (activeField) {
                "message" -> userMessage += pasteText
                "passphrase" -> userPassphrase += pasteText
                "confirmPassphrase" -> confirmPassphrase += pasteText
                "decryptPassphrase" -> decryptPassphrase += pasteText
            }
        }
    }

    val needsKeyboard = step == 0 || step == 1 || step == 4

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            InlineTranslations.t("how_works", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onDismiss) {
                                Text(InlineTranslations.t("skip", lang), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "${InlineTranslations.t("step", lang)} ${step + 1} / $totalSteps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Navigation buttons — above content so they're always visible
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (step > 0) {
                            Button(
                                onClick = { step--; activeField = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(InlineTranslations.t("back", lang))
                            }
                        } else { Spacer(Modifier) }
                        Button(onClick = {
                            if (step < totalSteps - 1) step++ else onDismiss()
                        }, enabled = step < totalSteps - 1 || userPassphrase.isNotEmpty()) {
                            Text(if (step < totalSteps - 1) InlineTranslations.t("next", lang) else InlineTranslations.t("done", lang))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "step"
                    ) { currentStep ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            when (currentStep) {
                                0 -> Step1_WriteNote(
                                    userMessage,
                                    isActive = activeField == "message",
                                    onActivate = { activeField = "message" },
                                    onCopy = ::handleCopy,
                                    onPaste = ::handlePaste,
                                    lang = lang
                                )
                                1 -> Step2_CreatePassphrase(
                                    userPassphrase,
                                    confirmPassphrase,
                                    showPassphrase,
                                    passphrasesMatch,
                                    activeField,
                                    onActivatePassphrase = { activeField = "passphrase" },
                                    onActivateConfirm = { activeField = "confirmPassphrase" },
                                    onToggleShow = { showPassphrase = !showPassphrase },
                                    onCopy = ::handleCopy,
                                    onPaste = ::handlePaste,
                                    lang = lang
                                )
                                2 -> Step3_Encrypt(
                                    userMessage,
                                    userPassphrase,
                                    isEncrypting,
                                    onEncrypt = { simulateEncrypt() },
                                    lang = lang
                                )
                                3 -> Step4_EncryptedResult(encryptedResult, lang = lang)
                                4 -> Step5_Decrypt(
                                    encryptedResult,
                                    decryptPassphrase,
                                    decryptedResult,
                                    isDecrypting,
                                    isActive = activeField == "decryptPassphrase",
                                    onActivate = { activeField = "decryptPassphrase" },
                                    onDecrypt = { simulateDecrypt() },
                                    onCopy = ::handleCopy,
                                    onPaste = ::handlePaste,
                                    lang = lang
                                )
                                5 -> Step6_Done(lang = lang)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Progress dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(totalSteps) { i ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (i == step) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == step) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }

                if (needsKeyboard) {
                    AnimatedVisibility(
                        visible = isKeyboardVisible,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        CustomKeyboard(
                            onKeyPress = { handleKeyPress(it) },
                            onDelete = { handleDelete() },
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
}

@Composable
private fun TutorialTextField(
    label: String,
    value: String,
    isActive: Boolean,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onActivate: () -> Unit,
    onTogglePassword: (() -> Unit)? = null,
    onCopy: () -> Unit = {},
    onPaste: () -> Unit = {},
    isError: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            if (isActive) {
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    IconButton(onClick = onPaste, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentPaste, "Paste", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
                .border(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else if (isActive) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onActivate)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isEmpty()) placeholder else if (isPassword && !showPassword) "•".repeat(value.length) else value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface
                )
                if (onTogglePassword != null) {
                    IconButton(onClick = onTogglePassword, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1_WriteNote(
    message: String,
    isActive: Boolean,
    onActivate: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    lang: String = "en"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("1", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("write_note", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("try_it", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TutorialTextField(
                    label = InlineTranslations.t("your_note", lang),
                    value = message,
                    isActive = isActive,
                    placeholder = InlineTranslations.t("tap_type_secret", lang),
                    onActivate = onActivate,
                    onCopy = onCopy,
                    onPaste = onPaste
                )
            }
        }
    }
}

@Composable
private fun Step2_CreatePassphrase(
    passphrase: String,
    confirmPassphrase: String,
    showPassphrase: Boolean,
    passphrasesMatch: Boolean?,
    activeField: String?,
    onActivatePassphrase: () -> Unit,
    onActivateConfirm: () -> Unit,
    onToggleShow: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    lang: String = "en"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("2", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("create_passphrase", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("passphrase_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(InlineTranslations.t("passphrase", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TutorialTextField(
                    label = "",
                    value = passphrase,
                    isActive = activeField == "passphrase",
                    placeholder = InlineTranslations.t("tap_enter_passphrase", lang),
                    isPassword = true,
                    showPassword = showPassphrase,
                    onActivate = onActivatePassphrase,
                    onTogglePassword = onToggleShow,
                    onCopy = onCopy,
                    onPaste = onPaste
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(InlineTranslations.t("confirm_pass", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TutorialTextField(
                    label = "",
                    value = confirmPassphrase,
                    isActive = activeField == "confirmPassphrase",
                    placeholder = InlineTranslations.t("tap_reenter_pass", lang),
                    isPassword = true,
                    showPassword = showPassphrase,
                    onActivate = onActivateConfirm,
                    isError = passphrasesMatch == false,
                    onCopy = onCopy,
                    onPaste = onPaste
                )

                if (passphrasesMatch != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (passphrasesMatch) Icons.Default.Check else Icons.Default.Close,
                            null,
                            tint = if (passphrasesMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (passphrasesMatch) InlineTranslations.t("pass_match", lang) else InlineTranslations.t("pass_no_match", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (passphrasesMatch) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            InlineTranslations.t("share_tip", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3_Encrypt(
    message: String,
    passphrase: String,
    isEncrypting: Boolean,
    onEncrypt: () -> Unit,
    lang: String = "en"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("3", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("encrypt_your_note_title", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("encrypt_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(InlineTranslations.t("plaintext_lbl", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.LockOpen, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("↓", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onEncrypt,
                    enabled = !isEncrypting && message.isNotBlank() && passphrase.length >= 4,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEncrypting) InlineTranslations.t("encrypting_btn", lang) else InlineTranslations.t("encrypt_note_btn", lang))
                }
            }
        }
    }
}

@Composable
private fun Step4_EncryptedResult(encryptedResult: String, lang: String = "en") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("4", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("enc_result_title", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("enc_result_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(InlineTranslations.t("encrypted_label", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    if (encryptedResult.isNotEmpty()) {
                        Text(
                            encryptedResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            InlineTranslations.t("no_enc_data", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedEncryptionGraphic()
            }
        }
    }
}

@Composable
private fun Step5_Decrypt(
    encryptedData: String,
    decryptPassphrase: String,
    decryptedResult: String,
    isDecrypting: Boolean,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDecrypt: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    lang: String = "en"
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("5", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("decrypt_read_back_title", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("decrypt_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(InlineTranslations.t("enc_data", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Text(
                        encryptedData.take(60) + if (encryptedData.length > 60) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                TutorialTextField(
                    label = InlineTranslations.t("enter_passphrase_decrypt", lang),
                    value = decryptPassphrase,
                    isActive = isActive,
                    placeholder = InlineTranslations.t("tap_enter_passphrase_only", lang),
                    isPassword = true,
                    onActivate = onActivate,
                    onCopy = onCopy,
                    onPaste = onPaste
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDecrypt,
                    enabled = !isDecrypting && decryptPassphrase.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LockOpen, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isDecrypting) InlineTranslations.t("decrypting_btn", lang) else InlineTranslations.t("decrypt_btn", lang))
                }

                if (decryptedResult.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val isSuccess = !decryptedResult.startsWith("ERROR")
                    Text(InlineTranslations.t("result_lbl", lang), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSuccess) Color(0xFF2E7D32).copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            decryptedResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step6_Done(lang: String = "en") {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .scale(scale.value),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            InlineTranslations.t("you_ready", lang),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            InlineTranslations.t("final_desc", lang),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(InlineTranslations.t("key_take", lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    InlineTranslations.t("key_each_note", lang),
                    InlineTranslations.t("key_never_share", lang),
                    InlineTranslations.t("key_lost_forever", lang),
                    InlineTranslations.t("key_data_unreadable", lang)
                ).forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", fontWeight = FontWeight.Bold)
                        Text(tip, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun AnimatedEncryptionGraphic() {
    val progress = remember { Animatable(0f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    LaunchedEffect(Unit) {
        while (true) {
            progress.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
            progress.animateTo(0f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
        }
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        val y = size.height / 2
        val startX = 20f
        val endX = size.width - 20f
        val totalWidth = endX - startX

        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2E7D32), primaryColor),
                startX = startX,
                endX = startX + totalWidth * progress.value
            ),
            start = Offset(startX, y),
            end = Offset(startX + totalWidth * progress.value, y),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = primaryColor,
            radius = 6f,
            center = Offset(startX + totalWidth * progress.value, y)
        )
    }
}
