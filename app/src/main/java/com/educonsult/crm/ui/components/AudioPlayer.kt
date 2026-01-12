package com.educonsult.crm.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PlayerState {
    IDLE, LOADING, PLAYING, PAUSED, ERROR
}

@Composable
fun AudioPlayer(
    url: String?,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    var playerState by remember { mutableStateOf(PlayerState.IDLE) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTime by remember { mutableStateOf("0:00") }
    var totalTime by remember { mutableStateOf("0:00") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    // Update progress while playing
    LaunchedEffect(playerState) {
        while (playerState == PlayerState.PLAYING) {
            try {
                val current = mediaPlayer.currentPosition
                val duration = mediaPlayer.duration
                if (duration > 0) {
                    progress = current.toFloat() / duration
                    currentTime = formatTime(current)
                    totalTime = formatTime(duration)
                }
            } catch (e: Exception) {
                // Player may have been released
            }
            delay(200)
        }
    }

    fun startPlayback() {
        if (url.isNullOrBlank()) {
            errorMessage = "Recording not available"
            playerState = PlayerState.ERROR
            onError("Recording not available")
            return
        }

        scope.launch {
            try {
                playerState = PlayerState.LOADING
                
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                
                mediaPlayer.setOnPreparedListener {
                    totalTime = formatTime(mediaPlayer.duration)
                    mediaPlayer.start()
                    playerState = PlayerState.PLAYING
                }
                
                mediaPlayer.setOnCompletionListener {
                    playerState = PlayerState.IDLE
                    progress = 0f
                    currentTime = "0:00"
                }
                
                mediaPlayer.setOnErrorListener { _, what, extra ->
                    errorMessage = "Playback error: $what"
                    playerState = PlayerState.ERROR
                    onError("Playback error: $what, $extra")
                    true
                }
            } catch (e: Exception) {
                errorMessage = e.message
                playerState = PlayerState.ERROR
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun togglePlayPause() {
        when (playerState) {
            PlayerState.IDLE, PlayerState.ERROR -> startPlayback()
            PlayerState.PLAYING -> {
                try {
                    mediaPlayer.pause()
                    playerState = PlayerState.PAUSED
                } catch (e: Exception) {
                    // Ignore
                }
            }
            PlayerState.PAUSED -> {
                try {
                    mediaPlayer.start()
                    playerState = PlayerState.PLAYING
                } catch (e: Exception) {
                    startPlayback()
                }
            }
            PlayerState.LOADING -> { /* Do nothing */ }
        }
    }

    fun stop() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            playerState = PlayerState.IDLE
            progress = 0f
            currentTime = "0:00"
        } catch (e: Exception) {
            // Ignore
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        FilledIconButton(
            onClick = { togglePlayPause() },
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            when (playerState) {
                PlayerState.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                PlayerState.PLAYING -> {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Progress and time
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )

            // Error message
            AnimatedVisibility(visible = playerState == PlayerState.ERROR) {
                Text(
                    text = errorMessage ?: "Error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Stop button (only when playing or paused)
        AnimatedVisibility(visible = playerState == PlayerState.PLAYING || playerState == PlayerState.PAUSED) {
            Row {
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { stop() },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}
