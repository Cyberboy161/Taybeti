package com.taybeti.app.data.repository

import com.taybeti.app.data.dao.LoginDao
import com.taybeti.app.data.dao.NoteDao
import com.taybeti.app.data.entities.LoginInfo
import com.taybeti.app.data.entities.NoteEntity
import com.taybeti.app.security.CryptoUtils
import com.taybeti.app.security.SecureMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(
    private val loginDao: LoginDao,
    private val noteDao: NoteDao
) {
    companion object {
        const val LOGIN_CANARY = "Taybeti::login::check::ok"
        const val DECOY_CANARY = "Taybeti::decoy::check::ok"
        const val MAX_FAILED_ATTEMPTS = 5
    }

    suspend fun hasLoginInfo(): Boolean = withContext(Dispatchers.IO) {
        loginDao.get() != null
    }

    suspend fun setupMasterPassword(password: CharArray, decoyPassword: CharArray? = null): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val salt = CryptoUtils.generateSalt()
                val key = CryptoUtils.deriveKey(password, salt)
                SecureMemory.clear(password)

                val canary = LOGIN_CANARY.toByteArray(Charsets.UTF_8)
                val encResult = CryptoUtils.encrypt(canary, key)
                SecureMemory.clear(key)

                var decoySalt: ByteArray? = null
                var decoyIv: ByteArray? = null
                var decoyCiphertext: ByteArray? = null
                var decoyTag: ByteArray? = null
                var decoyEnabled = false

                if (decoyPassword != null && decoyPassword.isNotEmpty()) {
                    decoySalt = CryptoUtils.generateSalt()
                    val decoyKey = CryptoUtils.deriveKey(decoyPassword, decoySalt)
                    SecureMemory.clear(decoyPassword)

                    val decoyCanary = DECOY_CANARY.toByteArray(Charsets.UTF_8)
                    val decoyEncResult = CryptoUtils.encrypt(decoyCanary, decoyKey)
                    SecureMemory.clear(decoyKey)

                    decoyIv = decoyEncResult.iv
                    decoyCiphertext = decoyEncResult.ciphertext
                    decoyTag = decoyEncResult.tag
                    decoyEnabled = true
                }

                loginDao.insert(
                    LoginInfo(
                        loginSalt = salt,
                        loginIv = encResult.iv,
                        loginCiphertext = encResult.ciphertext,
                        loginTag = encResult.tag,
                        decoySalt = decoySalt,
                        decoyIv = decoyIv,
                        decoyCiphertext = decoyCiphertext,
                        decoyTag = decoyTag,
                        decoyEnabled = decoyEnabled
                    )
                )
                Result.success(Unit)
            } catch (e: Throwable) {
                Result.failure(e as Exception)
            }
        }

    suspend fun isDecoyEnabled(): Boolean = withContext(Dispatchers.IO) {
        loginDao.get()?.decoyEnabled == true
    }

    suspend fun setupDecoyPassword(decoyPassword: CharArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val info = loginDao.get() ?: return@withContext Result.failure(Exception("No account"))
                if (info.decoyEnabled) return@withContext Result.failure(Exception("Decoy already set"))

                val decoySalt = CryptoUtils.generateSalt()
                val decoyKey = CryptoUtils.deriveKey(decoyPassword, decoySalt)
                SecureMemory.clear(decoyPassword)

                val decoyCanary = DECOY_CANARY.toByteArray(Charsets.UTF_8)
                val decoyEncResult = CryptoUtils.encrypt(decoyCanary, decoyKey)
                SecureMemory.clear(decoyKey)

                loginDao.updateDecoy(decoySalt, decoyEncResult.iv, decoyEncResult.ciphertext, decoyEncResult.tag)
                Result.success(Unit)
            } catch (e: Throwable) {
                Result.failure(e as Exception)
            }
        }

    suspend fun changePassword(
        currentPassword: CharArray,
        newPassword: CharArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val info = loginDao.get() ?: return@withContext Result.failure(Exception("No account"))

            val currentKey = CryptoUtils.deriveKey(currentPassword, info.loginSalt)
            val canaryBytes = CryptoUtils.decrypt(
                info.loginCiphertext, currentKey, info.loginIv, info.loginTag
            )
            SecureMemory.clear(currentKey)

            if (String(canaryBytes, Charsets.UTF_8) != LOGIN_CANARY) {
                SecureMemory.clear(currentPassword)
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            val newSalt = CryptoUtils.generateSalt()
            val newKey = CryptoUtils.deriveKey(newPassword, newSalt)
            SecureMemory.clear(currentPassword)
            SecureMemory.clear(newPassword)

            val canary = LOGIN_CANARY.toByteArray(Charsets.UTF_8)
            val encResult = CryptoUtils.encrypt(canary, newKey)
            SecureMemory.clear(newKey)

            loginDao.updateLogin(newSalt, encResult.iv, encResult.ciphertext, encResult.tag)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e as Exception)
        }
    }

    data class LoginResult(
        val success: Boolean,
        val isDecoy: Boolean = false,
        val error: String? = null,
        val lockedOut: Boolean = false,
        val remainingMs: Long = 0L
    )

    suspend fun attemptLogin(password: CharArray, lockoutDelayMs: Long = 30_000L): LoginResult = withContext(Dispatchers.IO) {
        try {
        val info = loginDao.get() ?: return@withContext LoginResult(false, error = "No account found")

        // Lockout check
        var currentFails = info.failedAttempts
        if (currentFails >= MAX_FAILED_ATTEMPTS) {
            val elapsed = System.currentTimeMillis() - info.lastFailedTime
            if (elapsed < lockoutDelayMs) {
                return@withContext LoginResult(
                    false,
                    lockedOut = true,
                    remainingMs = lockoutDelayMs - elapsed
                )
            }
            loginDao.updateFailedAttempts(0, 0L)
            currentFails = 0
        }

        // Try real password
        try {
            val key = CryptoUtils.deriveKey(password, info.loginSalt)
            val canaryBytes = CryptoUtils.decrypt(
                info.loginCiphertext, key, info.loginIv, info.loginTag
            )
            SecureMemory.clear(key)

            if (String(canaryBytes, Charsets.UTF_8) == LOGIN_CANARY) {
                loginDao.updateFailedAttempts(0, 0L)
                SecureMemory.clear(password)
                return@withContext LoginResult(true)
            }
        } catch (_: Exception) { /* fall through to decoy check */ }

        // Try decoy password
        if (info.decoyEnabled && info.decoySalt != null) {
            try {
                val decoyKey = CryptoUtils.deriveKey(password, info.decoySalt)
                val decoyCanaryBytes = CryptoUtils.decrypt(
                    info.decoyCiphertext!!, decoyKey, info.decoyIv!!, info.decoyTag!!
                )
                SecureMemory.clear(decoyKey)

                if (String(decoyCanaryBytes, Charsets.UTF_8) == DECOY_CANARY) {
                    loginDao.updateFailedAttempts(0, 0L)
                    SecureMemory.clear(password)
                    return@withContext LoginResult(true, isDecoy = true)
                }
            } catch (_: Exception) { /* fall through */ }
        }

        SecureMemory.clear(password)
        val newFails = currentFails + 1
        loginDao.updateFailedAttempts(newFails, System.currentTimeMillis())
        LoginResult(false, error = "Invalid password")
        } catch (e: Throwable) {
            SecureMemory.clear(password)
            LoginResult(false, error = "Login error: ${e.message}")
        }
    }

    suspend fun encryptNoteContent(
        noteId: String,
        title: String,
        plaintext: ByteArray,
        passphrase: CharArray,
        attachmentsJson: String = "[]"
    ): Result<NoteEntity> = withContext(Dispatchers.IO) {
        try {
            val salt = CryptoUtils.generateSalt()
            val key = CryptoUtils.deriveKey(passphrase, salt)
            SecureMemory.clear(passphrase)

            val encResult = CryptoUtils.encrypt(plaintext, key)
            SecureMemory.clear(key)

            val existing = noteDao.getById(noteId)
            val now = System.currentTimeMillis()

            val note = NoteEntity(
                id = noteId,
                title = title,
                salt = salt,
                iv = encResult.iv,
                ciphertext = encResult.ciphertext,
                tag = encResult.tag,
                isEncrypted = true,
                isFavorite = existing?.isFavorite ?: false,
                isDeleted = false,
                isDecoyNote = existing?.isDecoyNote ?: false,
                createdDate = existing?.createdDate ?: now,
                modifiedDate = now,
                attachments = attachmentsJson
            )
            noteDao.insert(note)
            Result.success(note)
        } catch (e: Throwable) {
            Result.failure(e as Exception)
        }
    }

    suspend fun decryptNoteContent(note: NoteEntity, passphrase: CharArray): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                val key = CryptoUtils.deriveKey(passphrase, note.salt)
                SecureMemory.clear(passphrase)

                val plaintext = CryptoUtils.decrypt(note.ciphertext, key, note.iv, note.tag)
                SecureMemory.clear(key)
                Result.success(plaintext)
            } catch (e: Throwable) {
                SecureMemory.clear(passphrase)
                Result.failure(e as Exception)
            }
        }

    suspend fun encryptExport(passphrase: CharArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val notes = noteDao.getAllByDecoy(false) + noteDao.getAllByDecoy(true)
            val sb = StringBuilder()
            for (n in notes) {
                sb.appendLine("===NOTE===")
                sb.appendLine(n.id)
                sb.appendLine(n.title)
                sb.appendLine(java.util.Base64.getEncoder().encodeToString(n.salt))
                sb.appendLine(java.util.Base64.getEncoder().encodeToString(n.iv))
                sb.appendLine(java.util.Base64.getEncoder().encodeToString(n.ciphertext))
                sb.appendLine(java.util.Base64.getEncoder().encodeToString(n.tag))
                sb.appendLine(n.isFavorite.toString())
                sb.appendLine(n.isDecoyNote.toString())
                sb.appendLine(n.createdDate.toString())
                sb.appendLine(n.modifiedDate.toString())
            }

            val salt = CryptoUtils.generateSalt()
            val key = CryptoUtils.deriveKey(passphrase, salt)
            SecureMemory.clear(passphrase)

            val encResult = CryptoUtils.encrypt(sb.toString().toByteArray(Charsets.UTF_8), key)
            SecureMemory.clear(key)

            val b64 = java.util.Base64.getEncoder()
            val out = "${b64.encodeToString(salt)}::${b64.encodeToString(encResult.iv)}::${b64.encodeToString(encResult.tag)}::${b64.encodeToString(encResult.ciphertext)}"
            Result.success(out)
        } catch (e: Exception) {
            SecureMemory.clear(passphrase)
            Result.failure(e)
        }
    }

    suspend fun importEncrypted(blob: String, passphrase: CharArray): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val parts = blob.split("::")
                if (parts.size != 4) return@withContext Result.failure(Exception("Invalid blob format"))

                val b64 = java.util.Base64.getDecoder()
                val salt = b64.decode(parts[0])
                val iv = b64.decode(parts[1])
                val tag = b64.decode(parts[2])
                val ciphertext = b64.decode(parts[3])

                val key = CryptoUtils.deriveKey(passphrase, salt)
                SecureMemory.clear(passphrase)

                val plaintext = CryptoUtils.decrypt(ciphertext, key, iv, tag)
                SecureMemory.clear(key)

                val data = String(plaintext, Charsets.UTF_8)
                val lines = data.lines().filter { it.isNotBlank() }
                var count = 0

                var i = 0
                while (i < lines.size) {
                    if (lines[i] == "===NOTE===") {
                        i++
                        if (i + 8 >= lines.size) break
                        val note = NoteEntity(
                            id = lines[i++],
                            title = lines[i++],
                            salt = b64.decode(lines[i++]),
                            iv = b64.decode(lines[i++]),
                            ciphertext = b64.decode(lines[i++]),
                            tag = b64.decode(lines[i++]),
                            isEncrypted = true,
                            isFavorite = lines[i++].toBoolean(),
                            isDecoyNote = lines[i++].toBoolean(),
                            createdDate = lines[i++].toLong(),
                            modifiedDate = lines[i].toLong()
                        )
                        noteDao.insert(note)
                        count++
                    }
                    i++
                }
                Result.success(count)
            } catch (e: Throwable) {
                SecureMemory.clear(passphrase)
                Result.failure(e as Exception)
            }
        }
}
