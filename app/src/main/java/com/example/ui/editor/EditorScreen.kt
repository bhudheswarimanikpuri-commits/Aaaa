package com.example.ui.editor

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import com.example.ui.ai.AiTextToVideoDialog
import com.example.ui.editor.components.CinematicColorGradingDialog
import com.example.ui.editor.components.JavaScriptEngineDialog
import com.example.ui.editor.components.AddMediaDialog
import com.example.ui.editor.components.EditorToolsPanel
import com.example.ui.editor.components.TimelineTrackView
import com.example.ui.editor.components.VideoCanvasPreview
import com.example.ui.export.ExportDialog
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHover
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectId) {
        if (projectId.isNotBlank()) {
            viewModel.loadProject(projectId)
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    BackHandler {
        viewModel.pausePlayback()
        onNavigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.projectTitle,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${uiState.aspectRatio.label} • ${uiState.clips.size} clips • ${(uiState.totalDurationMs / 1000f)}s",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.pausePlayback()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // JavaScript Enable / Console Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.isJsEnabled) StudioGold.copy(alpha = 0.2f) else StudioSurfaceHover,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isJsEnabled) StudioGold else StudioBorder),
                        modifier = Modifier
                            .clickable { viewModel.setJsDialogVisible(true) }
                            .padding(end = 6.dp)
                            .testTag("top_js_engine_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "JavaScript Engine",
                                tint = if (uiState.isJsEnabled) StudioGold else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (uiState.isJsEnabled) " JS: ON" else " JS: OFF",
                                color = if (uiState.isJsEnabled) StudioGold else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Color Grade Quick Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioSurfaceHover,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier
                            .clickable { viewModel.setColorGradingDialogVisible(true) }
                            .padding(end = 6.dp)
                            .testTag("top_color_grade_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Color Grade",
                                tint = PlayheadColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " Grade",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // AI Quick Generator Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioPrimary.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PlayheadColor),
                        modifier = Modifier
                            .clickable { viewModel.setAiTextDialogVisible(true) }
                            .padding(end = 8.dp)
                            .testTag("top_ai_text_to_video_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Text to Video",
                                tint = StudioGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = " AI Video",
                                color = PlayheadColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Export Button
                    Button(
                        onClick = { viewModel.setExportDialogVisible(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("top_export_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(16.dp))
                        Text(" Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurface
                )
            )
        },
        containerColor = StudioBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Center Video Preview Canvas with active overlays and filters
            VideoCanvasPreview(
                clip = uiState.activeClipAtPlayhead,
                aspectRatio = uiState.aspectRatio,
                playheadMs = uiState.playheadMs,
                isPlaying = uiState.isPlaying,
                activeTexts = uiState.activeTextOverlays,
                activeStickers = uiState.activeStickers,
                jsTransform = uiState.jsTransform,
                isJsEnabled = uiState.isJsEnabled,
                onTogglePlay = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 2. Playback Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.seekTo(0L) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = "Replay", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { viewModel.stepFrame(-1000L) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Step Back", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(StudioPrimary)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.stepFrame(1000L) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Step Forward", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    // Split at playhead shortcut
                    IconButton(
                        onClick = { viewModel.splitClipAtPlayhead() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✂️", fontSize = 16.sp)
                    }
                }
            }

            // 3. Multi-track Timeline View
            TimelineTrackView(
                clips = uiState.clips,
                selectedClipIndex = uiState.selectedClipIndex,
                audioTracks = uiState.audioTracks,
                textOverlays = uiState.textOverlays,
                beatMarkers = uiState.beatMarkers,
                playheadMs = uiState.playheadMs,
                totalDurationMs = uiState.totalDurationMs,
                isPlaying = uiState.isPlaying,
                onSeek = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onAddMedia = { viewModel.setAddMediaDialogVisible(true) },
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Bottom Drawer Tools Panel
            EditorToolsPanel(
                selectedClip = uiState.selectedClip,
                activeTab = uiState.activeToolTab,
                currentAspectRatio = uiState.aspectRatio,
                onTabSelected = { viewModel.setToolTab(it) },
                onSplitClip = { viewModel.splitClipAtPlayhead() },
                onSetSpeed = { viewModel.setClipSpeed(it) },
                onApplyFilter = { preset, intensity -> viewModel.applyFilter(preset, intensity) },
                onUpdateAdjustments = { viewModel.updateAdjustments(it) },
                onSetTransition = { viewModel.setTransition(it) },
                onSetAspectRatio = { viewModel.setAspectRatio(it) },
                onAddText = { viewModel.addTextOverlay(it) },
                onAddSticker = { emoji, label -> viewModel.addSticker(emoji, label) },
                onAddSoundtrack = { viewModel.addAudioTrack(it) },
                onAiTextToVideo = { viewModel.setAiTextDialogVisible(true) },
                onAiAutoCaptions = { viewModel.generateAiAutoCaptions() },
                onAiBeatSync = { viewModel.generateAiBeatMarkers() },
                onAiAutoEnhance = { viewModel.aiAutoEnhanceColors() },
                onOpenColorGradingSuite = { viewModel.setColorGradingDialogVisible(true) },
                onOpenJsEngine = { viewModel.setJsDialogVisible(true) },
                onDuplicateClip = { viewModel.duplicateSelectedClip() },
                onDeleteClip = { viewModel.deleteSelectedClip() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // JavaScript Motion & Expression Engine Modal
    if (uiState.showJsDialog) {
        JavaScriptEngineDialog(
            isJsEnabled = uiState.isJsEnabled,
            currentScript = uiState.jsCurrentScript,
            onToggleJs = { viewModel.toggleJsEnabled(it) },
            onSaveScript = { viewModel.setJsScript(it) },
            onTestRun = { code, onOutput -> viewModel.testRunJs(code, onOutput) },
            onDismiss = { viewModel.setJsDialogVisible(false) }
        )
    }

    // Cinematic Color Grading Suite Modal
    if (uiState.showColorGradingDialog) {
        CinematicColorGradingDialog(
            clip = uiState.selectedClip ?: uiState.activeClipAtPlayhead,
            onDismiss = { viewModel.setColorGradingDialogVisible(false) },
            onUpdateAdjustments = { viewModel.updateAdjustments(it) },
            onApplyFilter = { preset, intensity -> viewModel.applyFilter(preset, intensity) },
            onAutoGrade = { viewModel.aiAutoColorGrade() },
            onApplyToAllClips = { viewModel.applyGradeToAllClips() },
            onResetGrade = { viewModel.resetGradingForSelectedClip() }
        )
    }

    // AI Text-To-Video Generator Modal
    if (uiState.showAiTextDialog) {
        AiTextToVideoDialog(
            isGenerating = uiState.isAiGenerating,
            progressText = uiState.aiProgressText,
            progressFraction = uiState.aiProgressFraction,
            onDismiss = { viewModel.setAiTextDialogVisible(false) },
            onGenerate = { prompt, style, aspect, motion ->
                viewModel.generateTextToVideo(prompt, style, aspect, motion)
            }
        )
    }

    // Export Modal
    if (uiState.showExportDialog) {
        ExportDialog(
            durationMs = uiState.totalDurationMs,
            isExporting = uiState.isExporting,
            exportProgress = uiState.exportProgress,
            exportComplete = uiState.exportComplete,
            onDismiss = { viewModel.dismissExportDialog() },
            onStartExport = { res, fps, qual ->
                viewModel.startExport(res, fps, qual)
            }
        )
    }

    // Add Media Modal
    if (uiState.showAddMediaDialog) {
        AddMediaDialog(
            onDismiss = { viewModel.setAddMediaDialogVisible(false) },
            onSelectSampleClip = { title, resName ->
                viewModel.addClip(title, resName, null)
            },
            onMediaUrisSelected = { uris ->
                uris.forEachIndexed { i, uri ->
                    viewModel.addClip("Imported Clip ${i + 1}", null, uri.toString())
                }
            },
            onOpenAiTextToVideo = {
                viewModel.setAiTextDialogVisible(true)
            }
        )
    }
}
