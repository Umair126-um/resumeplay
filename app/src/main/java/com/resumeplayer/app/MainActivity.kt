package com.resumeplayer.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resumeplayer.app.data.MediaEntry
import com.resumeplayer.app.data.MediaRepository
import com.resumeplayer.app.data.PositionStore
import com.resumeplayer.app.ui.HomeScreen
import com.resumeplayer.app.ui.PlayerScreen
import com.resumeplayer.app.ui.theme.ResumePlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResumePlayerTheme {
                AppRoot()
            }
        }
    }
}

private fun requiredPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val repo = remember { MediaRepository(context) }
    val store = remember { PositionStore(context) }

    var granted by remember {
        mutableStateOf(
            requiredPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var videos by remember { mutableStateOf<List<MediaEntry>>(emptyList()) }
    var audio by remember { mutableStateOf<List<MediaEntry>>(emptyList()) }
    var selected by remember { mutableStateOf<MediaEntry?>(null) }

    val positionsFlow = remember { store.positionsFlow() }
    val positions by positionsFlow.collectAsStateWithLifecycle(initialValue = emptyMap())
    val lastPlayed by store.lastPlayed.collectAsStateWithLifecycle(initialValue = null)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.any { it }
    }

    // Ask for notification permission (Android 13+) so the playback notification
    // and lock-screen controls can appear while the screen is off.
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(granted) {
        if (granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(granted, selected) {
        if (granted && selected == null) {
            videos = repo.loadVideos()
            audio = repo.loadAudio()
        }
    }

    val current = selected
    if (current != null) {
        PlayerScreen(entry = current, store = store, onBack = { selected = null })
        return
    }

    if (!granted) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Resume Player needs access to your videos and audio to play them.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { launcher.launch(requiredPermissions()) },
                    modifier = Modifier.padding(top = 20.dp)
                ) { Text("Grant access") }
            }
        }
    } else {
        HomeScreen(
            videos = videos,
            audio = audio,
            positions = positions,
            lastPlayedKey = lastPlayed,
            onOpen = { selected = it }
        )
    }
}
