package com.example.eva.ui.theme.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eva.R
import kotlinx.coroutines.delay

/*
================================================================================
LIVE CALL SCREEN
================================================================================

PURPOSE:
    Voice call interface for real-time conversation with EVA.

NOTE:
    This is currently a UI template. Full voice implementation requires:
    - Audio recording (MediaRecorder or AudioRecord)
    - WebSocket connection for streaming
    - Audio playback (MediaPlayer or AudioTrack)
    - Speech-to-text and text-to-speech integration

CURRENT FEATURES:
    - Call duration timer
    - Mute toggle
    - Speaker toggle
    - End call button
    - Visual feedback (avatar, waveform placeholder)

FUTURE IMPLEMENTATION:
    The backend supports streaming via /chat/send/stream endpoint.
    For voice, you would:
    1. Record audio from microphone
    2. Send to speech-to-text service
    3. Send text to backend streaming endpoint
    4. Receive streamed response
    5. Convert to speech with text-to-speech
    6. Play audio

================================================================================
*/

@Composable
fun LiveCallScreen(
    onEndCall: () -> Unit
) {
    /*
    ================================================================================
    STATE
    ================================================================================
    */

    // Call duration in seconds
    var duration by remember { mutableIntStateOf(0) }

    // Mute state
    var isMuted by remember { mutableStateOf(false) }

    // Speaker state
    var isSpeakerOn by remember { mutableStateOf(true) }

    /*
    ================================================================================
    EFFECTS
    ================================================================================
    */

    // Timer to update call duration
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            duration++
        }
    }

    /*
    ================================================================================
    HELPER FUNCTIONS
    ================================================================================
    */

    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    /*
    ================================================================================
    UI
    ================================================================================
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top spacer
        Spacer(modifier = Modifier.height(48.dp))

        // Avatar and info section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // EVA Avatar
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = stringResource(R.string.call_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration
            Text(
                text = formatDuration(duration),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Waveform placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simple waveform visualization placeholder
            repeat(7) { index ->
                val height = when (index) {
                    0, 6 -> 20.dp
                    1, 5 -> 35.dp
                    2, 4 -> 50.dp
                    else -> 60.dp
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(6.dp)
                        .height(height)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                )
            }
        }

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMuted) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        } else {
                            Color.Gray.copy(alpha = 0.3f)
                        }
                    )
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = stringResource(R.string.call_mute_button),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.call_end_button),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Speaker button
            IconButton(
                onClick = { isSpeakerOn = !isSpeakerOn },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSpeakerOn) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        } else {
                            Color.Gray.copy(alpha = 0.3f)
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = stringResource(R.string.call_speaker_button),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}