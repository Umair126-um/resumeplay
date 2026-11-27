package com.resumeplayer.app.ui

import android.content.ComponentName
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.resumeplayer.app.data.MediaEntry
import com.resumeplayer.app.data.MediaKind
import com.resumeplayer.app.data.PositionStore
import com.resumeplayer.app.playback.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(
    entry: MediaEntry,
    store: PositionStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // A scope that outlives composition so the final position write always
    // completes, even when the user backs out of this screen.
    val saveScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    var controller by remember { mutableStateOf<MediaController?>(null) }

    fun persist(c: MediaController?) {
        c ?: return
        val pos = c.currentPosition
        val dur = c.duration
        saveScope.launch {
            // If finished (or within last 2s), clear so it restarts next time.
            if (dur > 0 && pos >= dur - 2000) store.clearPosition(entry.key)
            else if (pos > 0) store.savePosition(entry.key, pos)
        }
    }

    // Connect to the playback service. The service owns the player, so audio
    // keeps running when the screen is off or the app is backgrounded.
    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            { controller = future.get() },
            ContextCompat.getMainExecutor(context)
        )
        onDispose {
            // Leaving this screen (back press) = stop this item and tear down.
            val c = controller
            persist(c)
            c?.pause()
            c?.stop()
            c?.clearMediaItems()
            MediaController.releaseFuture(future)
            controller = null
        }
    }

    // Once connected, load the item, resume from the saved position, and play.
    LaunchedEffect(controller, entry.key) {
        val c = controller ?: return@LaunchedEffect
        c.setMediaItem(Media3Item.fromUri(entry.uri))
        c.prepare()
        val saved = store.getPosition(entry.key)
        if (saved > 0) c.seekTo(saved)
        store.setLastPlayed(entry.key)
        c.playWhenReady = true
    }

    // Clear position when playback reaches the end.
    DisposableEffect(controller) {
        val c = controller
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    saveScope.launch { store.clearPosition(entry.key) }
                }
            }
        }
        c?.addListener(listener)
        onDispose { c?.removeListener(listener) }
    }

    // Save every 5 seconds while open.
    LaunchedEffect(controller) {
        if (controller == null) return@LaunchedEffect
        while (true) {
            delay(5000)
            persist(controller)
        }
    }

    // Save on lifecycle pause (screen off / app backgrounded) — but DO NOT pause.
    // Playback continues in the foreground service while the screen is off.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) persist(controller)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler { onBack() }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            val activeController = controller
            if (entry.kind == MediaKind.VIDEO) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                        }
                    },
                    update = { view -> view.player = activeController }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        text = entry.title,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = true
                                setShowNextButton(false)
                                setShowPreviousButton(false)
                            }
                        },
                        update = { view -> view.player = activeController }
                    )
                }
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    }
}
