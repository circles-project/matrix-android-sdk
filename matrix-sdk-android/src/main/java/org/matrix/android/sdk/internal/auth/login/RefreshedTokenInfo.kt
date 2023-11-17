package org.matrix.android.sdk.internal.auth.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created for Circles /refresh
 */
@JsonClass(generateAdapter = true)
data class RefreshedTokenInfo(
        @Json(name = "access_token")
        val accessToken: String,

        @Json(name = "expires_in_ms")
        val expiresInMs: Long,

        @Json(name = "refresh_token")
        val refreshToken: String
)