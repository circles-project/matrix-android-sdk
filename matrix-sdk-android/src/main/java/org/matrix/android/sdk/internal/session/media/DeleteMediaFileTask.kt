package org.matrix.android.sdk.internal.session.media

import org.matrix.android.sdk.internal.network.GlobalErrorReceiver
import org.matrix.android.sdk.internal.network.executeRequest
import org.matrix.android.sdk.internal.network.token.HomeserverAccessTokenProvider
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

//Created for Circles
internal interface DeleteMediaFileTask : Task<DeleteMediaFileTask.Params, Unit> {
    data class Params(
            val server: String,
            val mediaId: String
    )
}

internal class DefaultDeleteMediaFileTask @Inject constructor(
        private val mediaAPI: MediaAPI,
        private val globalErrorReceiver: GlobalErrorReceiver,
        private val accessTokenProvider: HomeserverAccessTokenProvider
) : DeleteMediaFileTask {

    override suspend fun execute(params: DeleteMediaFileTask.Params) {
        return executeRequest(globalErrorReceiver) {
            val token = accessTokenProvider.getToken() ?: throw IllegalArgumentException("invalid token")
            mediaAPI.deleteMediaFile(params.server, params.mediaId, token)

        }
    }
}
