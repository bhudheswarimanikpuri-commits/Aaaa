package com.example.data.model

enum class AspectRatioType(val label: String, val ratioFloat: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f / 16f, "Reels / Shorts / TikTok"),
    RATIO_16_9("16:9", 16f / 9f, "YouTube / Cinema"),
    RATIO_1_1("1:1", 1f, "Instagram Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Portrait Post"),
    RATIO_21_9("21:9", 21f / 9f, "Cinemascope Ultra-Wide")
}

enum class FilterPreset(val displayName: String, val description: String) {
    NONE("Original", "Natural raw look"),
    TEAL_AND_ORANGE("Teal & Orange", "Hollywood blockbuster grade"),
    CYBERPUNK("Cyberpunk", "Electric neon magenta & cyan"),
    RETRO_FILM("Vintage 90s", "Warm nostalgic 35mm grain"),
    NOIR("Cinematic Noir", "High contrast black & white"),
    GOLDEN_HOUR("Golden Hour", "Dreamy sunset glow"),
    VIVID_POP("Vivid Pop", "Punchy saturated colors"),
    MATRIX("Matrix Green", "Cyber digital tint")
}

enum class TransitionType(val displayName: String, val icon: String) {
    NONE("None", "🚫"),
    CROSS_DISSOLVE("Dissolve", "🌫️"),
    FADE_BLACK("Fade Black", "⬛"),
    ZOOM_IN("Zoom In", "🔍"),
    GLITCH_CUT("Glitch", "⚡"),
    FLASH_WHITE("Flash White", "💡"),
    WHIP_PAN("Whip Pan", "💨")
}

enum class AudioCategory(val label: String) {
    CINEMATIC("Cinematic"),
    BEATS("Trap & Beats"),
    LO_FI("Lo-Fi Chill"),
    VLOG("Vlog Upbeat"),
    SFX("Sound FX")
}

data class AdjustmentSettings(
    val brightness: Float = 0f, // -0.5f to 0.5f (-100 to +100)
    val contrast: Float = 1f,   // 0.5f to 1.8f (50% to 180%)
    val saturation: Float = 1f, // 0f to 2f (0% to 200%)
    val warmth: Float = 0f,     // -0.5f to 0.5f (Color Temperature: Cool 3200K to Warm 7500K)
    val tint: Float = 0f,       // -0.5f to 0.5f (Tint: Green to Magenta)
    val highlights: Float = 0f, // -0.5f to 0.5f
    val shadows: Float = 0f,    // -0.5f to 0.5f
    val vignette: Float = 0f    // 0f to 1f
) {
    val brightnessInt: Int get() = (brightness * 200).toInt()
    val contrastInt: Int get() = (contrast * 100).toInt()
    val saturationInt: Int get() = (saturation * 100).toInt()
    val temperatureInt: Int get() = (warmth * 200).toInt()
    val tintInt: Int get() = (tint * 200).toInt()
    val vignetteInt: Int get() = (vignette * 100).toInt()

    val temperatureKelvin: String get() {
        val kelvin = 5500 + (warmth * 4000).toInt()
        return "${kelvin}K"
    }
}

data class ClipData(
    val id: String,
    val title: String,
    val drawableResName: String? = null,
    val contentUri: String? = null,
    val durationMs: Long = 4000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 4000L,
    val speed: Float = 1.0f,
    val filter: FilterPreset = FilterPreset.NONE,
    val filterIntensity: Float = 0.8f,
    val adjustments: AdjustmentSettings = AdjustmentSettings(),
    val volume: Float = 1.0f,
    val transition: TransitionType = TransitionType.NONE,
    val isMuted: Boolean = false
) {
    val effectiveDurationMs: Long
        get() {
            val raw = (trimEndMs - trimStartMs).coerceAtLeast(500L)
            return (raw / speed).toLong()
        }
}

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: AudioCategory,
    val startMs: Long = 0L,
    val durationMs: Long = 12000L,
    val volume: Float = 0.85f,
    val isMuted: Boolean = false
)

data class TextOverlay(
    val id: String,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val posX: Float = 0.5f, // 0..1 normalized
    val posY: Float = 0.75f,
    val fontSizeSp: Float = 22f,
    val colorHex: String = "#FFFFFF",
    val isCaption: Boolean = false,
    val animation: String = "POP" // POP, FADE, TYPEWRITER
)

data class StickerOverlay(
    val id: String,
    val emoji: String,
    val label: String,
    val startMs: Long,
    val endMs: Long,
    val posX: Float = 0.5f,
    val posY: Float = 0.3f,
    val scale: Float = 1.0f
)

data class BeatMarker(
    val timeMs: Long,
    val intensity: Float = 1.0f
)

data class ExportConfig(
    val resolution: String = "1080p Full HD",
    val fps: Int = 30,
    val bitrateMbps: Int = 18,
    val format: String = "MP4 (H.264)"
)
