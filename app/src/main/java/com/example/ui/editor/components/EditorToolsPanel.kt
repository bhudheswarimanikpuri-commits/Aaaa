package com.example.ui.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.TransitionType
import com.example.data.repository.SampleVideoLibrary
import com.example.ui.editor.EditorToolTab
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
fun EditorToolsPanel(
    selectedClip: ClipData?,
    activeTab: EditorToolTab,
    currentAspectRatio: AspectRatioType,
    onTabSelected: (EditorToolTab) -> Unit,
    onSplitClip: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onApplyFilter: (FilterPreset, Float) -> Unit,
    onUpdateAdjustments: (AdjustmentSettings) -> Unit,
    onSetTransition: (TransitionType) -> Unit,
    onSetAspectRatio: (AspectRatioType) -> Unit,
    onAddText: (String) -> Unit,
    onAddSticker: (String, String) -> Unit,
    onAddSoundtrack: (com.example.data.model.AudioTrack) -> Unit,
    onAiTextToVideo: () -> Unit,
    onAiAutoCaptions: () -> Unit,
    onAiBeatSync: () -> Unit,
    onAiAutoEnhance: () -> Unit,
    onOpenColorGradingSuite: () -> Unit,
    onOpenJsEngine: () -> Unit,
    onDuplicateClip: () -> Unit,
    onDeleteClip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(1.dp, StudioBorder)
    ) {
        // Expandable Sub-tool Settings Tray
        AnimatedVisibility(
            visible = activeTab != EditorToolTab.NONE,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurfaceVariant)
                    .padding(12.dp)
            ) {
                when (activeTab) {
                    EditorToolTab.SPEED -> {
                        SpeedSettingsTray(
                            currentSpeed = selectedClip?.speed ?: 1.0f,
                            onSpeedSelected = onSetSpeed
                        )
                    }
                    EditorToolTab.FILTERS -> {
                        FilterSettingsTray(
                            selectedFilter = selectedClip?.filter ?: FilterPreset.NONE,
                            intensity = selectedClip?.filterIntensity ?: 0.85f,
                            onFilterSelected = onApplyFilter
                        )
                    }
                    EditorToolTab.ADJUST -> {
                        AdjustmentsTray(
                            settings = selectedClip?.adjustments ?: AdjustmentSettings(),
                            onAdjustmentsChanged = onUpdateAdjustments,
                            onOpenGradingSuite = onOpenColorGradingSuite
                        )
                    }
                    EditorToolTab.TRANSITIONS -> {
                        TransitionsTray(
                            currentTransition = selectedClip?.transition ?: TransitionType.NONE,
                            onTransitionSelected = onSetTransition
                        )
                    }
                    EditorToolTab.CANVAS_RATIO -> {
                        AspectRatioTray(
                            currentRatio = currentAspectRatio,
                            onRatioSelected = onSetAspectRatio
                        )
                    }
                    EditorToolTab.AUDIO -> {
                        AudioSoundtracksTray(
                            onSelectSoundtrack = onAddSoundtrack
                        )
                    }
                    EditorToolTab.CAPTIONS_TEXT -> {
                        TextCaptionsTray(
                            onAddText = onAddText,
                            onGenerateAiCaptions = onAiAutoCaptions
                        )
                    }
                    EditorToolTab.AI_TOOLS -> {
                        AiToolsTray(
                            onOpenTextToVideo = onAiTextToVideo,
                            onGenerateCaptions = onAiAutoCaptions,
                            onBeatSync = onAiBeatSync,
                            onAutoEnhance = onAiAutoEnhance,
                            onOpenJsEngine = onOpenJsEngine
                        )
                    }
                    EditorToolTab.STICKERS -> {
                        StickersTray(onAddSticker = onAddSticker)
                    }
                    else -> {}
                }
            }
        }

        // Primary Bottom Tools Horizontal Action Bar
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Split Action
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Split",
                isActive = false,
                testTag = "tool_split_button",
                onClick = onSplitClip
            )

            // JavaScript Motion Engine (Explicitly enables JS / Expressions)
            ToolButton(
                icon = Icons.Default.Code,
                label = "JS Engine",
                accentColor = StudioGold,
                isActive = false,
                testTag = "tool_js_engine_button",
                onClick = onOpenJsEngine
            )

            // AI Text to Video (Highlight Star Feature!)
            ToolButton(
                icon = Icons.Default.AutoAwesome,
                label = "AI Magic",
                accentColor = StudioGold,
                isActive = activeTab == EditorToolTab.AI_TOOLS,
                testTag = "tool_ai_magic_button",
                onClick = { onTabSelected(EditorToolTab.AI_TOOLS) }
            )

            // Speed
            ToolButton(
                icon = Icons.Default.Speed,
                label = "Speed",
                isActive = activeTab == EditorToolTab.SPEED,
                testTag = "tool_speed_button",
                onClick = { onTabSelected(EditorToolTab.SPEED) }
            )

            // Filters / LUTs
            ToolButton(
                icon = Icons.Default.Filter,
                label = "Filters",
                isActive = activeTab == EditorToolTab.FILTERS,
                testTag = "tool_filters_button",
                onClick = { onTabSelected(EditorToolTab.FILTERS) }
            )

            // Adjust
            ToolButton(
                icon = Icons.Default.Tune,
                label = "Adjust",
                isActive = activeTab == EditorToolTab.ADJUST,
                testTag = "tool_adjust_button",
                onClick = { onTabSelected(EditorToolTab.ADJUST) }
            )

            // Transitions
            ToolButton(
                icon = Icons.Default.Transform,
                label = "Transition",
                isActive = activeTab == EditorToolTab.TRANSITIONS,
                testTag = "tool_transitions_button",
                onClick = { onTabSelected(EditorToolTab.TRANSITIONS) }
            )

            // Audio & Music
            ToolButton(
                icon = Icons.Default.MusicNote,
                label = "Audio",
                accentColor = StudioGreen,
                isActive = activeTab == EditorToolTab.AUDIO,
                testTag = "tool_audio_button",
                onClick = { onTabSelected(EditorToolTab.AUDIO) }
            )

            // Text & Subtitles
            ToolButton(
                icon = Icons.Default.Subtitles,
                label = "Text/Captions",
                isActive = activeTab == EditorToolTab.CAPTIONS_TEXT,
                testTag = "tool_text_button",
                onClick = { onTabSelected(EditorToolTab.CAPTIONS_TEXT) }
            )

            // Canvas Aspect Ratio
            ToolButton(
                icon = Icons.Default.AspectRatio,
                label = "Ratio",
                isActive = activeTab == EditorToolTab.CANVAS_RATIO,
                testTag = "tool_ratio_button",
                onClick = { onTabSelected(EditorToolTab.CANVAS_RATIO) }
            )

            // Stickers
            ToolButton(
                icon = Icons.Default.EmojiEmotions,
                label = "Stickers",
                isActive = activeTab == EditorToolTab.STICKERS,
                testTag = "tool_stickers_button",
                onClick = { onTabSelected(EditorToolTab.STICKERS) }
            )

            // Duplicate
            ToolButton(
                icon = Icons.Default.ContentCopy,
                label = "Duplicate",
                isActive = false,
                testTag = "tool_duplicate_button",
                onClick = onDuplicateClip
            )

            // Delete
            ToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                accentColor = StudioAccent,
                isActive = false,
                testTag = "tool_delete_button",
                onClick = onDeleteClip
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    testTag: String,
    accentColor: Color = PlayheadColor,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) StudioSurfaceHover else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) accentColor else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isActive) accentColor else TextPrimary,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SpeedSettingsTray(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val presets = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 4.0f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Speed Ramping", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = "${currentSpeed}x ${if (currentSpeed < 1.0f) "(Slow-Mo)" else if (currentSpeed > 1.0f) "(Fast)" else "(Normal)"}",
                color = StudioGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { speed ->
                val isSel = (currentSpeed - speed).let { kotlin.math.abs(it) < 0.05f }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) StudioPrimary else StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                    modifier = Modifier.clickable { onSpeedSelected(speed) }
                ) {
                    Text(
                        text = "${speed}x",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSettingsTray(
    selectedFilter: FilterPreset,
    intensity: Float,
    onFilterSelected: (FilterPreset, Float) -> Unit
) {
    var curIntensity by remember(selectedFilter, intensity) { mutableFloatStateOf(intensity) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cinematic LUTs & Filters", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${(curIntensity * 100).toInt()}%", color = PlayheadColor, fontSize = 12.sp)
        }

        Slider(
            value = curIntensity,
            onValueChange = {
                curIntensity = it
                onFilterSelected(selectedFilter, it)
            },
            colors = SliderDefaults.colors(
                thumbColor = PlayheadColor,
                activeTrackColor = StudioPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPreset.values().forEach { preset ->
                val isSel = preset == selectedFilter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) StudioPrimary else StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                    modifier = Modifier.clickable { onFilterSelected(preset, curIntensity) }
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
                            text = preset.description.take(16),
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjustmentsTray(
    settings: AdjustmentSettings,
    onAdjustmentsChanged: (AdjustmentSettings) -> Unit,
    onOpenGradingSuite: () -> Unit
) {
    var contrast by remember(settings) { mutableFloatStateOf(settings.contrast) }
    var saturation by remember(settings) { mutableFloatStateOf(settings.saturation) }
    var brightness by remember(settings) { mutableFloatStateOf(settings.brightness) }
    var warmth by remember(settings) { mutableFloatStateOf(settings.warmth) }
    var vignette by remember(settings) { mutableFloatStateOf(settings.vignette) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cinematic Color Grading", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = onOpenGradingSuite,
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("open_full_grading_suite_button")
            ) {
                Text("🎬 Open Full Suite", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Contrast Slider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Contrast", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = contrast,
                onValueChange = {
                    contrast = it
                    onAdjustmentsChanged(settings.copy(contrast = it))
                },
                valueRange = 0.5f..1.8f,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = StudioPrimary),
                modifier = Modifier.weight(1f)
            )
            Text("${(contrast * 100).toInt()}%", color = TextPrimary, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        }

        // Saturation Slider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Saturation", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = saturation,
                onValueChange = {
                    saturation = it
                    onAdjustmentsChanged(settings.copy(saturation = it))
                },
                valueRange = 0.0f..2.0f,
                colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary),
                modifier = Modifier.weight(1f)
            )
            Text("${(saturation * 100).toInt()}%", color = TextPrimary, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        }

        // Brightness Slider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Brightness", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = brightness,
                onValueChange = {
                    brightness = it
                    onAdjustmentsChanged(settings.copy(brightness = it))
                },
                valueRange = -0.5f..0.5f,
                colors = SliderDefaults.colors(thumbColor = PlayheadColor, activeTrackColor = PlayheadColor),
                modifier = Modifier.weight(1f)
            )
            Text("${(brightness * 200).toInt()}", color = TextPrimary, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        }

        // Color Temperature (Warmth) Slider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Temperature", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = warmth,
                onValueChange = {
                    warmth = it
                    onAdjustmentsChanged(settings.copy(warmth = it))
                },
                valueRange = -0.5f..0.5f,
                colors = SliderDefaults.colors(thumbColor = StudioGold, activeTrackColor = StudioGold),
                modifier = Modifier.weight(1f)
            )
            Text(settings.copy(warmth = warmth).temperatureKelvin, color = StudioGold, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        }

        // Vignette Slider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Vignette", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = vignette,
                onValueChange = {
                    vignette = it
                    onAdjustmentsChanged(settings.copy(vignette = it))
                },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = TextSecondary, activeTrackColor = TextSecondary),
                modifier = Modifier.weight(1f)
            )
            Text("${(vignette * 100).toInt()}%", color = TextPrimary, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        }
    }
}

