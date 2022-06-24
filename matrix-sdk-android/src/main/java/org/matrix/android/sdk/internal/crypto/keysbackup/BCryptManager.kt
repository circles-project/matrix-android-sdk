package org.matrix.android.sdk.internal.crypto.keysbackup

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest

internal object BCryptManager {

    private const val iterations = 14
    private const val saltLength = 16


    fun generateBcryptPrivateKeyWithPassword(
        userName: String,
        password: String
    ): GeneratePrivateKeyResult {
        val salt = userName.sha256().substring(0, saltLength).toByteArray()
        val privateKey = BCrypt.withDefaults().hash(iterations, salt, password.toByteArray())
        return GeneratePrivateKeyResult(privateKey, salt.toString(), iterations)
    }


    private fun String.sha256(): String = MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }

}