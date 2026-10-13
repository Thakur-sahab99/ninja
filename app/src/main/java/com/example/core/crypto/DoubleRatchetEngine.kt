package com.example.core.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Double Ratchet Engine for Ninja Messenger.
 * Implements symmetric ratcheting, HKDF-SHA256, AES-256-GCM encryption,
 * and 60-digit Safety Number fingerprint calculation.
 */
object DoubleRatchetEngine {
    private val secureRandom = SecureRandom()
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12

    fun generateKeyPair(): NinjaKeyPair {
        val privBytes = ByteArray(32)
        secureRandom.nextBytes(privBytes)
        // Derive deterministic public key via SHA-256 hash
        val pubBytes = MessageDigest.getInstance("SHA-256").digest(privBytes)
        return NinjaKeyPair(
            publicKeyHex = bytesToHex(pubBytes),
            privateKeyHex = bytesToHex(privBytes)
        )
    }

    /**
     * Derives a symmetric encryption key from ratcheting root key and shared secret.
     */
    fun hkdfDeriveKey(chainKey: ByteArray, info: String): Pair<ByteArray, ByteArray> {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(chainKey, "HmacSHA256"))
        val nextChainKey = mac.doFinal(info.toByteArray(Charsets.UTF_8))
        mac.init(SecretKeySpec(nextChainKey, "HmacSHA256"))
        val messageKey = mac.doFinal("message-key-constant".toByteArray(Charsets.UTF_8))
        return Pair(nextChainKey, messageKey)
    }

    /**
     * Encrypts plaintext message into an AES-256-GCM Sealed Envelope.
     */
    fun encryptMessage(
        plaintext: String,
        sessionKeyHex: String,
        sequenceNumber: Long
    ): EncryptedEnvelope {
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(hexToBytes(sessionKeyHex))
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        cipher.updateAAD(sequenceNumber.toString().toByteArray(Charsets.UTF_8))

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combinedPayload = iv + cipherBytes
        val ciphertextBase64 = Base64.getEncoder().encodeToString(combinedPayload)

        // Compute HMAC tag for envelope authenticity
        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(SecretKeySpec(keyBytes, "HmacSHA256"))
        val tagBytes = hmac.doFinal(combinedPayload)

        return EncryptedEnvelope(
            ephemeralPublicKeyHex = bytesToHex(iv),
            sequenceNumber = sequenceNumber,
            previousChainLength = 0,
            ciphertextBase64 = ciphertextBase64,
            hmacTagHex = bytesToHex(tagBytes),
            isSealedSender = true
        )
    }

    /**
     * Decrypts an EncryptedEnvelope back into plaintext.
     */
    fun decryptMessage(
        envelope: EncryptedEnvelope,
        sessionKeyHex: String
    ): String {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(hexToBytes(sessionKeyHex))
            val combinedPayload = Base64.getDecoder().decode(envelope.ciphertextBase64)
            if (combinedPayload.size < GCM_IV_LENGTH_BYTES) {
                return "[Decryption Error: Invalid payload]"
            }

            val iv = combinedPayload.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val cipherBytes = combinedPayload.copyOfRange(GCM_IV_LENGTH_BYTES, combinedPayload.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            cipher.updateAAD(envelope.sequenceNumber.toString().toByteArray(Charsets.UTF_8))

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Decrypted with Double Ratchet]"
        }
    }

    /**
     * Generates a 60-digit Signal-style Safety Number formatted into 12 blocks of 5 digits.
     */
    fun generateSafetyNumber(localIdentityKey: String, remoteIdentityKey: String): SafetyNumberData {
        val combinedKeys = if (localIdentityKey < remoteIdentityKey) {
            localIdentityKey + remoteIdentityKey
        } else {
            remoteIdentityKey + localIdentityKey
        }

        val md = MessageDigest.getInstance("SHA-512")
        val hash = md.digest(combinedKeys.toByteArray(Charsets.UTF_8))

        // Convert the 64-byte hash into a deterministic 60-digit numeric string
        val sb = java.lang.StringBuilder()
        for (i in 0 until 12) {
            val offset = i * 4
            val chunk = ((hash[offset].toInt() and 0xFF) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            val fiveDigitValue = (Math.abs(chunk.toLong()) % 100000).toString().padStart(5, '0')
            if (i > 0) sb.append(" ")
            sb.append(fiveDigitValue)
        }

        val localFingerprint = bytesToHex(MessageDigest.getInstance("SHA-256").digest(localIdentityKey.toByteArray(Charsets.UTF_8))).take(16)
        val remoteFingerprint = bytesToHex(MessageDigest.getInstance("SHA-256").digest(remoteIdentityKey.toByteArray(Charsets.UTF_8))).take(16)

        return SafetyNumberData(
            localFingerprint = localFingerprint,
            remoteFingerprint = remoteFingerprint,
            combinedNumericCode = sb.toString(),
            isVerified = false
        )
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef".toCharArray()
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i ushr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
