package org.matrix.android.sdk.internal.crypto.model.rest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class GetDehydratedDeviceEventsRequestParams(
        @Json(name = "next_batch")
        val nextBatch: String
)