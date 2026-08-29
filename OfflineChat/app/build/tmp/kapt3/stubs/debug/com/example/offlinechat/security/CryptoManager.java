package com.example.offlinechat.security;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u0006J\u000e\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bJ\u000e\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\u0017\u001a\u00020\u000bJ\u000e\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0019\u001a\u00020\u000bJ\u000e\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0019\u001a\u00020\u000bJ\u000e\u0010\u001b\u001a\u00020\u000b2\u0006\u0010\u0019\u001a\u00020\u000bJ\u0006\u0010\u001c\u001a\u00020\u0006J\u0006\u0010\u001d\u001a\u00020\u0006J\b\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u001fH\u0002J\u000e\u0010!\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020\u0006J\u000e\u0010#\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u000bJ\u000e\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020%J\u000e\u0010\'\u001a\u00020(2\u0006\u0010&\u001a\u00020%R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/example/offlinechat/security/CryptoManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "IDENTITY_KEYSET_NAME", "", "MASTER_KEY_URI", "PREF_FILE_NAME", "STORAGE_KEYSET_NAME", "TRANSIT_KEY_BYTES", "", "identityKeysetHandle", "Lcom/google/crypto/tink/KeysetHandle;", "localStorageAead", "Lcom/google/crypto/tink/Aead;", "peerPublicKeyHandle", "sessionPrivateKeyHandle", "decryptFromStorage", "ciphertextBase64", "decryptFromTransit", "data", "decryptMessage", "ciphertext", "encryptForStorage", "plaintext", "encryptForTransit", "encryptMessage", "generateSessionPublicKey", "getSerializedPublicKey", "initializeIdentityKey", "", "initializeLocalStorageKey", "receivePeerSessionKey", "serializedPubKey", "signData", "signPacketEnvelope", "Lcom/example/offlinechat/network/MeshPacket;", "packet", "verifyPacketSignature", "", "app_debug"})
public final class CryptoManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String PREF_FILE_NAME = "offlinechat_keys";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String IDENTITY_KEYSET_NAME = "identity_keyset";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String STORAGE_KEYSET_NAME = "storage_keyset";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String MASTER_KEY_URI = "android-keystore://offlinechat_master_key";
    private com.google.crypto.tink.KeysetHandle identityKeysetHandle;
    private com.google.crypto.tink.Aead localStorageAead;
    @org.jetbrains.annotations.Nullable
    private com.google.crypto.tink.KeysetHandle sessionPrivateKeyHandle;
    @org.jetbrains.annotations.Nullable
    private com.google.crypto.tink.KeysetHandle peerPublicKeyHandle;
    @org.jetbrains.annotations.NotNull
    private final byte[] TRANSIT_KEY_BYTES = {(byte)87, (byte)104, (byte)105, (byte)115, (byte)112, (byte)79, (byte)102, (byte)102, (byte)108, (byte)105, (byte)110, (byte)101, (byte)77, (byte)101, (byte)115, (byte)104, (byte)83, (byte)101, (byte)99, (byte)117, (byte)114, (byte)101, (byte)75, (byte)101, (byte)121, (byte)50, (byte)48, (byte)50, (byte)54, (byte)33, (byte)64, (byte)35};
    
    public CryptoManager(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    private final void initializeIdentityKey() {
    }
    
    private final void initializeLocalStorageKey() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getSerializedPublicKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final byte[] signData(@org.jetbrains.annotations.NotNull
    byte[] data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.network.MeshPacket signPacketEnvelope(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.MeshPacket packet) {
        return null;
    }
    
    public final boolean verifyPacketSignature(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.MeshPacket packet) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String generateSessionPublicKey() {
        return null;
    }
    
    public final void receivePeerSessionKey(@org.jetbrains.annotations.NotNull
    java.lang.String serializedPubKey) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final byte[] encryptForTransit(@org.jetbrains.annotations.NotNull
    byte[] plaintext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final byte[] decryptFromTransit(@org.jetbrains.annotations.NotNull
    byte[] data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String encryptForStorage(@org.jetbrains.annotations.NotNull
    byte[] plaintext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String decryptFromStorage(@org.jetbrains.annotations.NotNull
    java.lang.String ciphertextBase64) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final byte[] encryptMessage(@org.jetbrains.annotations.NotNull
    byte[] plaintext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final byte[] decryptMessage(@org.jetbrains.annotations.NotNull
    byte[] ciphertext) {
        return null;
    }
}