package com.nulldata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.nulldata.app.util.LocaleManager
import com.nulldata.app.util.LocalStrings

@Composable
fun LanguageScreen(
    onLanguageSelected: (String) -> Unit
) {
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            strings.appName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            strings.chooseLanguage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        LocaleManager.languages.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onLanguageSelected(language.code) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (language.flagEmoji != null) {
                    Text(language.flagEmoji, fontSize = 28.sp)
                } else {
                    KurdistanFlag(modifier = Modifier.size(48.dp, 32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    language.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun KurdistanFlag(modifier: Modifier = Modifier) {
    val red = Color(0xFFED2024)
    val sunGold = Color(0xFFFEBD11)
    val green = Color(0xFF278E43)

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(red)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(green)
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val sunSize = minOf(size.width, size.height) * 0.85f
            val outerR = sunSize / 2f
            val centerR = outerR * 0.25f
            val innerR = centerR
            val rayCount = 21

            // Draw sun rays — 21 triangles, wide base, sharp tip
            for (i in 0 until rayCount) {
                val angle = (2.0 * Math.PI * i / rayCount) - Math.PI / 2.0
                val halfSpan = Math.PI / rayCount * 0.92  // wide base

                val a1 = angle - halfSpan
                val a2 = angle + halfSpan

                val path = Path().apply {
                    moveTo(cx + (innerR * cos(a1)).toFloat(), cy + (innerR * sin(a1)).toFloat())
                    lineTo(cx + (outerR * cos(angle)).toFloat(), cy + (outerR * sin(angle)).toFloat())
                    lineTo(cx + (innerR * cos(a2)).toFloat(), cy + (innerR * sin(a2)).toFloat())
                    close()
                }
                drawPath(path, color = sunGold)
            }
            drawCircle(color = sunGold, radius = centerR)
        }
    }
}
