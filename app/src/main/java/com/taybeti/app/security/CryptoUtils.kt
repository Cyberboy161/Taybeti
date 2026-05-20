package com.taybeti.app.security

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val KEY_LENGTH = 32
    private const val ARGON2_ITERATIONS = 6
    private const val ARGON2_MEMORY_KIB = 65536  // 64 MB
    private const val ARGON2_PARALLELISM = 4
    private const val PADDED_SIZE = 1024
    private const val LENGTH_PREFIX_BYTES = 2

    private const val STREAM_BUFFER_SIZE = 8192

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
        val tagSize = GCM_TAG_LENGTH / 8
        val ciphertext = output.copyOfRange(0, output.size - tagSize)
        val tag = output.copyOfRange(output.size - tagSize, output.size)
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

    fun encryptFile(
        inputFile: File,
        outputFile: File,
        passphrase: CharArray,
        progressCallback: ((Long, Long) -> Unit)? = null
    ) {
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
                output.write(salt)
                output.write(iv)

                CipherOutputStream(output, cipher).use { cipherOut ->
                    val buffer = ByteArray(STREAM_BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        cipherOut.write(buffer, 0, bytesRead)
                        processed += bytesRead
                        progressCallback?.invoke(processed, inputSize)
                    }
                }
            }
        }
    }

    fun decryptFile(
        inputFile: File,
        outputFile: File,
        passphrase: CharArray,
        progressCallback: ((Long, Long) -> Unit)? = null
    ) {
        val inputSize = inputFile.length()

        inputFile.inputStream().use { input ->
            val salt = ByteArray(32)
            val iv = ByteArray(12)
            input.readFully(salt)
            input.readFully(iv)

            val key = deriveKey(passphrase, salt)
            SecureMemory.clear(passphrase)

            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            var processed: Long = 44L

            CipherInputStream(input, cipher).use { cipherIn ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(STREAM_BUFFER_SIZE)
                    var bytesRead: Int
                    while (cipherIn.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        processed += bytesRead
                        progressCallback?.invoke(processed, inputSize)
                    }
                }
            }
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
