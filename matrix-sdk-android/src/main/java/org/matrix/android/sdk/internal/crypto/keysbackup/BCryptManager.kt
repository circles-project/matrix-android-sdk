package org.matrix.android.sdk.internal.crypto.keysbackup

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest

internal object BCryptManager {

    private const val iterations = 14
    private const val saltLength = 16
    private const val privateKeyLength = 32

    fun generateBcryptPrivateKeyWithPassword(
        userName: String,
        password: String
    ): GeneratePrivateKeyResult {
        val salt = userName.sha256().substring(0, saltLength)
        val privateKey = retrievePrivateKeyWithPassword(password, salt, iterations)
        return GeneratePrivateKeyResult(privateKey, salt, iterations)
    }

    fun retrievePrivateKeyWithPassword(
        password: String,
        salt: String?,
        iterations: Int?
    ): ByteArray =
        BCrypt.withDefaults()
            .hash(iterations ?: this.iterations, (salt ?: "").toByteArray(), password.toByteArray())
            .takeLast(privateKeyLength)
            .reversed()
            .toByteArray()


    private fun String.sha256(): String = MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}