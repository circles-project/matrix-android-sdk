package org.matrix.android.sdk.internal.session.media

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GetMediaUsageResult(

        @Json(name = "org.matrix.msc4034.storage.used")
        val usedSize: Long? = null,

        @Json(name = "org.matrix.msc4034.storage.files")
        val filesCount: Long? = null
)