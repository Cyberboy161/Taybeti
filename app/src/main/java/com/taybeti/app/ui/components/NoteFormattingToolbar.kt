package com.taybeti.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NoteFormattingToolbar(
    onInsertFormat: (String) -> Unit,
    onAddAttachment: (AttachmentType) -> Unit
) {
    var showAttachMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FormatButton(Icons.Default.FormatListBulleted, "Bullets") {
                onInsertFormat("\n• ")
            }
            FormatButton(Icons.Default.FormatListNumbered, "Numbers") {
                onInsertFormat("\n1. ")
            }
            FormatButton(Icons.Default.CheckBoxOutlineBlank, "Checkbox") {
                onInsertFormat("\n☐ ")
            }
            FormatButton(Icons.Default.FormatBold, "Bold") {
                onInsertFormat("**")
            }
            FormatButton(Icons.Default.FormatItalic, "Italic") {
                onInsertFormat("_")
            }
            FormatButton(Icons.Default.FormatUnderlined, "Underline") {
                onInsertFormat("__")
            }
            FormatButton(Icons.Default.Title, "Header") {
                onInsertFormat("\n## ")
            }
            FormatButton(Icons.Default.FormatQuote, "Quote") {
                onInsertFormat("\n> ")
            }
            FormatButton(Icons.Default.HorizontalRule, "Divider") {
                onInsertFormat("\n---\n")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .clickable { showAttachMenu = true }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Attach",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = showAttachMenu,
                onDismissRequest = { showAttachMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Image") },
                    onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.IMAGE) }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Audio") },
                    onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.AUDIO) }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.VideoFile, null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Video") },
                    onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.VIDEO) }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.List, null, modifier = Modifier.size(18.dp)) },
                    text = { Text("Document") },
                    onClick = { showAttachMenu = false; onAddAttachment(AttachmentType.DOCUMENT) }
                )
            }
        }
    }
}

@Composable
private fun FormatButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

enum class AttachmentType {
    IMAGE, AUDIO, VIDEO, DOCUMENT
}

fun AttachmentType.toMimePattern(): String = when (this) {
    AttachmentType.IMAGE -> "image/*"
    AttachmentType.AUDIO -> "audio/*"
    AttachmentType.VIDEO -> "video/*"
    AttachmentType.DOCUMENT -> "*/*"
}
