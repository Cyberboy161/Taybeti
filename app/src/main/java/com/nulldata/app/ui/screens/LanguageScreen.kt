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
                    KurdistanFlag(modifier = Modifier.size(36.dp, 24.dp))
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

    Column(modifier = modifier.clip(RoundedCornerShape(4.dp))) {
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
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerR = size.minDimension / 2f
                val innerR = outerR * 0.5f
                val rayCount = 21
                val rayWidthAngle = Math.toRadians(360.0 / rayCount)

                // Draw sun rays
                for (i in 0 until rayCount) {
                    val angle = i * rayWidthAngle - Math.PI / 2
                    val halfGap = rayWidthAngle * 0.08
                    val a1 = angle + halfGap
                    val a2 = angle + rayWidthAngle - halfGap

                    val path = Path().apply {
                        moveTo(
                            cx + (innerR * cos(a1)).toFloat(),
                            cy + (innerR * sin(a1)).toFloat()
                        )
                        lineTo(
                            cx + (outerR * cos(a1)).toFloat(),
                            cy + (outerR * sin(a1)).toFloat()
                        )
                        lineTo(
                            cx + (outerR * cos(a2)).toFloat(),
                            cy + (outerR * sin(a2)).toFloat()
                        )
                        lineTo(
                            cx + (innerR * cos(a2)).toFloat(),
                            cy + (innerR * sin(a2)).toFloat()
                        )
                        close()
                    }
                    drawPath(path, color = sunGold)
                }

                // Draw center disc
                drawCircle(
                    color = sunGold,
                    radius = innerR * 0.95f
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(green)
        )
    }
}
