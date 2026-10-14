package com.kotlin.learn.ui.tools

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlin.learn.R
import com.kotlin.learn.ui.theme.Indigo
import kotlinx.coroutines.delay

@Composable
fun MusicPlayerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { player?.release() }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { position = it.currentPosition }
            delay(200)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Text("🎵 八音盒旋律（内置演示音频）", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            if (player == null) {
                MediaPlayer.create(context, R.raw.demo_melody)?.let { mp ->
                    player = mp
                    duration = mp.duration
                    mp.setOnCompletionListener {
                        isPlaying = false
                        position = 0
                    }
                    mp.start()
                    isPlaying = true
                }
            } else {
                player?.let { mp ->
                    if (mp.isPlaying) { mp.pause(); isPlaying = false }
                    else { mp.start(); isPlaying = true }
                }
            }
        }) { Text(if (isPlaying) "⏸ 暂停" else "▶ 播放") }
        Slider(
            value = if (duration > 0) position.toFloat() / duration else 0f,
            onValueChange = { fraction ->
                position = (fraction * duration).toInt()
                player?.seekTo(position)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = duration > 0,
        )
        Row {
            Text(
                "%02d:%02d / %02d:%02d".format(
                    position / 1000 / 60, position / 1000 % 60,
                    duration / 1000 / 60, duration / 1000 % 60,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Indigo,
            )
        }
    }
}
