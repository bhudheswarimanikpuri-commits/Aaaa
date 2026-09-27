package com.example.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.example.data.model.AudioTrack
import com.example.data.model.BeatMarker
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHover
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackTextColor
import com.example.ui.theme.TrackVideoColor

@Composable
fun TimelineTrackView(
    clips: List<ClipData>,
    selectedClipIndex: Int,
    audioTracks: List<AudioTrack>,
    textOverlays: List<TextOverlay>,
    beatMarkers: List<BeatMarker>,
    playheadMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean = false,
    onSeek: (Long) -> Unit,
    onSelectClip: (Int) -> Unit,
    onAddMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom factor: pixels per millisecond
    var zoomScale by remember { mutableFloatStateOf(0.085f) } // ~85dp per second
    val totalWidthDp = ((totalDurationMs * zoomScale) + 300f).coerceAtLeast(400f).dp

    val scrollState = rememberScrollState()
    val density = LocalDensity.current.density
    val haptic = LocalHapticFeedback.current

    // Smooth auto-scroll following the playhead when playing
    LaunchedEffect(playheadMs, isPlaying) {
        if (isPlaying) {
            val playheadPx = (24f + (playheadMs * zoomScale)) * density
            val currentScroll = scrollState.value
            val targetScroll = (playheadPx - 200 * density).toInt().coerceAtLeast(0)
            if (kotlin.math.abs(targetScroll - currentScroll) > 50 * density) {
                scrollState.animateScrollTo(targetScroll)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(1.dp, StudioBorder)
    ) {
        // Timeline Header: Timecode readout & Zoom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTimecode(playheadMs),
                    color = PlayheadColor,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " / " + formatTimecode(totalDurationMs),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.02f).coerceAtLeast(0.04f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.02f).coerceAtMost(0.18f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Interactive Scrollable Track Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .horizontalScroll(scrollState)
                .pointerInput(totalDurationMs, zoomScale) {
                    detectTapGestures { offset ->
                        val clickedTimeMs = (offset.x / (density * zoomScale)).toLong()
                        onSeek(clickedTimeMs.coerceIn(0L, totalDurationMs))
                    }
                }
                .pointerInput(totalDurationMs, zoomScale) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val draggedTimeMs = (change.position.x / (density * zoomScale)).toLong()
                        onSeek(draggedTimeMs.coerceIn(0L, totalDurationMs))
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
                    .padding(start = 24.dp, end = 120.dp)
            ) {
                // 1. Timecode Ruler & Beat Markers Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(StudioSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val duration = totalDurationMs
                        var sec = 0
                        while (sec * 1000L <= duration + 3000L) {
                            val x = sec * 1000L * zoomScale * density
                            drawLine(
                                color = StudioBorder,
                                start = Offset(x, size.height - 10),
                                end = Offset(x, size.height),
                                strokeWidth = 1.5f
                            )
                            sec += 1
                        }
                    }

                    // Beat sync golden markers
                    beatMarkers.forEach { beat ->
                        val xOffset = (beat.timeMs * zoomScale).dp
                        Box(
                            modifier = Modifier
                                .offset(x = xOffset)
                                .size(6.dp)
                                .background(StudioGold, CircleShape)
                                .align(Alignment.CenterStart)
                        )
                    }
                }

                // 2. Video Clips Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    clips.forEachIndexed { index, clip ->
                        val clipWidth = (clip.effectiveDurationMs * zoomScale).coerceAtLeast(48f).dp
                        val isSelected = index == selectedClipIndex

                        Box(
                            modifier = Modifier
                                .width(clipWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) StudioPrimary.copy(alpha = 0.25f)
                                    else TrackVideoColor.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PlayheadColor else StudioBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectClip(index) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = clip.title,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    if (clip.speed != 1.0f) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StudioGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${clip.speed}x",
                                                color = StudioGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (clip.filter != FilterPreset.NONE) {
                                        Text(
                                            text = "🎨 ${clip.filter.displayName}",
                                            color = StudioGold,
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                    }
                                    if (clip.transition != TransitionType.NONE) {
                                        Text(
                                            text = clip.transition.icon,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "${clip.effectiveDurationMs / 1000f}s",
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Mini gap/transition icon between clips
                        if (index < clips.size - 1) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(StudioSurfaceHover, CircleShape)
                                    .border(1.dp, StudioBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (clips[index].transition != TransitionType.NONE) "⚡" else "•",
                                    fontSize = 8.sp,
                                    color = PlayheadColor
                                )
                            }
                        }
                    }

                    // Add Clip button on track
                    IconButton(
                        onClick = onAddMedia,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .background(StudioSurfaceHover, RoundedCornerShape(8.dp))
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Clip",
                            tint = PlayheadColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 3. Audio Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    audioTracks.forEach { audio ->
                        val audioWidth = (audio.durationMs * zoomScale).coerceAtLeast(60f).dp
                        Box(
                            modifier = Modifier
                                .width(audioWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TrackAudioColor.copy(alpha = 0.25f))
                                .border(1.dp, StudioGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Audio Track",
                                        tint = StudioGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = " ${audio.title}",
                                        color = StudioGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${audio.artist} • ${(audio.volume * 100).toInt()}%",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // 4. Captions & Text Overlays Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(vertical = 2.dp)
                ) {
                    textOverlays.forEach { textOverlay ->
                        val startX = (textOverlay.startMs * zoomScale).dp
                        val width = ((textOverlay.endMs - textOverlay.startMs) * zoomScale).coerceAtLeast(32f).dp

                        Box(
                            modifier = Modifier
                                .offset(x = startX)
                                .width(width)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(TrackTextColor.copy(alpha = 0.35f))
                                .border(1.dp, TrackTextColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Subtitle",
                                    tint = TrackTextColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " ${textOverlay.text}",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 5. Electric Cyan Glowing Playhead Needle (zero relayout penalty)
            Box(
                modifier = Modifier
                    .offset { IntOffset(((24f + (playheadMs * zoomScale)) * density).toInt(), 0) }
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(PlayheadColor)
                    .testTag("timeline_playhead_needle")
            ) {
                // Playhead head icon at top
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = (-5).dp, y = 0.dp)
                        .background(PlayheadColor, RoundedCornerShape(3.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val frames = ((ms % 1000) / 33).coerceIn(0, 29) // 30 fps frames
    return String.format("%02d:%02d.%02d", minutes, seconds, frames)
}
