package com.example.data.repository

import com.example.R
import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrack
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.StickerOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType

data class SampleClipMeta(
    val title: String,
    val description: String,
    val drawableRes: Int,
    val category: String,
    val durationMs: Long
)

data class AiTextToVideoTemplate(
    val title: String,
    val prompt: String,
    val style: String,
    val cameraMovement: String,
    val aspectRatio: AspectRatioType,
    val musicMood: String,
    val sceneDescriptions: List<String>,
    val voiceoverNarration: String
)

object SampleVideoLibrary {
    val sampleClips = listOf(
        SampleClipMeta(
            title = "Cyberpunk Neo Tokyo",
            description = "Futuristic city street with neon reflections & light trails",
            drawableRes = R.drawable.scene_cyberpunk_1790386424324,
            category = "Cyberpunk",
            durationMs = 4500L
        ),
        SampleClipMeta(
            title = "Golden Coast Drone",
            description = "Cinematic aerial glide over turquoise cliffs & golden hour waves",
            drawableRes = R.drawable.scene_sunset_coast_1790386436886,
            category = "Nature",
            durationMs = 5000L
        ),
        SampleClipMeta(
            title = "High Velocity Parkour",
            description = "Dynamic roof runner leaping with dramatic lens flare",
            drawableRes = R.drawable.scene_action_motion_1790386447549,
            category = "Action",
            durationMs = 4000L
        ),
        SampleClipMeta(
            title = "Neon DJ Vertical Reel",
            description = "Electric portrait with glowing headphones for Reels & Shorts",
            drawableRes = R.drawable.scene_neon_portrait_1790386479877,
            category = "Reel",
            durationMs = 4000L
        )
    )

    val sampleSoundtracks = listOf(
        AudioTrack(
            id = "audio_synth_1",
            title = "Neon Horizon 2099",
            artist = "CinePro Synth Lab",
            category = AudioCategory.CINEMATIC,
            durationMs = 15000L,
            volume = 0.85f
        ),
        AudioTrack(
            id = "audio_beats_1",
            title = "Midnight Tokyo Drift",
            artist = "808 Trap District",
            category = AudioCategory.BEATS,
            durationMs = 14000L,
            volume = 0.9f
        ),
        AudioTrack(
            id = "audio_lofi_1",
            title = "Coffee & Rain Drops",
            artist = "ChillHop Collective",
            category = AudioCategory.LO_FI,
            durationMs = 16000L,
            volume = 0.75f
        ),
        AudioTrack(
            id = "audio_vlog_1",
            title = "Golden Hour Memories",
            artist = "Acoustic Sunset",
            category = AudioCategory.VLOG,
            durationMs = 12000L,
            volume = 0.8f
        )
    )

    val aiTextToVideoTemplates = listOf(
        AiTextToVideoTemplate(
            title = "Cyberpunk Tokyo 2077",
            prompt = "Cyberpunk sports car speeding through rain-slicked Tokyo streets with neon violet reflections and holograms",
            style = "Cyberpunk Neon 4K",
            cameraMovement = "Low-Angle Speed Track & Drone Flyby",
            aspectRatio = AspectRatioType.RATIO_9_16,
            musicMood = "Cyber Synthwave",
            sceneDescriptions = listOf(
                "Establishing shot of towering neon skyscrapers shrouded in violet mist",
                "Low angle tracking shot of headlights reflecting on wet asphalt",
                "High speed pass through cyber alleyway with holographic ads"
            ),
            voiceoverNarration = "In the neon heart of the city, velocity is the only language that matters."
        ),
        AiTextToVideoTemplate(
            title = "Pacific Coastline Drone",
            prompt = "Cinematic 8K drone glide over golden hour sunset ocean waves crashing into dramatic cliffside",
            style = "Cinematic Movie Master",
            cameraMovement = "Smooth Forward Orbit & Pull-Out",
            aspectRatio = AspectRatioType.RATIO_16_9,
            musicMood = "Epic Orchestral",
            sceneDescriptions = listOf(
                "Sun breaking through clouds over deep blue ocean swells",
                "Aerial pass skimming turquoise crests crashing on rugged stones",
                "Slow motion panoramic sunset painting the coastal horizon in gold"
            ),
            voiceoverNarration = "Where the wild ocean meets the eternal cliffs, pure tranquility begins."
        ),
        AiTextToVideoTemplate(
            title = "Urban Gravity Parkour",
            prompt = "Hyper-action athlete leaping across skyscraper rooftops against sunset sky with dramatic flares",
            style = "Hyper-Action 60FPS",
            cameraMovement = "Whip Pan & Dynamic Chase Cam",
            aspectRatio = AspectRatioType.RATIO_9_16,
            musicMood = "High Energy Trap Beats",
            sceneDescriptions = listOf(
                "Sprinting start along edge of modern glass skyscraper",
                "Mid-air slow motion leap with glowing sunset backlight",
                "Smooth roll landing and dash towards the urban skyline"
            ),
            voiceoverNarration = "Break every boundary. Defy every limit. The city is our playground."
        ),
        AiTextToVideoTemplate(
            title = "Cosmic Odyssey",
            prompt = "Futuristic starship accelerating through a sparkling purple and cyan nebula into deep space",
            style = "Sci-Fi Hollywood VFX",
            cameraMovement = "Deep Space Flyby & Warp Speed Burst",
            aspectRatio = AspectRatioType.RATIO_21_9,
            musicMood = "Deep Space Ambient",
            sceneDescriptions = listOf(
                "Distant view of a swirling nebula glowing in violet and sapphire",
                "Sleek exploration cruiser warming ion thrusters",
                "Sub-light jump leaving trails of cosmic stardust across the cosmos"
            ),
            voiceoverNarration = "Beyond the edge of known stars, a new frontier awaits humanity."
        )
    )

