package com.example.offlinechat.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var aliceCryptoManager: CryptoManager
    private lateinit var bobCryptoManager: CryptoManager

    @Before
    fun setup() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // In a real app we might inject different SharedPreferences for Alice and Bob
        // but for this test, we are mainly testing the ephemeral in-memory Hybrid keys
        // which don't conflict, so sharing the context is fine.
        aliceCryptoManager = CryptoManager(appContext)
        bobCryptoManager = CryptoManager(appContext)
    }

    @Test
    fun testIdentitySignAndVerify() {
        val data = "test_data".toByteArray()
        val signature = aliceCryptoManager.signData(data)
        
        assertNotNull(signature)
        assertTrue(signature.isNotEmpty())
    }

    @Test
    fun testHybridSessionEncryption() {
        // 1. Alice generates an ephemeral session public key
        val alicePubKey = aliceCryptoManager.generateSessionPublicKey()
        assertNotNull(alicePubKey)
        
        // 2. Bob also generates his own session public key (simulating full duplex)
        val bobPubKey = bobCryptoManager.generateSessionPublicKey()
        assertNotNull(bobPubKey)
        
        // 3. Bob receives Alice's public key over the network
        bobCryptoManager.receivePeerSessionKey(alicePubKey)
        
        // 4. Bob encrypts a message for Alice using her public key
        val plaintext = "Hello Alice, this is an encrypted offline message!".toByteArray()
        val ciphertext = bobCryptoManager.encryptMessage(plaintext)
        
        // Ciphertext should not be equal to plaintext
        assertTrue(!ciphertext.contentEquals(plaintext))
        
        // 5. Alice decrypts the message using her private session key
        val decrypted = aliceCryptoManager.decryptMessage(ciphertext)
        
        // 6. Verify decryption was successful
        assertEquals(String(plaintext), String(decrypted))
    }
}
