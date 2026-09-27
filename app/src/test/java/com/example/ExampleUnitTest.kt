package com.example

import com.example.data.model.AdjustmentSettings
import com.example.data.model.AspectRatioType
import com.example.data.model.ClipData
import com.example.data.model.FilterPreset
import com.example.data.model.TransitionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testClipEffectiveDurationWithSpeed() {
        val clip = ClipData(
            id = "clip_1",
            title = "Tokyo Drift",
            durationMs = 4000L,
            trimStartMs = 0L,
            trimEndMs = 4000L,
            speed = 2.0f
        )
        // 4000ms at 2x speed should be 2000ms effective duration
        assertEquals(2000L, clip.effectiveDurationMs)
    }

    @Test
    fun testClipEffectiveDurationWithSlowMotion() {
        val clip = ClipData(
            id = "clip_2",
            title = "Slow Mo Wave",
            durationMs = 5000L,
            trimStartMs = 1000L,
            trimEndMs = 4000L, // 3000ms raw
            speed = 0.5f
        )
        // 3000ms at 0.5x speed should be 6000ms effective duration
        assertEquals(6000L, clip.effectiveDurationMs)
    }

    @Test
    fun testAspectRatioValues() {
        assertEquals(9f / 16f, AspectRatioType.RATIO_9_16.ratioFloat, 0.001f)
        assertEquals(16f / 9f, AspectRatioType.RATIO_16_9.ratioFloat, 0.001f)
        assertEquals(1.0f, AspectRatioType.RATIO_1_1.ratioFloat, 0.001f)
    }

    @Test
    fun testFilterPresetsExist() {
        assertTrue(FilterPreset.values().any { it == FilterPreset.TEAL_AND_ORANGE })
        assertTrue(FilterPreset.values().any { it == FilterPreset.CYBERPUNK })
        assertTrue(TransitionType.values().any { it == TransitionType.GLITCH_CUT })
    }

    @Test
    fun testColorGradingAdjustments() {
        val adjustments = AdjustmentSettings(
            contrast = 1.25f,
            saturation = 1.10f,
            brightness = 0.05f,
            warmth = 0.15f // warm golden 6100K
        )
        assertEquals(125, adjustments.contrastInt)
        assertEquals(110, adjustments.saturationInt)
        assertEquals(10, adjustments.brightnessInt)
        assertEquals(30, adjustments.temperatureInt)
        assertEquals("6100K", adjustments.temperatureKelvin)
    }

    @Test
    fun testJavaScriptEnginePresets() {
        val presets = com.example.data.js.VideoJavaScriptEngine.PRESETS
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.id == "js_wiggle" })
        assertTrue(presets.any { it.id == "js_pulse" })
        assertTrue(presets.any { it.id == "js_glitch" })

        val transform = com.example.data.js.JsFrameTransform(
            scaleMultiplier = 1.05f,
            rotationDeg = 2.0f,
            glitchIntensity = 0.5f
        )
        assertEquals(1.05f, transform.scaleMultiplier, 0.001f)
        assertEquals(2.0f, transform.rotationDeg, 0.001f)
    }
}
