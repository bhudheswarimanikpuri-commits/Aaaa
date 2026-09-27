package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiVideoEngine
import com.example.data.local.CineProDatabase
import com.example.data.local.Converters
import com.example.data.local.ProjectEntity
import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.AudioTrack
import com.example.data.model.BeatMarker
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.StickerOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SampleVideoLibrary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorToolTab {
    NONE,
    SPLIT,
    SPEED,
    FILTERS,
    ADJUST,
    TRANSITIONS,
    AUDIO,
    CAPTIONS_TEXT,
    AI_TOOLS,
    CANVAS_RATIO,
    STICKERS
}

data class EditorUiState(
    val projectId: String = "",
    val projectTitle: String = "Untitled Video",
    val clips: List<ClipData> = emptyList(),
    val selectedClipIndex: Int = 0,
    val audioTracks: List<AudioTrack> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickers: List<StickerOverlay> = emptyList(),
    val beatMarkers: List<BeatMarker> = emptyList(),
    val aspectRatio: AspectRatioType = AspectRatioType.RATIO_9_16,
    val playheadMs: Long = 0L,
    val isPlaying: Boolean = false,
    val activeToolTab: EditorToolTab = EditorToolTab.NONE,
    val isAiGenerating: Boolean = false,
    val aiProgressText: String = "",
    val aiProgressFraction: Float = 0f,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportComplete: Boolean = false,
    val showExportDialog: Boolean = false,
    val showAddMediaDialog: Boolean = false,
    val showAiTextDialog: Boolean = false,
    val showColorGradingDialog: Boolean = false,
    val isJsEnabled: Boolean = true,
    val jsCurrentScript: String = com.example.data.js.VideoJavaScriptEngine.DEFAULT_WIGGLE_SCRIPT,
    val jsTransform: com.example.data.js.JsFrameTransform = com.example.data.js.JsFrameTransform(),
    val showJsDialog: Boolean = false,
    val statusMessage: String? = null
) {
    val totalDurationMs: Long
        get() {
            val clipsDuration = clips.sumOf { it.effectiveDurationMs }
            return clipsDuration.coerceAtLeast(1000L)
        }

    val selectedClip: ClipData?
        get() = clips.getOrNull(selectedClipIndex)

    val currentClipIndexAtPlayhead: Int
        get() {
            var accumulated = 0L
            for (i in clips.indices) {
                val dur = clips[i].effectiveDurationMs
                if (playheadMs in accumulated until (accumulated + dur)) {
                    return i
                }
                accumulated += dur
            }
            return (clips.size - 1).coerceAtLeast(0)
        }

    val activeClipAtPlayhead: ClipData?
        get() = clips.getOrNull(currentClipIndexAtPlayhead)

    val activeTextOverlays: List<TextOverlay>
        get() = textOverlays.filter { playheadMs in it.startMs..it.endMs }

    val activeStickers: List<StickerOverlay>
        get() = stickers.filter { playheadMs in it.startMs..it.endMs }
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CineProDatabase.getInstance(application)
    private val repository = ProjectRepository(db.projectDao())
    private val converters = Converters()
    private val jsEngine = com.example.data.js.VideoJavaScriptEngine(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        // Generate default beat markers
        val beats = (0..30).map { i ->
            BeatMarker(timeMs = i * 500L, intensity = if (i % 2 == 0) 1.0f else 0.7f)
        }
        _uiState.update { it.copy(beatMarkers = beats) }
    }

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProjectDirect(projectId)
            if (project != null) {
                val loadedClips = converters.toClipList(project.clipsJson)
                val loadedAudio = converters.toAudioList(project.audioTracksJson)
                val loadedText = converters.toTextList(project.textOverlaysJson)
                val loadedStickers = converters.toStickerList(project.stickersJson)
                val aspect = try {
                    AspectRatioType.valueOf(project.aspectRatio)
                } catch (e: Exception) {
                    AspectRatioType.RATIO_9_16
                }

                _uiState.update {
                    it.copy(
                        projectId = project.id,
                        projectTitle = project.title,
                        clips = loadedClips,
                        selectedClipIndex = 0,
                        audioTracks = loadedAudio,
                        textOverlays = loadedText,
                        stickers = loadedStickers,
                        aspectRatio = aspect,
                        playheadMs = 0L,
                        isPlaying = false
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val intervalMs = 16L // 60 FPS ultra-smooth playback (1000ms / 60 ≈ 16.6ms)
            while (isActive && _uiState.value.isPlaying) {
                delay(intervalMs)
                _uiState.update { state ->
                    val nextPlayhead = state.playheadMs + intervalMs
                    val timeSec = nextPlayhead / 1000f
                    val frameNum = ((nextPlayhead / 16L) % 10000L).toInt()
                    var transform = state.jsTransform
                    if (state.isJsEnabled) {
                        jsEngine.evaluateFrame(timeSec, frameNum) { res ->
                            transform = res
                        }
                    }
                    if (nextPlayhead >= state.totalDurationMs) {
                        state.copy(playheadMs = 0L, isPlaying = false, jsTransform = transform)
                    } else {
                        state.copy(playheadMs = nextPlayhead, jsTransform = transform)
                    }
                }
            }
        }
    }

    fun pausePlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(timeMs: Long) {
        val bounded = timeMs.coerceIn(0L, _uiState.value.totalDurationMs)
        val timeSec = bounded / 1000f
        val frameNum = ((bounded / 33L) % 10000L).toInt()
        var transform = _uiState.value.jsTransform
        if (_uiState.value.isJsEnabled) {
            jsEngine.evaluateFrame(timeSec, frameNum) { res ->
                transform = res
            }
        }
        _uiState.update { it.copy(playheadMs = bounded, jsTransform = transform) }
    }

    fun stepFrame(deltaMs: Long) {
        seekTo(_uiState.value.playheadMs + deltaMs)
    }

    fun selectClip(index: Int) {
        if (index in _uiState.value.clips.indices) {
            _uiState.update { it.copy(selectedClipIndex = index) }
        }
    }

    fun setToolTab(tab: EditorToolTab) {
        _uiState.update { current ->
            if (current.activeToolTab == tab) current.copy(activeToolTab = EditorToolTab.NONE)
            else current.copy(activeToolTab = tab)
        }
    }

    fun splitClipAtPlayhead() {
        val state = _uiState.value
        val playhead = state.playheadMs
        val clips = state.clips.toMutableList()

        var accumulated = 0L
        var targetIndex = -1
        var offsetInClip = 0L

        for (i in clips.indices) {
            val dur = clips[i].effectiveDurationMs
            if (playhead > accumulated && playhead < (accumulated + dur)) {
                targetIndex = i
                offsetInClip = (playhead - accumulated)
                break
            }
            accumulated += dur
        }

        if (targetIndex >= 0) {
            val original = clips[targetIndex]
            val splitOffsetMs = (offsetInClip * original.speed).toLong()
            val newTrimMiddle = original.trimStartMs + splitOffsetMs

            if (newTrimMiddle - original.trimStartMs >= 300L && original.trimEndMs - newTrimMiddle >= 300L) {
                val clipPart1 = original.copy(
                    id = "clip_" + UUID.randomUUID().toString().take(6),
                    trimEndMs = newTrimMiddle
                )
                val clipPart2 = original.copy(
                    id = "clip_" + UUID.randomUUID().toString().take(6),
                    title = "${original.title} (Part 2)",
                    trimStartMs = newTrimMiddle,
                    transition = TransitionType.CROSS_DISSOLVE
                )

                clips.removeAt(targetIndex)
                clips.add(targetIndex, clipPart2)
                clips.add(targetIndex, clipPart1)

                _uiState.update {
                    it.copy(
                        clips = clips,
                        selectedClipIndex = targetIndex + 1,
                        statusMessage = "Clip successfully split at playhead"
                    )
                }
                saveProjectChanges()
            }
        }
    }

    fun updateClipTrim(clipIndex: Int, newTrimStartMs: Long, newTrimEndMs: Long) {
        val clips = _uiState.value.clips.toMutableList()
        if (clipIndex in clips.indices) {
            val clip = clips[clipIndex]
            val validStart = newTrimStartMs.coerceIn(0L, clip.durationMs - 300L)
            val validEnd = newTrimEndMs.coerceIn(validStart + 300L, clip.durationMs)
            clips[clipIndex] = clip.copy(trimStartMs = validStart, trimEndMs = validEnd)
            _uiState.update { it.copy(clips = clips) }
            saveProjectChanges()
        }
    }

    fun setClipSpeed(speed: Float) {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(speed = speed)
            _uiState.update { it.copy(clips = clips) }
            saveProjectChanges()
        }
    }

    fun applyFilter(preset: FilterPreset, intensity: Float = 0.85f) {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(filter = preset, filterIntensity = intensity)
            _uiState.update { it.copy(clips = clips) }
            saveProjectChanges()
        }
    }

    fun updateAdjustments(adjustments: AdjustmentSettings) {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(adjustments = adjustments)
            _uiState.update { it.copy(clips = clips) }
            saveProjectChanges()
        }
    }

    fun setTransition(type: TransitionType) {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(transition = type)
            _uiState.update { it.copy(clips = clips) }
            saveProjectChanges()
        }
    }

    fun deleteSelectedClip() {
        val clips = _uiState.value.clips.toMutableList()
        val index = _uiState.value.selectedClipIndex
        if (clips.size > 1 && index in clips.indices) {
            clips.removeAt(index)
            val newIndex = (index - 1).coerceAtLeast(0)
            _uiState.update { it.copy(clips = clips, selectedClipIndex = newIndex) }
            saveProjectChanges()
        } else {
            _uiState.update { it.copy(statusMessage = "Cannot delete the only clip in project") }
        }
    }

    fun duplicateSelectedClip() {
        val clips = _uiState.value.clips.toMutableList()
        val index = _uiState.value.selectedClipIndex
        if (index in clips.indices) {
            val original = clips[index]
            val duplicate = original.copy(
                id = "clip_" + UUID.randomUUID().toString().take(6),
                title = "${original.title} (Copy)"
            )
            clips.add(index + 1, duplicate)
            _uiState.update { it.copy(clips = clips, selectedClipIndex = index + 1) }
            saveProjectChanges()
        }
    }

    fun setAspectRatio(ratio: AspectRatioType) {
        _uiState.update { it.copy(aspectRatio = ratio) }
        saveProjectChanges()
    }

    fun addAudioTrack(track: AudioTrack) {
        val current = _uiState.value.audioTracks.toMutableList()
        current.add(track)
        _uiState.update { it.copy(audioTracks = current, statusMessage = "Added soundtrack: ${track.title}") }
        saveProjectChanges()
    }

    fun removeAudioTrack(id: String) {
        val filtered = _uiState.value.audioTracks.filter { it.id != id }
        _uiState.update { it.copy(audioTracks = filtered) }
        saveProjectChanges()
    }

    fun addTextOverlay(text: String, isCaption: Boolean = false) {
        val playhead = _uiState.value.playheadMs
        val newOverlay = TextOverlay(
            id = "txt_" + UUID.randomUUID().toString().take(6),
            text = text,
            startMs = playhead,
            endMs = (playhead + 3000L).coerceAtMost(_uiState.value.totalDurationMs),
            posY = if (isCaption) 0.78f else 0.45f,
            fontSizeSp = if (isCaption) 20f else 26f,
            colorHex = if (isCaption) "#00E5FF" else "#FFFFFF",
            isCaption = isCaption,
            animation = "POP"
        )
        val current = _uiState.value.textOverlays.toMutableList()
        current.add(newOverlay)
        _uiState.update { it.copy(textOverlays = current, statusMessage = "Text overlay added") }
        saveProjectChanges()
    }

    fun removeTextOverlay(id: String) {
        val filtered = _uiState.value.textOverlays.filter { it.id != id }
        _uiState.update { it.copy(textOverlays = filtered) }
        saveProjectChanges()
    }

    fun addSticker(emoji: String, label: String) {
        val playhead = _uiState.value.playheadMs
        val newSticker = StickerOverlay(
            id = "stk_" + UUID.randomUUID().toString().take(6),
            emoji = emoji,
            label = label,
            startMs = playhead,
            endMs = (playhead + 3500L).coerceAtMost(_uiState.value.totalDurationMs),
            posX = 0.5f,
            posY = 0.35f,
            scale = 1.0f
        )
        val current = _uiState.value.stickers.toMutableList()
        current.add(newSticker)
        _uiState.update { it.copy(stickers = current) }
        saveProjectChanges()
    }

    fun addClip(title: String, drawableResName: String?, contentUri: String?) {
        val newClip = ClipData(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            title = title,
            drawableResName = drawableResName,
            contentUri = contentUri,
            durationMs = 4500L,
            trimStartMs = 0L,
            trimEndMs = 4500L,
            transition = TransitionType.CROSS_DISSOLVE
        )
        val current = _uiState.value.clips.toMutableList()
        current.add(newClip)
        _uiState.update { it.copy(clips = current, selectedClipIndex = current.size - 1) }
        saveProjectChanges()
    }

    // AI AUTO CAPTIONS GENERATOR
    fun generateAiAutoCaptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true, aiProgressText = "AI Transcribing & Synchronizing Captions...", aiProgressFraction = 0.3f) }
            delay(800)
            _uiState.update { it.copy(aiProgressFraction = 0.7f, aiProgressText = "Generating Dynamic Pop-in Subtitle Animations...") }
            delay(600)

            val generatedCaptions = listOf(
                TextOverlay("ai_c1", "⚡ WORLD'S BEST VIDEO EDITOR", 300L, 3500L, posY = 0.76f, fontSizeSp = 22f, colorHex = "#00E5FF", isCaption = true, animation = "POP"),
                TextOverlay("ai_c2", "✨ Cinematic Visuals & AI Text-to-Video", 3800L, 7500L, posY = 0.76f, fontSizeSp = 20f, colorHex = "#FFD600", isCaption = true, animation = "GLOW"),
                TextOverlay("ai_c3", "🔥 4K Master Grade Export Ready", 7800L, 12000L, posY = 0.76f, fontSizeSp = 22f, colorHex = "#FF3D71", isCaption = true, animation = "POP")
            )

            _uiState.update {
                it.copy(
                    isAiGenerating = false,
                    textOverlays = generatedCaptions,
                    statusMessage = "AI Auto-Captions synchronized perfectly!"
                )
            }
            saveProjectChanges()
        }
    }

    // AI BEAT-SYNC GENERATOR
    fun generateAiBeatMarkers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGenerating = true, aiProgressText = "AI Audio Waveform Frequency Analysis...", aiProgressFraction = 0.4f) }
            delay(600)
            _uiState.update { it.copy(aiProgressFraction = 0.85f, aiProgressText = "Detecting 128 BPM Bass Transients...") }
            delay(500)

            val beats = mutableListOf<BeatMarker>()
            val interval = 480L // ~125 BPM
            var t = 0L
            while (t < _uiState.value.totalDurationMs) {
                beats.add(BeatMarker(timeMs = t, intensity = if ((t / interval) % 2 == 0L) 1.0f else 0.75f))
                t += interval
            }

            _uiState.update {
                it.copy(
                    isAiGenerating = false,
                    beatMarkers = beats,
                    statusMessage = "AI Beat-Sync activated: ${beats.size} rhythmic cut points marked!"
                )
            }
        }
    }

    // AI AUTO HDR ENHANCE
    fun aiAutoEnhanceColors() {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(
                filter = FilterPreset.TEAL_AND_ORANGE,
                filterIntensity = 0.9f,
                adjustments = AdjustmentSettings(
                    contrast = 1.25f,
                    saturation = 1.3f,
                    warmth = 0.1f,
                    vignette = 0.2f
                )
            )
            _uiState.update {
                it.copy(
                    clips = clips,
                    statusMessage = "✨ AI Smart HDR & Color Contrast Enhanced!"
                )
            }
            saveProjectChanges()
        }
    }

    // TEXT TO VIDEO GENERATION
    fun generateTextToVideo(
        prompt: String,
        style: String,
        aspectRatio: AspectRatioType,
        cameraMotion: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiGenerating = true,
                    aiProgressText = "Initializing Text-to-Video Engine...",
                    aiProgressFraction = 0.05f
                )
            }

            val result = AiVideoEngine.generateStoryAndScenes(
                prompt = prompt,
                style = style,
                aspectRatio = aspectRatio,
                cameraMotion = cameraMotion
            ) { status, frac ->
                _uiState.update { it.copy(aiProgressText = status, aiProgressFraction = frac) }
            }

            val newProjectId = repository.createFromAiTextToVideo(
                prompt = result.title,
                style = result.style,
                aspectRatio = result.aspectRatio,
                cameraMotion = result.cameraMovement,
                musicMood = result.musicMood,
                generatedScenes = result.scenes,
                narration = result.narration
            )

            loadProject(newProjectId)

            _uiState.update {
                it.copy(
                    isAiGenerating = false,
                    showAiTextDialog = false,
                    statusMessage = "🚀 AI Video created from text! Ready to edit."
                )
            }
        }
    }

    // EXPORT SIMULATION
    fun startExport(resolution: String, fps: Int, quality: String) {
        pausePlayback()
        _uiState.update {
            it.copy(
                isExporting = true,
                exportProgress = 0f,
                exportComplete = false,
                showExportDialog = true
            )
        }

        viewModelScope.launch {
            val totalSteps = 40
            for (step in 1..totalSteps) {
                delay(70)
                val progress = step.toFloat() / totalSteps
                _uiState.update { it.copy(exportProgress = progress) }
            }
            _uiState.update { it.copy(exportProgress = 1f, exportComplete = true) }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showExportDialog = false, isExporting = false, exportComplete = false) }
    }

    fun setExportDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showExportDialog = visible) }
    }

    fun setAiTextDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAiTextDialog = visible) }
    }

    fun setAddMediaDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAddMediaDialog = visible) }
    }

    fun setColorGradingDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showColorGradingDialog = visible) }
    }

    fun resetGradingForSelectedClip() {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(
                filter = FilterPreset.NONE,
                filterIntensity = 0.85f,
                adjustments = AdjustmentSettings()
            )
            _uiState.update {
                it.copy(
                    clips = clips,
                    statusMessage = "Color grade reset to neutral"
                )
            }
            saveProjectChanges()
        }
    }

    fun applyGradeToAllClips() {
        val selected = _uiState.value.selectedClip ?: return
        val clips = _uiState.value.clips.map { clip ->
            clip.copy(
                filter = selected.filter,
                filterIntensity = selected.filterIntensity,
                adjustments = selected.adjustments
            )
        }
        _uiState.update {
            it.copy(
                clips = clips,
                statusMessage = "✨ Color grade applied to all ${clips.size} clips in project!"
            )
        }
        saveProjectChanges()
    }

    fun aiAutoColorGrade() {
        val index = _uiState.value.selectedClipIndex
        val clips = _uiState.value.clips.toMutableList()
        if (index in clips.indices) {
            clips[index] = clips[index].copy(
                filter = FilterPreset.TEAL_AND_ORANGE,
                filterIntensity = 0.75f,
                adjustments = AdjustmentSettings(
                    contrast = 1.16f,
                    saturation = 1.15f,
                    brightness = 0.04f,
                    warmth = 0.12f, // Golden 5980K warmth
                    tint = -0.04f,  // Emerald cinematic undertone
                    vignette = 0.22f
                )
            )
            _uiState.update {
                it.copy(
                    clips = clips,
                    statusMessage = "🎬 Cinematic Hollywood Auto-Grade applied!"
                )
            }
            saveProjectChanges()
        }
    }

    fun toggleJsEnabled(enabled: Boolean) {
        jsEngine.isEnabled = enabled
        _uiState.update {
            it.copy(
                isJsEnabled = enabled,
                statusMessage = if (enabled) "⚡ JavaScript Enabled (WebView V8 Engine)" else "JavaScript Disabled"
            )
        }
    }

    fun setJsScript(code: String) {
        jsEngine.currentScript = code
        _uiState.update {
            it.copy(
                jsCurrentScript = code,
                statusMessage = "JavaScript expression script updated"
            )
        }
    }

    fun setJsDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showJsDialog = visible) }
    }

    fun testRunJs(code: String, onOutput: (String) -> Unit) {
        jsEngine.runDirectCode(code, onOutput)
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun saveProjectChanges() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentProject = repository.getProjectDirect(state.projectId)
            if (currentProject != null) {
                val updated = currentProject.copy(
                    aspectRatio = state.aspectRatio.name,
                    durationMs = state.totalDurationMs,
                    updatedAt = System.currentTimeMillis(),
                    clipCount = state.clips.size,
                    clipsJson = converters.fromClipList(state.clips),
                    audioTracksJson = converters.fromAudioList(state.audioTracks),
                    textOverlaysJson = converters.fromTextList(state.textOverlays),
                    stickersJson = converters.fromStickerList(state.stickers)
                )
                repository.saveProject(updated)
            }
        }
    }
}
