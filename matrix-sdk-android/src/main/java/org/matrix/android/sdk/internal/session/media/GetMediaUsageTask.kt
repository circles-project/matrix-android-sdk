package org.matrix.android.sdk.internal.session.media

import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

//Created for Circles
internal class GetMediaUsageTask @Inject constructor(
        private val mediaAPI: MediaAPI
) : Task<Unit, MediaUsageInfo?> {

    override suspend fun execute(params: Unit): MediaUsageInfo? {
        return tryOrNull {
            val config = mediaAPI.getMediaConfig()
            val usage = mediaAPI.getMediaUsage()
            config.storageSize ?: throw IllegalArgumentException("storeSize is null")
            usage.usedSize ?: throw IllegalArgumentException("usedSize is null")
            MediaUsageInfo(
                    config.storageSize,
                    usage.usedSize,
                    usage.filesCount,
            )
        }
    }
}