@Composable
private fun TransitionsTray(
    currentTransition: TransitionType,
    onTransitionSelected: (TransitionType) -> Unit
) {
    Column {
        Text("Between-Clip Transitions", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransitionType.values().forEach { trans ->
                val isSel = trans == currentTransition
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) StudioPrimary else StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                    modifier = Modifier.clickable { onTransitionSelected(trans) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = trans.icon, fontSize = 16.sp)
                        Text(
                            text = " ${trans.displayName}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectRatioTray(
    currentRatio: AspectRatioType,
    onRatioSelected: (AspectRatioType) -> Unit
) {
    Column {
        Text("Canvas Aspect Ratio & Resolution", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatioType.values().forEach { ratio ->
                val isSel = ratio == currentRatio
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) StudioPrimary else StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                    modifier = Modifier.clickable { onRatioSelected(ratio) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = ratio.label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = ratio.subtitle,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSoundtracksTray(
    onSelectSoundtrack: (com.example.data.model.AudioTrack) -> Unit
) {
    Column {
        Text("Royalty-Free Soundtracks", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SampleVideoLibrary.sampleSoundtracks.forEach { sound ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onSelectSoundtrack(sound) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = StudioGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(text = sound.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${sound.artist} • ${sound.category.label}", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextCaptionsTray(
    onAddText: (String) -> Unit,
    onGenerateAiCaptions: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Text & Subtitles", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Button(
                onClick = onGenerateAiCaptions,
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("✨ AI Auto-Captions", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Enter text overlay...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            )
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onAddText(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(46.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun AiToolsTray(
    onOpenTextToVideo: () -> Unit,
    onGenerateCaptions: () -> Unit,
    onBeatSync: () -> Unit,
    onAutoEnhance: () -> Unit,
    onOpenJsEngine: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioGold, modifier = Modifier.size(18.dp))
            Text(" AI & Motion Superpowers", color = StudioGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text to video feature button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioPrimary,
                border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor),
                modifier = Modifier.clickable { onOpenTextToVideo() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("🎬", fontSize = 16.sp)
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("Text-to-Video AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Veo & Vision Engine", color = PlayheadColor, fontSize = 9.sp)
                    }
                }
            }

            // JavaScript Motion Engine Card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceHover,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioGold),
                modifier = Modifier.clickable { onOpenJsEngine() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("JavaScript Engine", color = StudioGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("JS Motion & Expressions", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceHover,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.clickable { onAutoEnhance() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("🌟", fontSize = 16.sp)
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("Auto HDR Enhance", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Smart Color Boost", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceHover,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.clickable { onBeatSync() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("🎵", fontSize = 16.sp)
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("AI Beat Sync", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Rhythm snap marks", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceHover,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.clickable { onGenerateCaptions() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text("💬", fontSize = 16.sp)
                    Column(modifier = Modifier.padding(start = 6.dp)) {
                        Text("Auto Captions", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Synchronized pills", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StickersTray(
    onAddSticker: (String, String) -> Unit
) {
    val stickers = listOf(
        "🔥" to "FIRE",
        "🎬" to "ACTION",
        "✨" to "MAGIC",
        "🚀" to "SPEED",
        "⚡" to "ENERGY",
        "💎" to "PRO",
        "🏆" to "TOP",
        "❤️" to "LOVE"
    )

    Column {
        Text("Overlay Stickers & Badges", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            stickers.forEach { (emoji, label) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioSurfaceHover,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.clickable { onAddSticker(emoji, label) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                        Text(text = label, color = TextSecondary, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
