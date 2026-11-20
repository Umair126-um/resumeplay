package com.resumeplayer.app.data

import android.net.Uri

enum class MediaKind { VIDEO, AUDIO }

data class MediaEntry(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val kind: MediaKind
) {
    val key: String get() = uri.toString()
}
