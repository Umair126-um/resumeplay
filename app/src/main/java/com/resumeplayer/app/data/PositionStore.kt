package com.resumeplayer.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback")

class PositionStore(private val context: Context) {

    private fun posKey(mediaKey: String) = longPreferencesKey("pos_" + mediaKey)
    private val lastKey = stringPreferencesKey("last_played_uri")

    suspend fun savePosition(mediaKey: String, positionMs: Long) {
        context.dataStore.edit { it[posKey(mediaKey)] = positionMs }
    }

    suspend fun clearPosition(mediaKey: String) {
        context.dataStore.edit { it.remove(posKey(mediaKey)) }
    }

    suspend fun getPosition(mediaKey: String): Long {
        return context.dataStore.data.first()[posKey(mediaKey)] ?: 0L
    }

    suspend fun setLastPlayed(mediaKey: String) {
        context.dataStore.edit { it[lastKey] = mediaKey }
    }

    val lastPlayed: Flow<String?> = context.dataStore.data.map { it[lastKey] }

    fun positionsFlow(): Flow<Map<String, Long>> = context.dataStore.data.map { prefs ->
        prefs.asMap()
            .filterKeys { it.name.startsWith("pos_") }
            .mapNotNull { (k, v) ->
                val value = v as? Long ?: return@mapNotNull null
                k.name.removePrefix("pos_") to value
            }.toMap()
    }
}
