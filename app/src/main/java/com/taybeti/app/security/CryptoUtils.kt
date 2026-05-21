package com.taybeti.app.security

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val GCM_TAG_LENGTH = 128
    private const val GCM_TAG_BYTES = GCM_TAG_LENGTH / 8 // 16 bytes
    private const val GCM_IV_LENGTH = 12
    private const val KEY_LENGTH = 32
    private const val ARGON2_ITERATIONS = 6
    private const val ARGON2_MEMORY_KIB = 65536  // 64 MB
    private const val ARGON2_PARALLELISM = 4
    private const val PADDED_SIZE = 1024
    private const val LENGTH_PREFIX_BYTES = 2

    private const val STREAM_BUFFER_SIZE = 8192
    private const val NAME_LEN_BYTES = 2
    private const val SALT_IV_LENGTH = 32 + GCM_IV_LENGTH // 44

    data class DecryptionResult(
        val originalFilename: String,
        val outputFile: File
    )

    private val secureRandom = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        secureRandom.nextBytes(salt)
        return salt
    }

    fun generateIv(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        return iv
    }

    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val argon2 = Argon2Kt()
        val passwordBytes = password.map { it.code.toByte() }.toByteArray()
        val passwordBuffer = ByteBuffer.allocateDirect(passwordBytes.size).apply {
            put(passwordBytes)
            flip()
        }
        val saltBuffer = ByteBuffer.allocateDirect(salt.size).apply {
            put(salt)
            flip()
        }
        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = passwordBuffer,
            salt = saltBuffer,
            tCostInIterations = ARGON2_ITERATIONS,
            mCostInKibibyte = ARGON2_MEMORY_KIB,
            parallelism = ARGON2_PARALLELISM,
            hashLengthInBytes = KEY_LENGTH
        )
        val key = result.rawHashAsByteArray()
        passwordBuffer.clear()
        val zero = ByteArray(passwordBytes.size)
        passwordBuffer.put(zero)
        passwordBytes.fill(0)
        return key
    }

    fun encrypt(plaintext: ByteArray, key: ByteArray): EncryptionResult {
        val padded = pad(plaintext)
        val iv = generateIv()
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val output = cipher.doFinal(padded)
        val ciphertext = output.copyOfRange(0, output.size - GCM_TAG_BYTES)
        val tag = output.copyOfRange(output.size - GCM_TAG_BYTES, output.size)
        return EncryptionResult(iv, ciphertext, tag)
    }

    fun decrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray, tag: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val combined = ciphertext + tag
        val padded = cipher.doFinal(combined)
        return unpad(padded)
    }

    private fun pad(plaintext: ByteArray): ByteArray {
        val origLen = plaintext.size
        require(origLen <= PADDED_SIZE - LENGTH_PREFIX_BYTES) {
            "Plaintext too large for padding (max ${PADDED_SIZE - LENGTH_PREFIX_BYTES} bytes)"
        }
        val padded = ByteArray(PADDED_SIZE)
        padded[0] = ((origLen shr 8) and 0xFF).toByte()
        padded[1] = (origLen and 0xFF).toByte()
        System.arraycopy(plaintext, 0, padded, LENGTH_PREFIX_BYTES, origLen)
        val randBytes = ByteArray(PADDED_SIZE - LENGTH_PREFIX_BYTES - origLen)
        secureRandom.nextBytes(randBytes)
        System.arraycopy(randBytes, 0, padded, LENGTH_PREFIX_BYTES + origLen, randBytes.size)
        return padded
    }

    private fun unpad(padded: ByteArray): ByteArray {
        if (padded.size < LENGTH_PREFIX_BYTES) return padded
        val lenHi = padded[0].toInt() and 0xFF
        val lenLo = padded[1].toInt() and 0xFF
        val claimedLen = (lenHi shl 8) or lenLo
        if (claimedLen > 0 &&
            claimedLen <= padded.size - LENGTH_PREFIX_BYTES &&
            padded.size == PADDED_SIZE
        ) {
            return padded.copyOfRange(LENGTH_PREFIX_BYTES, LENGTH_PREFIX_BYTES + claimedLen)
        }
        return padded
    }

    data class EncryptionResult(
        val iv: ByteArray,
        val ciphertext: ByteArray,
        val tag: ByteArray
    )

    /**
     * Encrypt a file using AES-256-GCM with Argon2id key derivation.
     * File format: [nameLen:2][nameBytes:variable][salt:32][iv:12][ciphertext...][tag:16]
     * Uses manual chunked cipher.update()/doFinal() — reliable for GCM.
     */
    fun encryptFile(
        inputFile: File,
        outputFile: File,
        passphrase: CharArray,
        originalFilename: String = inputFile.name,
        progressCallback: ((Long, Long) -> Unit)? = null
    ) {
        val nameBytes = originalFilename.toByteArray(Charsets.UTF_8)
        require(nameBytes.size <= 65535) { "Filename too long" }

        val salt = generateSalt()
        val key = deriveKey(passphrase, salt)
        SecureMemory.clear(passphrase)

        val iv = generateIv()
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val inputSize = inputFile.length()
        var processed = 0L

        inputFile.inputStream().use { input ->
            outputFile.outputStream().use { output ->
                // Write header: name length + name + salt + iv
                output.write((nameBytes.size shr 8) and 0xFF)
                output.write(nameBytes.size and 0xFF)
                output.write(nameBytes)
                output.write(salt)
                output.write(iv)

                // Stream encrypt in chunks
                val buffer = ByteArray(STREAM_BUFFER_SIZE)
                var bytesRead: Int
                var lastProgress = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) output.write(encrypted)
                    processed += bytesRead
                    val pct = if (inputSize > 0) processed * 100 / inputSize else 0
                    if (pct != lastProgress) {
                        lastProgress = pct
                        progressCallback?.invoke(processed, inputSize)
                    }
                }
                // Finalize: produces the 16-byte GCM authentication tag
                val tag = cipher.doFinal()
                output.write(tag)
            }
        }
    }

    /**
     * Decrypt a file encrypted with encryptFile().
     * File format: [nameLen:2][nameBytes:variable][salt:32][iv:12][ciphertext...][tag:16]
     * Reads all ciphertext into memory, then calls doFinal(ciphertext + tag) — matches working in-memory decrypt.
     * Returns the original filename stored in the header.
     */
    fun decryptFile(
        inputFile: File,
        outputFile: File,
        passphrase: CharArray,
        progressCallback: ((Long, Long) -> Unit)? = null
    ): DecryptionResult {
        val inputSize = inputFile.length()

        inputFile.inputStream().use { input ->
            // Read header: name length + name
            val nameLenHi = input.read()
            val nameLenLo = input.read()
            if (nameLenHi == -1 || nameLenLo == -1) throw java.io.EOFException("Invalid file header")
            val nameLen = (nameLenHi shl 8) or nameLenLo
            require(nameLen > 0 && nameLen < 1024) { "Invalid filename length: $nameLen" }
            val nameBytes = ByteArray(nameLen)
            input.readFully(nameBytes)
            val originalFilename = String(nameBytes, Charsets.UTF_8)

            // Read salt + iv
            val salt = ByteArray(32)
            val iv = ByteArray(GCM_IV_LENGTH)
            input.readFully(salt)
            input.readFully(iv)

            val headerSize = NAME_LEN_BYTES + nameLen + SALT_IV_LENGTH
            require(inputSize > headerSize + GCM_TAG_BYTES) { "File too small to be valid" }

            val key = deriveKey(passphrase, salt)
            SecureMemory.clear(passphrase)

            val ciphertextSize = (inputSize - headerSize - GCM_TAG_BYTES).toInt()
            val ciphertext = ByteArray(ciphertextSize)
            input.readFully(ciphertext)

            val tag = ByteArray(GCM_TAG_BYTES)
            input.readFully(tag)

            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            progressCallback?.invoke(inputSize - GCM_TAG_BYTES, inputSize)
            val padded = cipher.doFinal(ciphertext + tag)
            outputFile.writeBytes(padded)
            progressCallback?.invoke(inputSize, inputSize)

            return DecryptionResult(originalFilename, outputFile)
        }
    }

    private fun InputStream.readFully(buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val read = read(buffer, offset, buffer.size - offset)
            if (read == -1) throw java.io.EOFException()
            offset += read
        }
    }
}
