package com.example.core.crypto

/**
 * Data structures for Ninja's End-to-End Encryption layer
 * based on Double Ratchet and X3DH cryptographic principles.
 */
data class NinjaKeyPair(
    val publicKeyHex: String,
    val privateKeyHex: String
)

data class PreKeyBundle(
    val identityKeyHex: String,
    val signedPreKeyHex: String,
    val signatureHex: String,
    val oneTimePreKeyHex: String? = null
)

data class EncryptedEnvelope(
    val ephemeralPublicKeyHex: String,
    val sequenceNumber: Long,
    val previousChainLength: Long,
    val ciphertextBase64: String,
    val hmacTagHex: String,
    val isSealedSender: Boolean = true
)

data class SafetyNumberData(
    val localFingerprint: String,
    val remoteFingerprint: String,
    val combinedNumericCode: String, // 60 digits formatted in 12 blocks of 5
    val isVerified: Boolean = false
)
