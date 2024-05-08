package org.matrix.android.sdk.internal.crypto.model.rest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.matrix.android.sdk.api.util.JsonDict

//Added for Circles
@JsonClass(generateAdapter = true)
internal data class GetDehydratedDeviceResponse(
        @Json(name = "device_id")
        val deviceId: String? = null,

        @Json(name = "device_data")
        val deviceData: JsonDict? = null
)
