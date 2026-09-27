package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.StickerOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioPrimary

@Composable
fun VideoCanvasPreview(
    clip: ClipData?,
    aspectRatio: AspectRatioType,
    playheadMs: Long,
    isPlaying: Boolean,
    activeTexts: List<TextOverlay>,
    activeStickers: List<StickerOverlay>,
    jsTransform: com.example.data.js.JsFrameTransform = com.example.data.js.JsFrameTransform(),
    isJsEnabled: Boolean = false,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Compute dynamic camera motion (subtle cinematic Ken Burns pan/zoom when playing)
    val motionProgress = (playheadMs % 4000L).toFloat() / 4000f
    val cameraScale = if (isPlaying) 1.0f + (motionProgress * 0.08f) else 1.0f
    val cameraPanX = if (isPlaying) (motionProgress - 0.5f) * 12f else 0f

    // Color grading filter matrix
    val colorFilter = clip?.let { getFilterColorFilter(it.filter, it.filterIntensity, it.adjustments) }

    Box(
        modifier = modifier
            .background(StudioBackground)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Aspect ratio container (e.g. 9:16 vertical or 16:9 widescreen)
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio.ratioFloat)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, Color(0xFF242A3D), RoundedCornerShape(12.dp))
                .clickable { onTogglePlay() }
                .testTag("video_canvas_preview"),
            contentAlignment = Alignment.Center
        ) {
            if (clip != null) {
                val drawableId = rememberDrawableResId(clip.drawableResName)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val finalScale = cameraScale * (if (isJsEnabled) jsTransform.scaleMultiplier else 1.0f)
                            scaleX = finalScale
                            scaleY = finalScale
                            translationX = cameraPanX + (if (isJsEnabled) jsTransform.translationX else 0f)
                            translationY = (if (isJsEnabled) jsTransform.translationY else 0f)
                            rotationZ = (if (isJsEnabled) jsTransform.rotationDeg else 0f)
                        }
                ) {
                    if (clip.contentUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(clip.contentUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = clip.title,
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = clip.title,
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback sample
                        Image(
                            painter = painterResource(id = R.drawable.scene_cyberpunk_1790386424324),
                            contentDescription = "Default Scene",
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // JavaScript Glitch effect overlay if active
                    if (isJsEnabled && jsTransform.glitchIntensity > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(StudioPrimary.copy(alpha = jsTransform.glitchIntensity * 0.45f))
                        )
                    }

                    // Vignette Overlay if set
                    if (clip.adjustments.vignette > 0.05f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = clip.adjustments.vignette * 0.8f)
                                        )
                                    )
                                )
                        )
                    }

                    // Transition overlay simulation
                    when (clip.transition) {
                        TransitionType.FLASH_WHITE -> {
                            val flashAlpha = if (playheadMs % 4000L < 250L) 0.65f else 0f
                            if (flashAlpha > 0f) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha)))
                            }
                        }
                        TransitionType.GLITCH_CUT -> {
                            val glitchActive = (playheadMs % 4000L in 0L..180L)
                            if (glitchActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(StudioPrimary.copy(alpha = 0.35f))
                                )
                            }
                        }
                        TransitionType.FADE_BLACK -> {
                            val fadeAlpha = if (playheadMs % 4000L < 300L) 0.8f else 0f
                            if (fadeAlpha > 0f) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = fadeAlpha)))
                            }
                        }
                        else -> {}
                    }
                }

                // Render dynamic text overlays & subtitles
                Box(modifier = Modifier.fillMaxSize()) {
                    activeTexts.forEach { textOverlay ->
                        val animScale by animateFloatAsState(
                            targetValue = 1.0f,
                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                            label = "text_pop"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (textOverlay.posY * 300).dp)
                                .graphicsLayer {
                                    scaleX = animScale
                                    scaleY = animScale
                                }
                                .padding(horizontal = 16.dp)
                        ) {
                            if (textOverlay.isCaption) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor.copy(alpha = 0.6f)),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = textOverlay.text,
                                        color = parseHexColor(textOverlay.colorHex),
                                        fontSize = textOverlay.fontSizeSp.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = textOverlay.text,
                                    color = parseHexColor(textOverlay.colorHex),
                                    fontSize = textOverlay.fontSizeSp.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Render active stickers
                    activeStickers.forEach { sticker ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(
                                    x = ((sticker.posX - 0.5f) * 200).dp,
                                    y = (sticker.posY * 300).dp
                                )
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = sticker.emoji,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No clips in timeline\nTap + to import media or generate with AI",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }

            // Top Badges (Aspect Ratio, Resolution & JS Engine)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333A52))
                    ) {
                        Text(
                            text = "${aspectRatio.label} • 4K 60FPS",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isJsEnabled) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StudioGold.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioGold),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "⚡ JS ENABLED",
                                color = StudioGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Center Play/Pause indicator icon on tap
            AnimatedVisibility(
                visible = !isPlaying,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.5.dp, PlayheadColor.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Preview",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

private fun rememberDrawableResId(name: String?): Int {
    if (name.isNullOrBlank()) return 0
    return when (name) {
        "scene_cyberpunk_1790386424324" -> R.drawable.scene_cyberpunk_1790386424324
        "scene_sunset_coast_1790386436886" -> R.drawable.scene_sunset_coast_1790386436886
        "scene_action_motion_1790386447549" -> R.drawable.scene_action_motion_1790386447549
        "scene_neon_portrait_1790386479877" -> R.drawable.scene_neon_portrait_1790386479877
        else -> 0
    }
}

private fun getFilterColorFilter(
    preset: FilterPreset,
    intensity: Float,
    adjustments: AdjustmentSettings
): ColorFilter? {
    val matrix = ColorMatrix()

    // 1. Base adjustments (Brightness, Contrast, Saturation, Color Temperature, Tint)
    val c = adjustments.contrast
    val b = adjustments.brightness * 128f
    val s = adjustments.saturation

    // Color Temperature Kelvin shift: positive = Warm Golden/Red, negative = Cool Ice Blue
    val tempRed = if (adjustments.warmth > 0) adjustments.warmth * 48f else adjustments.warmth * 32f
    val tempBlue = if (adjustments.warmth < 0) -adjustments.warmth * 52f else -adjustments.warmth * 36f
    val tempGreen = adjustments.warmth * 12f

    // Tint: negative = Emerald Green, positive = Neon Magenta
    val tintRed = adjustments.tint * 25f
    val tintGreen = -adjustments.tint * 38f
    val tintBlue = adjustments.tint * 25f

    val baseArray = floatArrayOf(
        c, 0f, 0f, 0f, b + tempRed + tintRed,
        0f, c, 0f, 0f, b + tempGreen + tintGreen,
        0f, 0f, c, 0f, b + tempBlue + tintBlue,
        0f, 0f, 0f, 1f, 0f
    )
    matrix.set(ColorMatrix(baseArray))
    matrix.setToSaturation(s)

    // 2. Preset LUT matrix
    val presetMatrix = when (preset) {
        FilterPreset.TEAL_AND_ORANGE -> ColorMatrix(
            floatArrayOf(
                1.2f, 0.0f, 0.0f, 0f, 20f,
                0.0f, 1.05f, 0.0f, 0f, 5f,
                0.0f, 0.1f, 1.25f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.CYBERPUNK -> ColorMatrix(
            floatArrayOf(
                1.3f, 0.0f, 0.2f, 0f, 25f,
                0.0f, 0.8f, 0.2f, 0f, 0f,
                0.3f, 0.0f, 1.4f, 0f, 40f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.RETRO_FILM -> ColorMatrix(
            floatArrayOf(
                1.15f, 0.1f, 0.0f, 0f, 25f,
                0.05f, 1.0f, 0.0f, 0f, 15f,
                0.0f, 0.05f, 0.85f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.NOIR -> {
            val m = ColorMatrix()
            m.setToSaturation(0f)
            m
        }
        FilterPreset.GOLDEN_HOUR -> ColorMatrix(
            floatArrayOf(
                1.25f, 0.15f, 0.0f, 0f, 30f,
                0.1f, 1.15f, 0.0f, 0f, 20f,
                0.0f, 0.0f, 0.75f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.VIVID_POP -> {
            val m = ColorMatrix()
            m.setToSaturation(1.5f)
            m
        }
        FilterPreset.MATRIX -> ColorMatrix(
            floatArrayOf(
                0.7f, 0.2f, 0.0f, 0f, 0f,
                0.2f, 1.4f, 0.1f, 0f, 35f,
                0.0f, 0.2f, 0.7f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.NONE -> null
    }

    if (presetMatrix != null) {
        // Blend preset
        matrix.timesAssign(presetMatrix)
    }

    return ColorFilter.colorMatrix(matrix)
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.White
    }
}
