package org.matrix.android.sdk.internal.session.media

data class MediaUsageInfo(
        val storageSize: Long,
        val usedSize: Long,
        val filesCount: Long?
)