package com.example.verity

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaMetadataReader(private val resolver: ContentResolver) {
    suspend fun read(uri: Uri): Map<String, String> = withContext(Dispatchers.IO) {
        when (resolver.getType(uri)?.substringBefore('/')) {
            "image" -> readImage(uri)
            "video", "audio" -> readAudioOrVideo(uri)
            else -> throw IllegalArgumentException("Choose an image, video, or audio file.")
        }
    }

    private fun readImage(uri: Uri): Map<String, String> {
        val stream = resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("The selected image could not be opened.")
        return stream.use { input ->
            val exif = ExifInterface(input)
            listOf(
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_IMAGE_DESCRIPTION,
                ExifInterface.TAG_USER_COMMENT,
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
            ).mapNotNull { tag -> exif.getAttribute(tag)?.takeIf(String::isNotBlank)?.let { tag to it } }.toMap()
        }
    }

    private fun readAudioOrVideo(uri: Uri): Map<String, String> {
        val descriptor = resolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("The selected media could not be opened.")
        return descriptor.use { file ->
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.fileDescriptor)
                listOf(
                    MediaMetadataRetriever.METADATA_KEY_TITLE,
                    MediaMetadataRetriever.METADATA_KEY_ALBUM,
                    MediaMetadataRetriever.METADATA_KEY_ARTIST,
                    MediaMetadataRetriever.METADATA_KEY_AUTHOR,
                    MediaMetadataRetriever.METADATA_KEY_COMPOSER,
                    MediaMetadataRetriever.METADATA_KEY_WRITER,
                    MediaMetadataRetriever.METADATA_KEY_GENRE,
                ).mapNotNull { key ->
                    retriever.extractMetadata(key)?.takeIf(String::isNotBlank)?.let { key.toString() to it }
                }.toMap()
            } finally {
                retriever.release()
            }
        }
    }
}

