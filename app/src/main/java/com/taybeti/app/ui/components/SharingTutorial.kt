package com.taybeti.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TutorialStep(
    val icon: String,
    val title: String,
    val description: String,
    val graphic: @Composable () -> Unit = {}
)

private val sharingTutorialSteps = listOf(
    TutorialStep(
        icon = "✍️",
        title = "Write your message",
        description = "Type the secret message you want to share privately. It can be text, links, or anything you need to keep confidential.",
        graphic = {
            MessageGraphic("Hello, secret plan...", Color(0xFF1565C0))
        }
    ),
    TutorialStep(
        icon = "🔑",
        title = "Pick a passphrase",
        description = "Use a strong, unique passphrase. Tap 'Generate Key' for a random one. Share this passphrase in person only — never digitally.",
        graphic = {
            KeyGraphic()
        }
    ),
    TutorialStep(
        icon = "🔒",
        title = "Encrypt & disguise",
        description = "Tap Encrypt. Your message is encrypted with AES-256-GCM. Then choose a platform (YouTube or Instagram) to disguise the encrypted text as a normal-looking link.",
        graphic = {
            DisguiseGraphic()
        }
    ),
    TutorialStep(
        icon = "📤",
        title = "Share with anyone",
        description = "Copy the disguised link and send it via SMS, WhatsApp, Signal, email — any platform. It looks like a harmless YouTube/Instagram link. Nobody suspects a thing.",
        graphic = {
            ShareGraphic()
        }
    ),
    TutorialStep(
        icon = "🔓",
        title = "They decode & decrypt",
        description = "Your friend pastes the link into the Decoy tab, picks the platform, taps Decode to extract the encrypted blob. Then they switch to Decrypt, enter the shared passphrase, and read the message.",
        graphic = {
            DecodeGraphic()
        }
    )
)

@Composable
fun SharingTutorialButton(
    modifier: Modifier = Modifier
) {
    var showTutorial by remember { mutableIntStateOf(0) }

    Button(
        onClick = { showTutorial = 1 },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("How sharing works", fontSize = 13.sp)
    }

    if (showTutorial > 0) {
        SharingTutorialDialog(
            onDismiss = { showTutorial = 0 }
        )
    }
}

@Composable
fun SharingTutorialDialog(
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = sharingTutorialSteps.size
    val progress by animateFloatAsState(
        targetValue = (step + 1).toFloat() / totalSteps,
        animationSpec = tween(400)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "How Covert Sharing Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                }
            }

            // Progress bar
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated step content
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
                val s = sharingTutorialSteps[currentStep]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Step indicator
                    Text(
                        "Step ${currentStep + 1} of $totalSteps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Icon
                    Text(s.icon, fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        s.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        s.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Graphic
                    Card(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            s.graphic()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 0) {
                    Button(onClick = { step-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (step < totalSteps - 1) {
                    Button(onClick = { step++ }) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
                    }
                } else {
                    Button(onClick = onDismiss) {
                        Text("Got it!")
                    }
                }
            }

            // Dots indicator
            Spacer(modifier = Modifier.height(12.dp))
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
    }
}

// ─── Step Graphics ───

@Composable
private fun MessageGraphic(text: String, color: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun KeyGraphic() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Key icon via Canvas
        Canvas(modifier = Modifier.size(32.dp)) {
            val cx = size.width / 2
            drawCircle(Color(0xFF1565C0), radius = 10f, center = Offset(cx, 16f))
            drawRect(Color(0xFF1565C0), Offset(cx - 3f, 0f), Size(6f, 16f))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("•".repeat(14), color = Color(0xFF1565C0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DisguiseGraphic() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        // Encrypted blob
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFD32F2F).copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("AES-256 encrypted", fontSize = 11.sp, color = Color(0xFFD32F2F))
        }
        Text("→", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        // Disguised link
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("youtube.com/watch?...", fontSize = 11.sp, color = Color(0xFF2E7D32))
        }
    }
}

@Composable
private fun ShareGraphic() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("💬 SMS", "📱 WhatsApp", "✉️ Email", "🔐 Signal").forEach { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(label, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DecodeGraphic() {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Paste link", fontSize = 11.sp, color = Color(0xFF2E7D32))
        }
        Text("→", fontSize = 14.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF57C00).copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Decode", fontSize = 11.sp, color = Color(0xFFF57C00))
        }
        Text("→", fontSize = 14.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1565C0).copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Decrypt with key", fontSize = 11.sp, color = Color(0xFF1565C0))
        }
    }
}
