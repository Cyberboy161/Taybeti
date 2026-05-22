package com.taybeti.app.security

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class NoteAttachment(
    val id: String = UUID.randomUUID().toString(),
    val originalName: String,
    val mimeType: String,
    val size: Long,
    val storedPath: String,
    val type: AttachmentType
) {
    enum class AttachmentType {
        IMAGE, AUDIO, VIDEO, DOCUMENT, OTHER
    }
}

object AttachmentManager {

    private const val ATTACHMENTS_DIR = "note_attachments"

    fun getAttachmentsDir(context: Context): File {
        val dir = File(context.filesDir, ATTACHMENTS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getNoteDir(context: Context, noteId: String): File {
        val dir = File(getAttachmentsDir(context), noteId)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun copyAttachment(
        context: Context,
        noteId: String,
        uri: Uri,
        contentResolver: android.content.ContentResolver
    ): Result<NoteAttachment> = withContext(Dispatchers.IO) {
        try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            var originalName = "attachment_${System.currentTimeMillis()}"
            var mimeType = "application/octet-stream"
            var size = 0L

            if (cursor != null && cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                originalName = if (nameIdx >= 0) cursor.getString(nameIdx) else originalName
                size = if (sizeIdx >= 0) cursor.getLong(sizeIdx) else size
                cursor.close()
            }

            val resolvedMimeType = contentResolver.getType(uri) ?: mimeType
            val extension = originalName.substringAfterLast('.', "")
            val attachmentType = when {
                resolvedMimeType.startsWith("image/") -> NoteAttachment.AttachmentType.IMAGE
                resolvedMimeType.startsWith("audio/") -> NoteAttachment.AttachmentType.AUDIO
                resolvedMimeType.startsWith("video/") -> NoteAttachment.AttachmentType.VIDEO
                else -> NoteAttachment.AttachmentType.DOCUMENT
            }

            val safeName = "${UUID.randomUUID()}${if (extension.isNotEmpty()) ".$extension" else ""}"
            val noteDir = getNoteDir(context, noteId)
            val destFile = File(noteDir, safeName)

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val attachment = NoteAttachment(
                originalName = originalName,
                mimeType = resolvedMimeType,
                size = destFile.length(),
                storedPath = destFile.absolutePath,
                type = attachmentType
            )

            Result.success(attachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNoteAttachments(context: Context, noteId: String) {
        withContext(Dispatchers.IO) {
            val noteDir = getNoteDir(context, noteId)
            if (noteDir.exists()) {
                noteDir.deleteRecursively()
            }
        }
    }

    suspend fun deleteAttachment(context: Context, noteId: String, attachmentId: String, storedPath: String) {
        withContext(Dispatchers.IO) {
            val file = File(storedPath)
            if (file.exists()) file.delete()
        }
    }

    fun getAttachmentsList(json: String): List<NoteAttachment> {
        return try {
            if (json.isBlank() || json == "[]") return emptyList()
            val items = json.trimStart('[').trimEnd(']').split("},{")
            items.map { raw ->
                val cleaned = raw.trim('{', '}')
                val map = cleaned.split(",").associate { pair ->
                    val (key, value) = pair.split(":", limit = 2)
                    key.trim().trim('"') to value.trim().trim('"')
                }
                NoteAttachment(
                    id = map["id"] ?: UUID.randomUUID().toString(),
                    originalName = map["originalName"] ?: "attachment",
                    mimeType = map["mimeType"] ?: "application/octet-stream",
                    size = map["size"]?.toLongOrNull() ?: 0L,
                    storedPath = map["storedPath"] ?: "",
                    type = try {
                        NoteAttachment.AttachmentType.valueOf(map["type"] ?: "OTHER")
                    } catch (_: Exception) {
                        NoteAttachment.AttachmentType.OTHER
                    }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun attachmentsToJson(attachments: List<NoteAttachment>): String {
        if (attachments.isEmpty()) return "[]"
        return attachments.joinToString(",", prefix = "[", postfix = "]") { att ->
            """{"id":"${att.id}","originalName":"${att.originalName}","mimeType":"${att.mimeType}","size":${att.size},"storedPath":"${att.storedPath}","type":"${att.type.name}"}"""
        }
    }
}
