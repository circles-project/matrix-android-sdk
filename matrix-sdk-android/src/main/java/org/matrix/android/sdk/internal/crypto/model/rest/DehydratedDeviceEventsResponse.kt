package org.matrix.android.sdk.internal.crypto.model.rest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.matrix.android.sdk.api.session.events.model.Event

//Added for Circles
@JsonClass(generateAdapter = true)
internal data class DehydratedDeviceEventsResponse(

        @Json(name = "events")
        val events: List<Event> = emptyList(),

        @Json(name = "next_batch")
        val nextBatch: String? = null
)