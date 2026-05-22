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
    val type: AttachmentType,
    val isIntegrated: Boolean = false,
    val encryptedPath: String = ""
) {
    enum class AttachmentType {
        IMAGE, AUDIO, VIDEO, DOCUMENT, OTHER
    }
}

object AttachmentManager {

    private const val ATTACHMENTS_DIR = "note_attachments"
    private const val ENCRYPTED_DIR = "encrypted_attachments"

    fun getAttachmentsDir(context: Context): File {
        val dir = File(context.filesDir, ATTACHMENTS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getEncryptedDir(context: Context): File {
        val dir = File(context.filesDir, ENCRYPTED_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getNoteDir(context: Context, noteId: String): File {
        val dir = File(getAttachmentsDir(context), noteId)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getEncryptedNoteDir(context: Context, noteId: String): File {
        val dir = File(getEncryptedDir(context), noteId)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun copyAttachment(
        context: Context,
        noteId: String,
        uri: Uri,
        contentResolver: android.content.ContentResolver,
        isIntegrated: Boolean = false
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
                type = attachmentType,
                isIntegrated = isIntegrated
            )

            Result.success(attachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun encryptAttachment(
        context: Context,
        noteId: String,
        attachment: NoteAttachment,
        key: ByteArray
    ): Result<NoteAttachment> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(attachment.storedPath)
            if (!sourceFile.exists()) return@withContext Result.failure(Exception("File not found"))

            val encryptedDir = getEncryptedNoteDir(context, noteId)
            val encryptedFile = File(encryptedDir, "${attachment.id}.enc")

            val plaintext = sourceFile.readBytes()
            val encResult = com.taybeti.app.security.CryptoUtils.encrypt(plaintext, key)

            val header = "TAYBEncV1".toByteArray()
            val ivLen = encResult.iv.size
            val tagLen = encResult.tag.size
            val totalSize = header.size + 4 + ivLen + 4 + tagLen + 4 + encResult.ciphertext.size

            encryptedDir.outputStream().use { out ->
                out.write(header)
                out.write(intToBytes(ivLen))
                out.write(encResult.iv)
                out.write(intToBytes(tagLen))
                out.write(encResult.tag)
                out.write(intToBytes(encResult.ciphertext.size))
                out.write(encResult.ciphertext)
            }

            sourceFile.delete()

            Result.success(attachment.copy(encryptedPath = encryptedFile.absolutePath, storedPath = ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decryptAttachment(
        context: Context,
        encryptedPath: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val encFile = File(encryptedPath)
            if (!encFile.exists()) return@withContext Result.failure(Exception("Encrypted file not found"))

            val data = encFile.readBytes()
            var offset = 0

            val header = "TAYBEncV1".toByteArray()
            if (!data.copyOfRange(0, header.size).contentEquals(header)) {
                return@withContext Result.failure(Exception("Invalid encrypted file"))
            }
            offset += header.size

            val ivLen = bytesToInt(data.copyOfRange(offset, offset + 4))
            offset += 4
            val iv = data.copyOfRange(offset, offset + ivLen)
            offset += ivLen

            val tagLen = bytesToInt(data.copyOfRange(offset, offset + 4))
            offset += 4
            val tag = data.copyOfRange(offset, offset + tagLen)
            offset += tagLen

            val cipherLen = bytesToInt(data.copyOfRange(offset, offset + 4))
            offset += 4
            val ciphertext = data.copyOfRange(offset, offset + cipherLen)

            Result.success(byteArrayOf(0))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    private fun bytesToInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) shl 24) or
               ((bytes[1].toInt() and 0xFF) shl 16) or
               ((bytes[2].toInt() and 0xFF) shl 8) or
               (bytes[3].toInt() and 0xFF)
    }

    suspend fun deleteNoteAttachments(context: Context, noteId: String) {
        withContext(Dispatchers.IO) {
            val noteDir = getNoteDir(context, noteId)
            if (noteDir.exists()) noteDir.deleteRecursively()
            val encDir = getEncryptedNoteDir(context, noteId)
            if (encDir.exists()) encDir.deleteRecursively()
        }
    }

    suspend fun deleteAttachment(context: Context, noteId: String, attachment: NoteAttachment) {
        withContext(Dispatchers.IO) {
            if (attachment.storedPath.isNotEmpty()) {
                val file = File(attachment.storedPath)
                if (file.exists()) file.delete()
            }
            if (attachment.encryptedPath.isNotEmpty()) {
                val file = File(attachment.encryptedPath)
                if (file.exists()) file.delete()
            }
        }
    }

    fun getAttachmentsList(json: String): List<NoteAttachment> {
        return try {
            if (json.isBlank() || json == "[]") return emptyList()
            val items = json.trimStart('[').trimEnd(']').split("},{")
            items.map { raw ->
                val cleaned = raw.trim('{', '}')
                val map = cleaned.split(",").associate { pair ->
                    val parts = pair.split(":", limit = 2)
                    if (parts.size == 2) parts[0].trim().trim('"') to parts[1].trim().trim('"')
                    else "" to ""
                }.filter { it.key.isNotEmpty() }
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
                    },
                    isIntegrated = map["isIntegrated"]?.toBoolean() ?: false,
                    encryptedPath = map["encryptedPath"] ?: ""
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun attachmentsToJson(attachments: List<NoteAttachment>): String {
        if (attachments.isEmpty()) return "[]"
        return attachments.joinToString(",", prefix = "[", postfix = "]") { att ->
            """{"id":"${att.id}","originalName":"${att.originalName.replace("\\", "\\\\")}","mimeType":"${att.mimeType}","size":${att.size},"storedPath":"${att.storedPath.replace("\\", "\\\\")}","type":"${att.type.name}","isIntegrated":${att.isIntegrated},"encryptedPath":"${att.encryptedPath.replace("\\", "\\\\")}"}"""
        }
    }
}
