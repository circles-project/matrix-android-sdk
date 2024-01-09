/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.session.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.util.MimeTypes
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

internal class ThumbnailExtractor @Inject constructor(
        private val context: Context
) {

    class ThumbnailData(
            val width: Int,
            val height: Int,
            val size: Long,
            val bytes: ByteArray,
            val mimeType: String
    )

    //Changed for Circles
    fun extractThumbnail(attachment: ContentAttachmentData): ThumbnailData? {
        if (attachment.mimeType == MimeTypes.Gif || attachment.mimeType == MimeTypes.Webp) return null
        return when (attachment.type) {
            ContentAttachmentData.Type.VIDEO -> extractVideoThumbnail(attachment)
            ContentAttachmentData.Type.IMAGE -> extractImageThumbnail(attachment)
            else                             -> null
        }
    }

    //Changed for Circles
    private fun extractVideoThumbnail(attachment: ContentAttachmentData): ThumbnailData? {
        var thumbnailData: ThumbnailData? = null
        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(context, attachment.queryUri)
            mediaMetadataRetriever.frameAtTime?.let { thumbnail ->
                val outputStream = ByteArrayOutputStream()
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val thumbnailWidth = thumbnail.width
                val thumbnailHeight = thumbnail.height
                val thumbnailSize = outputStream.size()
                thumbnailData = ThumbnailData(
                        width = thumbnailWidth,
                        height = thumbnailHeight,
                        size = thumbnailSize.toLong(),
                        bytes = outputStream.toByteArray(),
                        mimeType = MimeTypes.Jpeg
                )
                thumbnail.recycle()
                outputStream.reset()
            } ?: run {
                Timber.e("Cannot extract video thumbnail at ${attachment.queryUri}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Cannot extract video thumbnail")
        } finally {
            mediaMetadataRetriever.release()
        }
        return thumbnailData
    }

    //Added for Circles
    private fun extractImageThumbnail(attachment: ContentAttachmentData): ThumbnailData? {
        var thumbnailData: ThumbnailData? = null
        try {
            val thumbnail = createScaledThumbnailBitmap(getBitmapFromUri(attachment.queryUri))
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val thumbnailWidth = thumbnail.width
            val thumbnailHeight = thumbnail.height
            val thumbnailSize = outputStream.size()
            thumbnailData = ThumbnailData(
                    width = thumbnailWidth,
                    height = thumbnailHeight,
                    size = thumbnailSize.toLong(),
                    bytes = outputStream.toByteArray(),
                    mimeType = MimeTypes.Jpeg
            )
            thumbnail.recycle()
            outputStream.reset()
        } catch (e: Exception) {
            Timber.e(e, "Cannot extract image thumbnail")
        }
        return thumbnailData
    }

    @Suppress("DEPRECATION")
    private fun getBitmapFromUri(uri: Uri) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    //Added for Circles
    private fun createScaledThumbnailBitmap(originalBitmap: Bitmap): Bitmap {
        val maxThumbnailSize = 800
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val size = if (originalHeight > originalWidth) Size((maxThumbnailSize * aspectRatio).toInt(), maxThumbnailSize)
        else Size(maxThumbnailSize, (maxThumbnailSize / aspectRatio).toInt())
        return Bitmap.createScaledBitmap(originalBitmap, size.width, size.height, true)
    }
}
