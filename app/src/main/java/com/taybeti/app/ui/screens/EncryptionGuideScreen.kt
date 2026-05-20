package com.taybeti.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taybeti.app.util.LocalStrings

enum class GuideStep(val index: Int, val emoji: String) {
    STEP1(0, "🔐"),
    STEP2(1, "🔒"),
    STEP3(2, "📤"),
    STEP4(3, "📥"),
    STEP5(4, "🛡️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionGuideScreen(onBack: () -> Unit) {
    val strings = LocalStrings.current
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.encryptionGuideTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalSteps) {
                    val isActive = i == currentStep
                    val isPast = i < currentStep
                    val color by animateColorAsState(
                        targetValue = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        },
                        animationSpec = tween(400),
                        label = "dotColor"
                    )
                    val size by animateFloatAsState(
                        targetValue = if (isActive) 12f else 8f,
                        animationSpec = tween(300),
                        label = "dotSize"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(size.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Step content with animated transitions
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { if (forward) it else -it }
                    ) + fadeIn(tween(300))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { if (forward) -it else it }
                    ) + fadeOut(tween(300)))
                },
                label = "stepTransition",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { step ->
                StepContent(
                    step = step,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.guideBack)
                }
                Text(
                    "${currentStep + 1} / $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Button(
                    onClick = {
                        if (currentStep < totalSteps - 1) currentStep++
                        else onBack()
                    }
                ) {
                    Text(if (currentStep < totalSteps - 1) strings.guideNext else strings.guideFinish)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (currentStep < totalSteps - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepContent(step: Int, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val infiniteTransition = rememberInfiniteTransition(label = "stepAnim")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Animated visual
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                0 -> AnimatedLockVisual(infiniteTransition)
                1 -> AnimatedEncryptVisual(infiniteTransition)
                2 -> AnimatedShareVisual(infiniteTransition)
                3 -> AnimatedDecryptVisual(infiniteTransition)
                4 -> AnimatedShieldVisual(infiniteTransition)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Title
        Text(
            text = when (step) {
                0 -> strings.guideStep1Title
                1 -> strings.guideStep2Title
                2 -> strings.guideStep3Title
                3 -> strings.guideStep4Title
                4 -> strings.guideStep5Title
                else -> ""
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Body
        Text(
            text = when (step) {
                0 -> strings.guideStep1Body
                1 -> strings.guideStep2Body
                2 -> strings.guideStep3Body
                3 -> strings.guideStep4Body
                4 -> strings.guideStep5Body
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnimatedLockVisual(transition: androidx.compose.animation.core.InfiniteTransition) {
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lockBounce"
    )
    val rotateAngle by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lockRotate"
    )

    val scale by animateFloatAsState(
        targetValue = 1f + (bounce * 0.08f),
        animationSpec = tween(300),
        label = "lockScale"
    )

    Canvas(
        modifier = Modifier
            .size(140.dp)
            .scale(scale)
            .rotate(rotateAngle * (1f - bounce))
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val bw = size.width * 0.6f
        val bh = size.height * 0.55f
        val arcR = size.width * 0.25f

        // Lock body
        val bodyPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    cx - bw / 2, cy - bh * 0.1f, cx + bw / 2, cy + bh * 0.85f, 12f, 12f
                )
            )
        }
        drawPath(bodyPath, color = Color(0xFF4F9CF7), style = Fill)

        // Keyhole
        drawCircle(
            color = Color(0xFF0A0A0C),
            radius = bw * 0.12f,
            center = Offset(cx, cy + bh * 0.15f)
        )
        drawRect(
            color = Color(0xFF0A0A0C),
            topLeft = Offset(cx - bw * 0.05f, cy + bh * 0.15f),
            size = Size(bw * 0.1f, bh * 0.25f)
        )

        // Shackle with animation
        val shackleAlpha = 1f - bounce * 0.6f
        val shacklePath = Path().apply {
            val top = cy - bh * 0.45f
            val left = cx - arcR
            val right = cx + arcR
            moveTo(left, cy - bh * 0.05f)
            lineTo(left, top + arcR * 0.5f)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left, top - arcR * 0.5f, right, top + arcR
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(right, cy - bh * 0.05f)
        }
        drawPath(
            path = shacklePath,
            color = Color(0xFF4F9CF7).copy(alpha = shackleAlpha),
            style = Stroke(width = bw * 0.16f)
        )
    }
}

@Composable
private fun AnimatedEncryptVisual(transition: androidx.compose.animation.core.InfiniteTransition) {
    val glow by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "encryptGlow"
    )
    val textShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textShift"
    )

    Canvas(modifier = Modifier.size(160.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2

        // Glow ring
        drawCircle(
            color = Color(0xFF4F9CF7).copy(alpha = glow * 0.3f),
            radius = size.width * 0.35f,
            center = Offset(cx, cy)
        )

        // Document with text
        val docLeft = cx - size.width * 0.25f
        val docTop = cy - size.height * 0.3f
        val docRight = cx + size.width * 0.25f
        val docBottom = cy + size.height * 0.3f

        drawRoundRect(
            color = Color(0xFF1E1E24),
            topLeft = Offset(docLeft, docTop),
            size = Size(size.width * 0.5f, size.height * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )

        // Animated text lines → cipher
        for (i in 0..3) {
            val shift = textShift * (1f - i * 0.2f)
            val alpha = 1f - (shift / 40f) * 0.8f
            drawLine(
                color = Color(0xFFE0E0E0).copy(alpha = alpha),
                start = Offset(docLeft + 16f, docTop + 24f + i * 20f + shift * 0.2f),
                end = Offset(docRight - 16f, docTop + 24f + i * 20f - shift * 0.2f),
                strokeWidth = 5f
            )
        }

        // Arrows
        drawLine(
            color = Color(0xFF4F9CF7).copy(alpha = 0.7f),
            start = Offset(docRight + 8f, cy),
            end = Offset(docRight + 24f, cy),
            strokeWidth = 2f
        )

        // Encrypted result
        val rLeft = docRight + 32f
        drawRoundRect(
            color = Color(0xFF4F9CF7).copy(alpha = 0.2f),
            topLeft = Offset(rLeft, docTop),
            size = Size(size.width * 0.2f, size.height * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        for (i in 0..3) {
            drawLine(
                color = Color(0xFF4F9CF7).copy(alpha = 0.8f),
                start = Offset(rLeft + 6f, docTop + 24f + i * 20f),
                end = Offset(rLeft + size.width * 0.18f, docTop + 24f + i * 20f),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
private fun AnimatedShareVisual(transition: androidx.compose.animation.core.InfiniteTransition) {
    val fly by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "flyAnim"
    )

    Canvas(modifier = Modifier.size(160.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2

        // Person 1 (left)
        drawCircle(Color(0xFF4F9CF7), radius = 16f, Offset(cx - 50f, cy + 20f))
        drawRoundRect(
            Color(0xFF4F9CF7),
            topLeft = Offset(cx - 60f, cy + 38f),
            size = Size(20f, 30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Person 2 (right)
        drawCircle(Color(0xFFA78BFA), radius = 16f, Offset(cx + 50f, cy + 20f))
        drawRoundRect(
            Color(0xFFA78BFA),
            topLeft = Offset(cx + 40f, cy + 38f),
            size = Size(20f, 30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Flying envelope
        val envX = cx - 40f + fly * 80f
        val envY = cy - 20f - fly * 30f
        val envSize = 30f
        drawRoundRect(
            Color(0xFFE0E0E0),
            topLeft = Offset(envX - envSize / 2, envY - envSize / 2),
            size = Size(envSize, envSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Envelope flap
        val flapPath = Path().apply {
            moveTo(envX - envSize / 2, envY - envSize / 2)
            lineTo(envX, envY)
            lineTo(envX + envSize / 2, envY - envSize / 2)
        }
        drawPath(flapPath, Color(0xFFC0C0C0), style = Fill)

        // Trail dots
        for (i in 1..3) {
            val dotAlpha = (fly * 3f - i).coerceIn(0f, 1f)
            drawCircle(
                Color(0xFF4F9CF7).copy(alpha = dotAlpha * 0.6f),
                radius = 4f,
                Offset(cx - 40f + (fly * 80f) / 4 * (4 - i), envY)
            )
        }
    }
}

@Composable
private fun AnimatedDecryptVisual(transition: androidx.compose.animation.core.InfiniteTransition) {
    val reveal by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "revealAnim"
    )
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Canvas(modifier = Modifier.size(160.dp)) {
        val cx = size.width / 2
        val cy = size.height / 2

        // Encrypted block (fades out)
        drawRoundRect(
            color = Color(0xFF1E1E24).copy(alpha = 1f - reveal),
            topLeft = Offset(cx - 45f, cy - 40f),
            size = Size(90f, 80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        for (i in 0..4) {
            drawLine(
                color = Color(0xFF4F9CF7).copy(alpha = (1f - reveal) * 0.7f),
                start = Offset(cx - 30f, cy - 20f + i * 14f),
                end = Offset(cx + 30f, cy - 20f + i * 14f),
                strokeWidth = 4f
            )
        }

        // Key (scales with pulse)
        val keyX = cx
        val keyY = cy + 10f
        drawCircle(
            color = Color(0xFFA78BFA),
            radius = 10f * pulse,
            center = Offset(keyX, keyY - 12f)
        )
        drawLine(
            Color(0xFFA78BFA),
            start = Offset(keyX, keyY - 2f),
            end = Offset(keyX, keyY + 24f),
            strokeWidth = 4f
        )
        drawLine(Color(0xFFA78BFA), Offset(keyX, keyY + 8f), Offset(keyX + 12f, keyY + 4f), 3f)
        drawLine(Color(0xFFA78BFA), Offset(keyX, keyY + 16f), Offset(keyX + 10f, keyY + 20f), 3f)

        // Decrypted text (fades in)
        drawRoundRect(
            color = Color(0xFF1E1E24).copy(alpha = reveal),
            topLeft = Offset(cx - 35f, cy - 40f),
            size = Size(70f, 30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        for (i in 0..1) {
            drawLine(
                color = Color(0xFFE0E0E0).copy(alpha = reveal * 0.8f),
                start = Offset(cx - 22f, cy - 22f + i * 12f),
                end = Offset(cx + 22f, cy - 22f + i * 12f),
                strokeWidth = 4f
            )
        }
        // Checkmark
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = reveal),
            start = Offset(cx - 8f, cy + 20f),
            end = Offset(cx, cy + 28f),
            strokeWidth = 3f
        )
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = reveal),
            start = Offset(cx, cy + 28f),
            end = Offset(cx + 14f, cy + 12f),
            strokeWidth = 3f
        )
    }
}

@Composable
private fun AnimatedShieldVisual(transition: androidx.compose.animation.core.InfiniteTransition) {
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldShimmer"
    )
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldPulse"
    )

    val strokeAlpha by animateFloatAsState(
        targetValue = 0.4f + shimmer * 0.6f,
        animationSpec = tween(400),
        label = "strokeAlpha"
    )

    Canvas(modifier = Modifier.size(150.dp).scale(pulse)) {
        val cx = size.width / 2
        val cy = size.height / 2
        val w = size.width * 0.4f
        val h = size.height * 0.5f

        // Shield shape
        val shieldPath = Path().apply {
            moveTo(cx, cy - h)
            lineTo(cx + w, cy - h * 0.4f)
            lineTo(cx + w * 0.8f, cy + h * 0.7f)
            lineTo(cx, cy + h)
            lineTo(cx - w * 0.8f, cy + h * 0.7f)
            lineTo(cx - w, cy - h * 0.4f)
            close()
        }
        drawPath(shieldPath, color = Color(0xFF4F9CF7).copy(alpha = 0.25f), style = Fill)
        drawPath(shieldPath, color = Color(0xFF4F9CF7).copy(alpha = strokeAlpha), style = Stroke(width = 3f))

        // Checkmark inside
        val checkSize = w * 0.4f
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = strokeAlpha),
            start = Offset(cx - checkSize * 0.6f, cy + checkSize * 0.1f),
            end = Offset(cx, cy + checkSize * 0.7f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color(0xFF4CAF50).copy(alpha = strokeAlpha),
            start = Offset(cx, cy + checkSize * 0.7f),
            end = Offset(cx + checkSize * 0.8f, cy - checkSize * 0.3f),
            strokeWidth = 4f
        )
    }
}
