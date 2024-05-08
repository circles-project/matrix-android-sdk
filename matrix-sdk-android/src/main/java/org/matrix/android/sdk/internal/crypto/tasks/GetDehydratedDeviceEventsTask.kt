package org.matrix.android.sdk.internal.crypto.tasks

import org.matrix.android.sdk.internal.crypto.api.CryptoApi
import org.matrix.android.sdk.internal.crypto.model.rest.DehydratedDeviceEventsResponse
import org.matrix.android.sdk.internal.crypto.model.rest.GetDehydratedDeviceEventsRequestParams
import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

//Added for Circles
internal interface GetDehydratedDeviceEventsTask : Task<GetDehydratedDeviceEventsTask.Params, DehydratedDeviceEventsResponse> {
    data class Params(
            val deviceId: String,
            val nextBatch: String
    )
}

internal class DefaultGetDehydratedDeviceEventsTask @Inject constructor(
        private val cryptoApi: CryptoApi,
        private val globalErrorReceiver: GlobalErrorReceiver
) : GetDehydratedDeviceEventsTask {

    override suspend fun execute(params: GetDehydratedDeviceEventsTask.Params): DehydratedDeviceEventsResponse {
        return executeRequest(globalErrorReceiver) {
            cryptoApi.getDehydratedDeviceEvents(params.deviceId, GetDehydratedDeviceEventsRequestParams(params.nextBatch))
        }
    }
}
