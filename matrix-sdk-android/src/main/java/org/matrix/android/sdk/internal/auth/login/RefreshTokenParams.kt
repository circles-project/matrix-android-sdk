package org.matrix.android.sdk.internal.auth.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created for Circles /refresh
 */
@JsonClass(generateAdapter = true)
data class RefreshTokenParams(
        @Json(name = "refresh_token")
        val refreshToken: String
)
