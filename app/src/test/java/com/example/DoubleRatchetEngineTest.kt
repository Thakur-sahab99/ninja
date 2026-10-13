package com.example

import com.example.core.crypto.DoubleRatchetEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleRatchetEngineTest {

    @Test
    fun testKeyPairGeneration() {
        val keyPair = DoubleRatchetEngine.generateKeyPair()
        assertNotNull(keyPair)
        assertEquals(64, keyPair.publicKeyHex.length)
        assertEquals(64, keyPair.privateKeyHex.length)
        assertNotEquals(keyPair.publicKeyHex, keyPair.privateKeyHex)
    }

    @Test
    fun testEncryptDecryptMessageRoundtrip() {
        val plaintext = "Ninja classified rendezvous: 04:00 AM at the gate"
        val sessionKey = "0123456789abcdef0123456789abcdef"
        val sequenceNumber = 101L

        val envelope = DoubleRatchetEngine.encryptMessage(
            plaintext = plaintext,
            sessionKeyHex = sessionKey,
            sequenceNumber = sequenceNumber
        )

        assertNotNull(envelope)
        assertTrue(envelope.isSealedSender)
        assertTrue(envelope.ciphertextBase64.isNotBlank())
        assertEquals(sequenceNumber, envelope.sequenceNumber)

        val decrypted = DoubleRatchetEngine.decryptMessage(
            envelope = envelope,
            sessionKeyHex = sessionKey
        )

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun testSafetyNumberGeneration_FormatAndDeterminism() {
        val aliceIdentityKey = "3a9f02b1c8e744d90a12fe4490cd8812"
        val bobIdentityKey = "5e8d91c0bb227f10d938290fa8bc0091"

        val safetyNumberAlice = DoubleRatchetEngine.generateSafetyNumber(aliceIdentityKey, bobIdentityKey)
        val safetyNumberBob = DoubleRatchetEngine.generateSafetyNumber(bobIdentityKey, aliceIdentityKey)

        // Safety numbers must be identical regardless of who generates them
        assertEquals(safetyNumberAlice.combinedNumericCode, safetyNumberBob.combinedNumericCode)

        // Must consist of 12 blocks of 5 digits separated by spaces
        val blocks = safetyNumberAlice.combinedNumericCode.split(" ")
        assertEquals(12, blocks.size)
        for (b in blocks) {
            assertEquals(5, b.length)
            assertTrue(b.all { it.isDigit() })
        }
    }

    @Test
    fun testHkdfDeriveKey() {
        val rootKey = "initial-chain-root-32-byte-key!!".toByteArray(Charsets.UTF_8)
        val (nextChain, messageKey) = DoubleRatchetEngine.hkdfDeriveKey(rootKey, "step-1")

        assertNotNull(nextChain)
        assertNotNull(messageKey)
        assertEquals(32, nextChain.size)
        assertEquals(32, messageKey.size)
    }
}
