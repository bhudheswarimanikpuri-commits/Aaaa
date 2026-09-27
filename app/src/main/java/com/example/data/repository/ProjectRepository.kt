package com.example.data.repository

import com.example.data.local.CineProDao
import com.example.data.local.Converters
import com.example.data.local.ProjectEntity
import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectRepository(
    private val dao: CineProDao
) {
    private val converters = Converters()

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    fun getProject(id: String): Flow<ProjectEntity?> = dao.getProjectById(id)

    suspend fun getProjectDirect(id: String): ProjectEntity? = withContext(Dispatchers.IO) {
        dao.getProjectByIdDirect(id)
    }

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        dao.insertProject(project)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    suspend fun duplicateProject(id: String) = withContext(Dispatchers.IO) {
        val original = dao.getProjectByIdDirect(id) ?: return@withContext
        val duplicate = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copy)",
            updatedAt = System.currentTimeMillis()
        )
        dao.insertProject(duplicate)
    }

    suspend fun ensureStarterProjects() = withContext(Dispatchers.IO) {
        // Check if projects already exist
        val existing = dao.getProjectByIdDirect("starter_proj_1")
        if (existing == null) {
            val starterClips = SampleVideoLibrary.createInitialStarterClips()
            val captions = SampleVideoLibrary.createInitialCaptions()
            val stickers = SampleVideoLibrary.createInitialStickers()
            val soundtracks = listOf(SampleVideoLibrary.sampleSoundtracks[0])

            val proj1 = ProjectEntity(
                id = "starter_proj_1",
                title = "⚡ Cyberpunk Tokyo Reel [AI]",
                aspectRatio = AspectRatioType.RATIO_9_16.name,
                resolution = "1080p FHD",
                fps = 60,
                durationMs = 13000L,
                updatedAt = System.currentTimeMillis(),
                thumbnailResName = "scene_cyberpunk_1790386424324",
                thumbnailUri = null,
                clipCount = starterClips.size,
                clipsJson = converters.fromClipList(starterClips),
                audioTracksJson = converters.fromAudioList(soundtracks),
                textOverlaysJson = converters.fromTextList(captions),
                stickersJson = converters.fromStickerList(stickers)
            )
            dao.insertProject(proj1)

            val proj2Clips = listOf(
                ClipData(
                    id = "p2_c1",
                    title = "Pacific Waves",
                    drawableResName = "scene_sunset_coast_1790386436886",
                    durationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 5000L,
                    filter = FilterPreset.GOLDEN_HOUR,
                    filterIntensity = 0.95f,
                    transition = TransitionType.CROSS_DISSOLVE
                ),
                ClipData(
                    id = "p2_c2",
                    title = "Action Peak",
                    drawableResName = "scene_action_motion_1790386447549",
                    durationMs = 4000L,
                    trimStartMs = 0L,
                    trimEndMs = 4000L,
                    filter = FilterPreset.TEAL_AND_ORANGE,
                    filterIntensity = 0.85f,
                    transition = TransitionType.ZOOM_IN
                )
            )

            val proj2 = ProjectEntity(
                id = "starter_proj_2",
                title = "🌊 Golden Coast Cinema [4K]",
                aspectRatio = AspectRatioType.RATIO_16_9.name,
                resolution = "4K Ultra HD",
                fps = 30,
                durationMs = 9000L,
                updatedAt = System.currentTimeMillis() - 3600000L,
                thumbnailResName = "scene_sunset_coast_1790386436886",
                thumbnailUri = null,
                clipCount = proj2Clips.size,
                clipsJson = converters.fromClipList(proj2Clips),
                audioTracksJson = converters.fromAudioList(listOf(SampleVideoLibrary.sampleSoundtracks[3])),
                textOverlaysJson = converters.fromTextList(
                    listOf(
                        TextOverlay(
                            id = "p2_txt",
                            text = "PACIFIC EXPLORATION 4K",
                            startMs = 500L,
                            endMs = 4500L,
                            posY = 0.8f,
                            fontSizeSp = 24f,
                            colorHex = "#FFD600",
                            isCaption = false,
                            animation = "POP"
                        )
                    )
                ),
                stickersJson = converters.fromStickerList(emptyList())
            )
            dao.insertProject(proj2)
        }
    }

    suspend fun createNewProject(
        title: String,
        aspectRatio: AspectRatioType,
        clips: List<ClipData> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val newId = "proj_" + UUID.randomUUID().toString().take(8)
        val finalClips = if (clips.isNotEmpty()) clips else listOf(
            ClipData(
                id = "clip_" + UUID.randomUUID().toString().take(6),
                title = "Scene 1",
                drawableResName = "scene_cyberpunk_1790386424324",
                durationMs = 4000L,
                trimStartMs = 0L,
                trimEndMs = 4000L,
                filter = FilterPreset.NONE
            )
        )

        val totalDuration = finalClips.sumOf { it.effectiveDurationMs }
        val newProject = ProjectEntity(
            id = newId,
            title = title.ifBlank { "Untitled Project" },
            aspectRatio = aspectRatio.name,
            resolution = "1080p FHD",
            fps = 30,
            durationMs = totalDuration,
            updatedAt = System.currentTimeMillis(),
            thumbnailResName = finalClips.firstOrNull()?.drawableResName,
            thumbnailUri = finalClips.firstOrNull()?.contentUri,
            clipCount = finalClips.size,
            clipsJson = converters.fromClipList(finalClips),
            audioTracksJson = converters.fromAudioList(listOf(SampleVideoLibrary.sampleSoundtracks.first())),
            textOverlaysJson = converters.fromTextList(emptyList()),
            stickersJson = converters.fromStickerList(emptyList())
        )
        dao.insertProject(newProject)
        newId
    }

    suspend fun createFromAiTextToVideo(
        prompt: String,
        style: String,
        aspectRatio: AspectRatioType,
        cameraMotion: String,
        musicMood: String,
        generatedScenes: List<String>,
        narration: String
    ): String = withContext(Dispatchers.IO) {
        val newId = "ai_proj_" + UUID.randomUUID().toString().take(8)
        val sampleDrawables = listOf(
            "scene_cyberpunk_1790386424324",
            "scene_sunset_coast_1790386436886",
            "scene_action_motion_1790386447549",
            "scene_neon_portrait_1790386479877"
        )

        val clips = generatedScenes.mapIndexed { index, sceneTitle ->
            val assignedDrawable = sampleDrawables[index % sampleDrawables.size]
            val filter = when {
                style.contains("Cyber", ignoreCase = true) -> FilterPreset.CYBERPUNK
                style.contains("Retro", ignoreCase = true) || style.contains("70s", ignoreCase = true) -> FilterPreset.RETRO_FILM
                style.contains("Noir", ignoreCase = true) -> FilterPreset.NOIR
                style.contains("Nature", ignoreCase = true) || style.contains("Golden", ignoreCase = true) -> FilterPreset.GOLDEN_HOUR
                else -> FilterPreset.TEAL_AND_ORANGE
            }
            ClipData(
                id = "ai_clip_${index + 1}",
                title = "Scene ${index + 1}: ${sceneTitle.take(28)}",
                drawableResName = assignedDrawable,
                durationMs = 4500L,
                trimStartMs = 0L,
                trimEndMs = 4500L,
                speed = 1.0f,
                filter = filter,
                filterIntensity = 0.9f,
                adjustments = AdjustmentSettings(
                    contrast = 1.15f,
                    saturation = 1.2f,
                    vignette = 0.2f
                ),
                transition = if (index > 0) TransitionType.CROSS_DISSOLVE else TransitionType.NONE
            )
        }

        val captions = if (narration.isNotBlank()) {
            val words = narration.split(" ")
            val chunkSize = 6
            val chunks = words.chunked(chunkSize).map { it.joinToString(" ") }
            val chunkDuration = 4000L
            chunks.mapIndexed { idx, chunkText ->
                TextOverlay(
                    id = "ai_cap_$idx",
                    text = chunkText,
                    startMs = idx * chunkDuration + 300L,
                    endMs = (idx + 1) * chunkDuration - 200L,
                    posY = 0.76f,
                    fontSizeSp = 22f,
                    colorHex = "#00E5FF",
                    isCaption = true,
                    animation = "POP"
                )
            }
        } else emptyList()

        val audioTrack = when {
            musicMood.contains("Synth", ignoreCase = true) -> SampleVideoLibrary.sampleSoundtracks[0]
            musicMood.contains("Trap", ignoreCase = true) || musicMood.contains("Beat", ignoreCase = true) -> SampleVideoLibrary.sampleSoundtracks[1]
            musicMood.contains("Chill", ignoreCase = true) -> SampleVideoLibrary.sampleSoundtracks[2]
            else -> SampleVideoLibrary.sampleSoundtracks[3]
        }

        val totalDuration = clips.sumOf { it.effectiveDurationMs }
        val titlePrompt = prompt.take(30).ifBlank { "AI Video Project" }

        val project = ProjectEntity(
            id = newId,
            title = "✨ AI: $titlePrompt",
            aspectRatio = aspectRatio.name,
            resolution = "1080p FHD",
            fps = 60,
            durationMs = totalDuration,
            updatedAt = System.currentTimeMillis(),
            thumbnailResName = clips.firstOrNull()?.drawableResName,
            thumbnailUri = null,
            clipCount = clips.size,
            clipsJson = converters.fromClipList(clips),
            audioTracksJson = converters.fromAudioList(listOf(audioTrack)),
            textOverlaysJson = converters.fromTextList(captions),
            stickersJson = converters.fromStickerList(
                listOf(
                    com.example.data.model.StickerOverlay(
                        id = "ai_badge",
                        emoji = "✨",
                        label = "AI GENERATED",
                        startMs = 500L,
                        endMs = 4000L,
                        posX = 0.85f,
                        posY = 0.15f,
                        scale = 1.0f
                    )
                )
            )
        )

        dao.insertProject(project)
        newId
    }
}
