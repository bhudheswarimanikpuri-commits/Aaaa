package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.AdjustmentSettings
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioAccent
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

@Composable
fun CinematicColorGradingDialog(
    clip: ClipData?,
    onDismiss: () -> Unit,
    onUpdateAdjustments: (AdjustmentSettings) -> Unit,
    onApplyFilter: (FilterPreset, Float) -> Unit,
    onAutoGrade: () -> Unit,
    onApplyToAllClips: () -> Unit,
    onResetGrade: () -> Unit
) {
    if (clip == null) return

    val context = LocalContext.current

    // Local adjustment state initialized from clip
    var currentContrast by remember(clip) { mutableFloatStateOf(clip.adjustments.contrast) }
    var currentSaturation by remember(clip) { mutableFloatStateOf(clip.adjustments.saturation) }
    var currentBrightness by remember(clip) { mutableFloatStateOf(clip.adjustments.brightness) }
    var currentWarmth by remember(clip) { mutableFloatStateOf(clip.adjustments.warmth) }
    var currentTint by remember(clip) { mutableFloatStateOf(clip.adjustments.tint) }
    var currentVignette by remember(clip) { mutableFloatStateOf(clip.adjustments.vignette) }

    var selectedFilter by remember(clip) { mutableStateOf(clip.filter) }
    var filterIntensity by remember(clip) { mutableFloatStateOf(clip.filterIntensity) }

    // Split-screen comparison mode: 0f = full original, 0.5f = half-half, 1f = full graded
    var isSplitCompareMode by remember { mutableStateOf(false) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    // Press and hold to compare raw original
    val holdCompareInteraction = remember { MutableInteractionSource() }
    val isHoldingCompare by holdCompareInteraction.collectIsPressedAsState()

    // Active adjustments object
    val activeAdjustments = remember(currentContrast, currentSaturation, currentBrightness, currentWarmth, currentTint, currentVignette) {
        AdjustmentSettings(
            brightness = currentBrightness,
            contrast = currentContrast,
            saturation = currentSaturation,
            warmth = currentWarmth,
            tint = currentTint,
            vignette = currentVignette
        )
    }

    val gradedColorFilter = remember(selectedFilter, filterIntensity, activeAdjustments, isHoldingCompare) {
        if (isHoldingCompare) null
        else computeGradingColorFilter(selectedFilter, filterIntensity, activeAdjustments)
    }

    fun syncAdjustments() {
        onUpdateAdjustments(activeAdjustments)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioSurface),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Brush.linearGradient(listOf(StudioPrimary, PlayheadColor)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Cinematic Color Grading",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hollywood Master Suite • ${clip.title}",
                                color = PlayheadColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                // 1. Live Visual Preview with Before/After Split Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val drawableId = rememberDrawableRes(clip.drawableResName)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.5.dp, StudioBorder, RoundedCornerShape(12.dp))
                    ) {
                        if (clip.contentUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(clip.contentUri).build(),
                                contentDescription = "Graded Preview",
                                contentScale = ContentScale.Crop,
                                colorFilter = gradedColorFilter,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = if (drawableId != 0) drawableId else R.drawable.scene_cyberpunk_1790386424324),
                                contentDescription = "Graded Preview",
                                contentScale = ContentScale.Crop,
                                colorFilter = gradedColorFilter,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Vignette border
                        if (currentVignette > 0.05f && !isHoldingCompare) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = currentVignette * 0.85f)
                                            )
                                        )
                                    )
                            )
                        }

                        // Split Screen View Comparison Line & Overlay
                        if (isSplitCompareMode && !isHoldingCompare) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, _ ->
                                            change.consume()
                                            splitFraction = (change.position.x / size.width).coerceIn(0.1f, 0.9f)
                                        }
                                    }
                            ) {
                                // Raw left half
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(splitFraction)
                                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                                ) {
                                    if (clip.contentUri != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context).data(clip.contentUri).build(),
                                            contentDescription = "Raw Original",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = if (drawableId != 0) drawableId else R.drawable.scene_cyberpunk_1790386424324),
                                            contentDescription = "Raw Original",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Center Split Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(2.dp)
                                        .align(Alignment.CenterStart)
                                        .offset(x = (splitFraction * 320).dp) // approximate visual tracking
                                        .background(PlayheadColor)
                                )
                            }
                        }

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                            ) {
                                Text(
                                    text = if (isHoldingCompare) "RAW ORIGINAL (PREVIEW)" else if (isSplitCompareMode) "SPLIT: RAW | GRADED" else "CINEMATIC MASTER",
                                    color = if (isHoldingCompare) StudioGold else PlayheadColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = activeAdjustments.temperatureKelvin,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Comparison Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hold to compare button
                    OutlinedButton(
                        onClick = { },
                        interactionSource = holdCompareInteraction,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (isHoldingCompare) " Viewing Raw..." else " Hold for Original",
                            fontSize = 11.sp,
                            color = if (isHoldingCompare) StudioGold else TextPrimary
                        )
                    }

                    // Split view toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSplitCompareMode) StudioPrimary else StudioSurfaceHover,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSplitCompareMode) PlayheadColor else StudioBorder),
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { isSplitCompareMode = !isSplitCompareMode }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = if (isSplitCompareMode) " Split On" else " Split View",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Reset button
                    IconButton(
                        onClick = {
                            currentContrast = 1.0f
                            currentSaturation = 1.0f
                            currentBrightness = 0.0f
                            currentWarmth = 0.0f
                            currentTint = 0.0f
                            currentVignette = 0.0f
                            selectedFilter = FilterPreset.NONE
                            onResetGrade()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Grade", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Real-Time RGB Waveform / Histogram Scope Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F121C))
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                ) {
                    RgbParadeWaveform(
                        contrast = currentContrast,
                        saturation = currentSaturation,
                        brightness = currentBrightness,
                        warmth = currentWarmth,
                        tint = currentTint,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Text(
                        text = "RGB PARADE SCOPE",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Precision Grading Sliders (Contrast, Saturation, Brightness, Color Temperature)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SLIDER 1: CONTRAST
                    GradingSliderItem(
                        icon = Icons.Default.Contrast,
                        title = "Contrast",
                        subtitle = "Dynamic Tone Curve",
                        value = currentContrast,
                        valueRange = 0.5f..1.8f,
                        valueLabel = "${(currentContrast * 100).toInt()}%",
                        neutralValue = 1.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(Color(0xFF555555), Color(0xFF9E9E9E), Color(0xFFFFFFFF))
                        ),
                        accentColor = Color.White,
                        onValueChange = {
                            currentContrast = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentContrast = 1.0f
                            syncAdjustments()
                        }
                    )

                    // SLIDER 2: SATURATION
                    GradingSliderItem(
                        icon = Icons.Default.Tonality,
                        title = "Saturation",
                        subtitle = "Chroma & Color Vibrancy",
                        value = currentSaturation,
                        valueRange = 0.0f..2.0f,
                        valueLabel = "${(currentSaturation * 100).toInt()}%",
                        neutralValue = 1.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(Color(0xFF757575), Color(0xFFFF5252), Color(0xFFFFD600), Color(0xFF00E676), Color(0xFF00E5FF), Color(0xFFE040FB))
                        ),
                        accentColor = StudioPrimary,
                        onValueChange = {
                            currentSaturation = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentSaturation = 1.0f
                            syncAdjustments()
                        }
                    )

                    // SLIDER 3: BRIGHTNESS
                    GradingSliderItem(
                        icon = Icons.Default.Brightness6,
                        title = "Brightness",
                        subtitle = "Luminance & Exposure",
                        value = currentBrightness,
                        valueRange = -0.5f..0.5f,
                        valueLabel = "${(currentBrightness * 200).toInt()}",
                        neutralValue = 0.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(Color(0xFF1A1A1A), Color(0xFF757575), Color(0xFFFFFFFF))
                        ),
                        accentColor = PlayheadColor,
                        onValueChange = {
                            currentBrightness = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentBrightness = 0.0f
                            syncAdjustments()
                        }
                    )

                    // SLIDER 4: COLOR TEMPERATURE (Warmth)
                    GradingSliderItem(
                        icon = Icons.Default.Thermostat,
                        title = "Color Temperature",
                        subtitle = "White Balance (${activeAdjustments.temperatureKelvin})",
                        value = currentWarmth,
                        valueRange = -0.5f..0.5f,
                        valueLabel = if (currentWarmth > 0.02f) "+${(currentWarmth * 200).toInt()} Warm" else if (currentWarmth < -0.02f) "${(currentWarmth * 200).toInt()} Cool" else "Neutral 5500K",
                        neutralValue = 0.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00B0FF), // 3200K Cool Ice Blue
                                Color(0xFF80D8FF),
                                Color(0xFFFFFFFF), // 5500K Daylight
                                Color(0xFFFFD54F),
                                Color(0xFFFF9100)  // 7500K Golden Amber
                            )
                        ),
                        accentColor = StudioGold,
                        onValueChange = {
                            currentWarmth = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentWarmth = 0.0f
                            syncAdjustments()
                        }
                    )

                    // SLIDER 5: TINT (Green to Magenta)
                    GradingSliderItem(
                        icon = Icons.Default.WbSunny,
                        title = "Tint",
                        subtitle = "Emerald Green / Magenta Cast",
                        value = currentTint,
                        valueRange = -0.5f..0.5f,
                        valueLabel = if (currentTint < -0.02f) "${(currentTint * 200).toInt()} Green" else if (currentTint > 0.02f) "+${(currentTint * 200).toInt()} Magenta" else "0 (Neutral)",
                        neutralValue = 0.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00E676), // Green
                                Color(0xFFFFFFFF),
                                Color(0xFFFF007F)  // Magenta
                            )
                        ),
                        accentColor = Color(0xFFFF4081),
                        onValueChange = {
                            currentTint = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentTint = 0.0f
                            syncAdjustments()
                        }
                    )

                    // SLIDER 6: VIGNETTE
                    GradingSliderItem(
                        icon = Icons.Default.Filter,
                        title = "Vignette",
                        subtitle = "Cinematic Lens Edge Falloff",
                        value = currentVignette,
                        valueRange = 0.0f..1.0f,
                        valueLabel = "${(currentVignette * 100).toInt()}%",
                        neutralValue = 0.0f,
                        trackGradient = Brush.horizontalGradient(
                            listOf(Color.White, Color(0xFF616161), Color.Black)
                        ),
                        accentColor = TextSecondary,
                        onValueChange = {
                            currentVignette = it
                            syncAdjustments()
                        },
                        onReset = {
                            currentVignette = 0.0f
                            syncAdjustments()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Cinematic Hollywood LUT Presets
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hollywood Cinematic LUTs",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "LUT Intensity: ${(filterIntensity * 100).toInt()}%",
                            color = PlayheadColor,
                            fontSize = 11.sp
                        )
                    }

                    Slider(
                        value = filterIntensity,
                        onValueChange = {
                            filterIntensity = it
                            onApplyFilter(selectedFilter, it)
                        },
                        colors = SliderDefaults.colors(thumbColor = PlayheadColor, activeTrackColor = StudioPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPreset.values().forEach { preset ->
                            val isSel = preset == selectedFilter
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier.clickable {
                                    selectedFilter = preset
                                    onApplyFilter(preset, filterIntensity)
                                }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = preset.displayName,
                                        color = Color.White,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = preset.description.take(15),
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Pro Actions Bar (AI Auto Grade, Apply to All, Done)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioSurfaceVariant)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI Auto Grade
                    Button(
                        onClick = {
                            onAutoGrade()
                            currentContrast = 1.16f
                            currentSaturation = 1.15f
                            currentBrightness = 0.04f
                            currentWarmth = 0.12f
                            currentTint = -0.04f
                            currentVignette = 0.22f
                            selectedFilter = FilterPreset.TEAL_AND_ORANGE
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceHover),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioGold, modifier = Modifier.size(16.dp))
                        Text(" AI Auto", color = StudioGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Apply to All Clips
                    OutlinedButton(
                        onClick = onApplyToAllClips,
                        modifier = Modifier.weight(1.2f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply to All Clips", fontSize = 11.sp, color = TextPrimary)
                    }

                    // Done Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(" Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradingSliderItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    neutralValue: Float,
    trackGradient: Brush,
    accentColor: Color,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    val isModified = kotlin.math.abs(value - neutralValue) > 0.01f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurfaceHover, RoundedCornerShape(10.dp))
            .border(1.dp, if (isModified) accentColor.copy(alpha = 0.4f) else StudioBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isModified) accentColor else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isModified) accentColor.copy(alpha = 0.18f) else Color(0xFF1E2232)
                ) {
                    Text(
                        text = valueLabel,
                        color = if (isModified) accentColor else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (isModified) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset $title",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Custom gradient slider visual track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(3.dp))
                    .background(trackGradient)
            )

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RgbParadeWaveform(
    contrast: Float,
    saturation: Float,
    brightness: Float,
    warmth: Float,
    tint: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val redShift = (warmth * 18f) + (brightness * 12f) + (tint * 10f)
        val greenShift = (brightness * 12f) - (tint * 15f)
        val blueShift = (-warmth * 20f) + (brightness * 12f) + (tint * 10f)

        fun drawChannelCurve(color: Color, shiftY: Float, variance: Float) {
            val path = Path()
            val points = 32
            val stepX = w / (points - 1)
            for (i in 0 until points) {
                val progress = i.toFloat() / points
                val baseline = h * 0.5f - shiftY
                val wave = kotlin.math.sin(progress * 12f + shiftY) * (8f * contrast * variance)
                val y = (baseline + wave).coerceIn(4f, h - 4f)
                val x = i * stepX
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color.copy(alpha = 0.75f), style = Stroke(width = 2f))
        }

        drawChannelCurve(Color(0xFFFF3366), redShift, saturation)
        drawChannelCurve(Color(0xFF00E676), greenShift, saturation)
        drawChannelCurve(Color(0xFF00E5FF), blueShift, saturation)
    }
}

private fun rememberDrawableRes(name: String?): Int {
    if (name.isNullOrBlank()) return 0
    return when (name) {
        "scene_cyberpunk_1790386424324" -> R.drawable.scene_cyberpunk_1790386424324
        "scene_sunset_coast_1790386436886" -> R.drawable.scene_sunset_coast_1790386436886
        "scene_action_motion_1790386447549" -> R.drawable.scene_action_motion_1790386447549
        "scene_neon_portrait_1790386479877" -> R.drawable.scene_neon_portrait_1790386479877
        else -> 0
    }
}

private fun computeGradingColorFilter(
    preset: FilterPreset,
    intensity: Float,
    adjustments: AdjustmentSettings
): ColorFilter {
    val matrix = ColorMatrix()

    val c = adjustments.contrast
    val b = adjustments.brightness * 128f
    val s = adjustments.saturation

    // Temperature (Kelvin)
    val tempRed = if (adjustments.warmth > 0) adjustments.warmth * 48f else adjustments.warmth * 32f
    val tempBlue = if (adjustments.warmth < 0) -adjustments.warmth * 52f else -adjustments.warmth * 36f
    val tempGreen = adjustments.warmth * 12f

    // Tint
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

    val presetMatrix = when (preset) {
        FilterPreset.TEAL_AND_ORANGE -> ColorMatrix(
            floatArrayOf(
                1.2f, 0.0f, 0.0f, 0f, 20f * intensity,
                0.0f, 1.05f, 0.0f, 0f, 5f * intensity,
                0.0f, 0.1f, 1.25f, 0f, 30f * intensity,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.CYBERPUNK -> ColorMatrix(
            floatArrayOf(
                1.3f, 0.0f, 0.2f, 0f, 25f * intensity,
                0.0f, 0.8f, 0.2f, 0f, 0f,
                0.3f, 0.0f, 1.4f, 0f, 40f * intensity,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.RETRO_FILM -> ColorMatrix(
            floatArrayOf(
                1.15f, 0.1f, 0.0f, 0f, 25f * intensity,
                0.05f, 1.0f, 0.0f, 0f, 15f * intensity,
                0.0f, 0.05f, 0.85f, 0f, -10f * intensity,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.NOIR -> {
            val m = ColorMatrix()
            m.setToSaturation(1f - intensity)
            m
        }
        FilterPreset.GOLDEN_HOUR -> ColorMatrix(
            floatArrayOf(
                1.25f, 0.15f, 0.0f, 0f, 30f * intensity,
                0.1f, 1.15f, 0.0f, 0f, 20f * intensity,
                0.0f, 0.0f, 0.75f, 0f, -20f * intensity,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.VIVID_POP -> {
            val m = ColorMatrix()
            m.setToSaturation(1f + (0.6f * intensity))
            m
        }
        FilterPreset.MATRIX -> ColorMatrix(
            floatArrayOf(
                0.7f, 0.2f, 0.0f, 0f, 0f,
                0.2f, 1.4f, 0.1f, 0f, 35f * intensity,
                0.0f, 0.2f, 0.7f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        FilterPreset.NONE -> null
    }

    if (presetMatrix != null) {
        matrix.timesAssign(presetMatrix)
    }

    return ColorFilter.colorMatrix(matrix)
}
