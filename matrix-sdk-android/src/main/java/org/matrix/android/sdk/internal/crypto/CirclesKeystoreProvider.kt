package org.matrix.android.sdk.internal.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import org.matrix.android.sdk.api.extensions.tryOrNull
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

//Created for Circles
class CirclesKeystoreProvider @Inject constructor(
        context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val keyStore
        get() = KeyStore.getInstance(ANDROID_KEYS_TORE).apply { load(null) }

    fun storeBsSpekePrivateKey(keyBytes: ByteArray, keyId: String) {
        tryOrNull {
            val alias = "$ORG_FUTO_SSSS_KEY_PREFIX.$keyId"
            val masterKey = getOrGenerateMasterKeyIfNotExist(alias)
                    ?: throw IllegalArgumentException("Failed to get master key")
            val encrypted = encryptData(keyBytes, masterKey)
            storeBSspekeEncryptedPrivateKeyBase64(alias, encrypted)
        }
    }

    fun getBsSpekePrivateKey(keyId: String): ByteArray? = tryOrNull {
        val alias = "$ORG_FUTO_SSSS_KEY_PREFIX.$keyId"
        val masterKey = getOrGenerateMasterKeyIfNotExist(alias)
                ?: throw IllegalArgumentException("Failed to get master key")
        val encrypted = getBSspekeEncryptedPrivateKeyBase64(alias)
                ?: throw IllegalArgumentException("Not saved in preferences $alias")
        decryptData(encrypted, masterKey)
    }

    private fun getOrGenerateMasterKeyIfNotExist(alias: String): SecretKey? {
        if (!keyStore.containsAlias(alias)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYS_TORE).apply {
                init(
                        KeyGenParameterSpec.Builder(
                                alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                                .setKeySize(KEY_SIZE)
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                .build()
                )
                generateKey()
            }
        }
        return keyStore.getKey(alias, null) as? SecretKey
    }

    private fun encryptData(data: ByteArray, masterKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_NO_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        val ivString = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
        val encryptedBytes: ByteArray = cipher.doFinal(data)
        return ivString + IV_SEPARATOR + Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptData(encryptedData: String, masterKey: SecretKey): ByteArray {
        val (iv64, encrypted64) = encryptedData.split(IV_SEPARATOR)
        val cipher = Cipher.getInstance(AES_NO_PADDING)
        val spec = GCMParameterSpec(KEY_SIZE, Base64.decode(iv64, Base64.DEFAULT))
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
        return cipher.doFinal(Base64.decode(encrypted64, Base64.DEFAULT))
    }

    private fun getBSspekeEncryptedPrivateKeyBase64(alias: String): String? {
        return sharedPreferences.getString(alias, null)
    }

    private fun storeBSspekeEncryptedPrivateKeyBase64(alias: String, key: String) {
        sharedPreferences.edit { putString(alias, key) }
    }

    companion object {
        private const val PREF_NAME = "org.futo.circles.keystore"
        private const val AES_NO_PADDING = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]"
        private const val KEY_SIZE = 128
        private const val ANDROID_KEYS_TORE = "AndroidKeyStore"
        private const val ORG_FUTO_SSSS_KEY_PREFIX = "org.futo.ssss.key"
    }
}
