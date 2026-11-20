package com.resumeplayer.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    suspend fun loadVideos(): List<MediaEntry> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaEntry>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )
        context.contentResolver.query(
            collection, projection, null, null,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items.add(
                    MediaEntry(id, uri, c.getString(nameCol) ?: "Untitled",
                        c.getLong(durCol), MediaKind.VIDEO)
                )
            }
        }
        items
    }

    suspend fun loadAudio(): List<MediaEntry> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaEntry>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION
        )
        context.contentResolver.query(
            collection, projection, null, null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                items.add(
                    MediaEntry(id, uri, c.getString(nameCol) ?: "Untitled",
                        c.getLong(durCol), MediaKind.AUDIO)
                )
            }
        }
        items
    }
}
