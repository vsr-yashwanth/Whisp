package com.example.offlinechat.security

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.PublicKeyVerify
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.signature.SignatureConfig
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(private val context: Context) {

    private val PREF_FILE_NAME = "offlinechat_keys"
    private val IDENTITY_KEYSET_NAME = "identity_keyset"
    private val STORAGE_KEYSET_NAME = "storage_keyset"
    private val MASTER_KEY_URI = "android-keystore://offlinechat_master_key"

    private lateinit var identityKeysetHandle: KeysetHandle
    private lateinit var localStorageAead: Aead

    // Ephemeral session states
    private var sessionPrivateKeyHandle: KeysetHandle? = null
    private var peerPublicKeyHandle: KeysetHandle? = null

    // Mesh Channel Transit Key for offline mesh broadcast
    private val TRANSIT_KEY_BYTES = byteArrayOf(
        0x57.toByte(), 0x68.toByte(), 0x69.toByte(), 0x73.toByte(),
        0x70.toByte(), 0x4F.toByte(), 0x66.toByte(), 0x66.toByte(),
        0x6C.toByte(), 0x69.toByte(), 0x6E.toByte(), 0x65.toByte(),
        0x4D.toByte(), 0x65.toByte(), 0x73.toByte(), 0x68.toByte(),
        0x53.toByte(), 0x65.toByte(), 0x63.toByte(), 0x75.toByte(),
        0x72.toByte(), 0x65.toByte(), 0x4B.toByte(), 0x65.toByte(),
        0x79.toByte(), 0x32.toByte(), 0x30.toByte(), 0x32.toByte(),
        0x36.toByte(), 0x21.toByte(), 0x40.toByte(), 0x23.toByte()
    ) // 32 bytes AES-256

    init {
        // Initialize Tink Configs for Signatures, AEAD, and Hybrid Encryption
        SignatureConfig.register()
        AeadConfig.register()
        HybridConfig.register()

        initializeIdentityKey()
        initializeLocalStorageKey()
    }

    private fun initializeIdentityKey() {
        identityKeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, IDENTITY_KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("ED25519"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

    private fun initializeLocalStorageKey() {
        val storageKeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, STORAGE_KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        localStorageAead = storageKeysetHandle.getPrimitive(Aead::class.java)
    }

    fun getSerializedPublicKey(): String {
        val publicKeysetHandle = identityKeysetHandle.publicKeysetHandle
        val outputStream = ByteArrayOutputStream()
        CleartextKeysetHandle.write(publicKeysetHandle, JsonKeysetWriter.withOutputStream(outputStream))
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun signData(data: ByteArray): ByteArray {
        val signer = identityKeysetHandle.getPrimitive(PublicKeySign::class.java)
        return signer.sign(data)
    }

    fun signPacketEnvelope(packet: com.example.offlinechat.network.MeshPacket): com.example.offlinechat.network.MeshPacket {
        return try {
            val payloadBytes = packet.computeSigningPayload()
            val sigBytes = signData(payloadBytes)
            val sigBase64 = Base64.encodeToString(sigBytes, Base64.NO_WRAP)
            val pubKey = getSerializedPublicKey()
            packet.copy(signature = sigBase64, senderPublicKey = pubKey)
        } catch (e: Exception) {
            packet
        }
    }

    fun verifyPacketSignature(packet: com.example.offlinechat.network.MeshPacket): Boolean {
        if (packet.signature.isBlank() || packet.senderPublicKey.isBlank()) {
            return false // Fail closed for unsigned packets in V4
        }
        return try {
            val pubKeyBytes = Base64.decode(packet.senderPublicKey, Base64.NO_WRAP)
            val publicKeysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(pubKeyBytes))
            val verifier = publicKeysetHandle.getPrimitive(PublicKeyVerify::class.java)
            val sigBytes = Base64.decode(packet.signature, Base64.NO_WRAP)
            verifier.verify(sigBytes, packet.computeSigningPayload())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generateSessionPublicKey(): String {
        sessionPrivateKeyHandle = KeysetHandle.generateNew(KeyTemplates.get("ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM"))
        val publicKeysetHandle = sessionPrivateKeyHandle!!.publicKeysetHandle
        val outputStream = ByteArrayOutputStream()
        CleartextKeysetHandle.write(publicKeysetHandle, JsonKeysetWriter.withOutputStream(outputStream))
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun receivePeerSessionKey(serializedPubKey: String) {
        val bytes = Base64.decode(serializedPubKey, Base64.NO_WRAP)
        peerPublicKeyHandle = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(bytes))
    }

    // Transit Encryption (Over-the-wire)
    fun encryptForTransit(plaintext: ByteArray): ByteArray {
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(TRANSIT_KEY_BYTES, "AES"), GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    fun decryptFromTransit(data: ByteArray): ByteArray {
        if (data.size < 12) throw IllegalArgumentException("Transit ciphertext too short")
        val iv = data.copyOfRange(0, 12)
        val ciphertext = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(TRANSIT_KEY_BYTES, "AES"), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    // Storage Encryption (At rest in local SQLite)
    fun encryptForStorage(plaintext: ByteArray): String {
        val encrypted = localStorageAead.encrypt(plaintext, ByteArray(0))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decryptFromStorage(ciphertextBase64: String): String {
        return try {
            val bytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val decrypted = localStorageAead.decrypt(bytes, ByteArray(0))
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            try {
                val bytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
                String(decryptFromTransit(bytes), StandardCharsets.UTF_8)
            } catch (e2: Exception) {
                try {
                    String(Base64.decode(ciphertextBase64, Base64.NO_WRAP), StandardCharsets.UTF_8)
                } catch (e3: Exception) {
                    "[Encrypted Message]"
                }
            }
        }
    }

    // Legacy helpers
    fun encryptMessage(plaintext: ByteArray): ByteArray = encryptForTransit(plaintext)
    fun decryptMessage(ciphertext: ByteArray): ByteArray {
        return try {
            decryptFromTransit(ciphertext)
        } catch (e: Exception) {
            localStorageAead.decrypt(ciphertext, ByteArray(0))
        }
    }
}
