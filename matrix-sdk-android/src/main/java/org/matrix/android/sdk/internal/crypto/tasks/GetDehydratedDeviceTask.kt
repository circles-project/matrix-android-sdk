package org.matrix.android.sdk.internal.crypto.tasks

import org.matrix.android.sdk.internal.crypto.api.CryptoApi
import org.matrix.android.sdk.internal.crypto.model.rest.GetDehydratedDeviceResponse
import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

//Added for Circles
internal interface GetDehydratedDeviceTask : Task<Unit, GetDehydratedDeviceResponse>

internal class DefaultGetDehydratedDeviceTask @Inject constructor(
        private val cryptoApi: CryptoApi,
        private val globalErrorReceiver: GlobalErrorReceiver
) : GetDehydratedDeviceTask {

    override suspend fun execute(params: Unit): GetDehydratedDeviceResponse {
        return executeRequest(globalErrorReceiver) {
            cryptoApi.getDehydratedDevice()
        }
    }
}