    fun createInitialStarterClips(): List<ClipData> {
        return listOf(
            ClipData(
                id = "starter_clip_1",
                title = "Cyberpunk Neo Tokyo",
                drawableResName = "scene_cyberpunk_1790386424324",
                durationMs = 4500L,
                trimStartMs = 0L,
                trimEndMs = 4500L,
                filter = FilterPreset.CYBERPUNK,
                filterIntensity = 0.85f,
                adjustments = AdjustmentSettings(contrast = 1.15f, saturation = 1.25f, vignette = 0.25f),
                transition = TransitionType.GLITCH_CUT
            ),
            ClipData(
                id = "starter_clip_2",
                title = "Golden Coast Drone",
                drawableResName = "scene_sunset_coast_1790386436886",
                durationMs = 4500L,
                trimStartMs = 0L,
                trimEndMs = 4500L,
                filter = FilterPreset.GOLDEN_HOUR,
                filterIntensity = 0.9f,
                adjustments = AdjustmentSettings(warmth = 0.25f, contrast = 1.1f),
                transition = TransitionType.CROSS_DISSOLVE
            ),
            ClipData(
                id = "starter_clip_3",
                title = "High Velocity Action",
                drawableResName = "scene_action_motion_1790386447549",
                durationMs = 4000L,
                trimStartMs = 0L,
                trimEndMs = 4000L,
                filter = FilterPreset.TEAL_AND_ORANGE,
                filterIntensity = 0.8f,
                adjustments = AdjustmentSettings(contrast = 1.2f, saturation = 1.15f),
                transition = TransitionType.ZOOM_IN
            )
        )
    }

    fun createInitialCaptions(): List<TextOverlay> {
        return listOf(
            TextOverlay(
                id = "cap_1",
                text = "⚡ WORLD'S BEST VIDEO EDITOR",
                startMs = 500L,
                endMs = 3800L,
                posY = 0.72f,
                fontSizeSp = 22f,
                colorHex = "#00E5FF",
                isCaption = true,
                animation = "POP"
            ),
            TextOverlay(
                id = "cap_2",
                text = "✨ AI Text-to-Video & 4K Timeline",
                startMs = 4600L,
                endMs = 8500L,
                posY = 0.72f,
                fontSizeSp = 20f,
                colorHex = "#FFD600",
                isCaption = true,
                animation = "GLOW"
            ),
            TextOverlay(
                id = "cap_3",
                text = "🚀 Create Cinematic Magic",
                startMs = 9200L,
                endMs = 12500L,
                posY = 0.72f,
                fontSizeSp = 22f,
                colorHex = "#FF3D71",
                isCaption = true,
                animation = "POP"
            )
        )
    }

    fun createInitialStickers(): List<StickerOverlay> {
        return listOf(
            StickerOverlay(
                id = "stk_1",
                emoji = "🔥",
                label = "FIRE",
                startMs = 1000L,
                endMs = 4000L,
                posX = 0.82f,
                posY = 0.22f,
                scale = 1.2f
            ),
            StickerOverlay(
                id = "stk_2",
                emoji = "🎬",
                label = "PRO 4K",
                startMs = 4800L,
                endMs = 8000L,
                posX = 0.18f,
                posY = 0.22f,
                scale = 1.1f
            )
        )
    }
}
