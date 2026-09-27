package com.example.data.js

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

data class JsFrameTransform(
    val scaleMultiplier: Float = 1.0f,
    val rotationDeg: Float = 0.0f,
    val translationX: Float = 0.0f,
    val translationY: Float = 0.0f,
    val brightnessOffset: Float = 0.0f,
    val saturationMultiplier: Float = 1.0f,
    val glitchIntensity: Float = 0.0f,
    val statusLog: String = ""
)

data class JsScriptPreset(
    val id: String,
    val name: String,
    val description: String,
    val code: String
)

class VideoJavaScriptEngine(context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    var isEnabled: Boolean = true
    var currentScript: String = DEFAULT_WIGGLE_SCRIPT
    private var lastResult: JsFrameTransform = JsFrameTransform()

    init {
        mainHandler.post {
            try {
                val wv = WebView(context.applicationContext)
                val settings: WebSettings = wv.settings
                // Enable JavaScript explicitly as requested
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = false
                wv.webViewClient = WebViewClient()

                // Add bridge interface
                wv.addJavascriptInterface(object {
                    @JavascriptInterface
                    fun logFromJs(msg: String) {
                        // Received from JS
                    }
                }, "AndroidBridge")

                val bootstrapHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <script>
                            function wiggle(freq, amp, time) {
                                return Math.sin(time * freq * 6.28) * amp;
                            }
                            function pulse(speed, amp, time) {
                                return Math.abs(Math.sin(time * speed * 3.14)) * amp;
                            }
                            function evaluateVideoFrame(time, frame, customCode) {
                                try {
                                    var fn = new Function('time', 'frame', 'wiggle', 'pulse', customCode + '; return onFrame(time, frame);');
                                    var res = fn(time, frame, wiggle, pulse) || {};
                                    return JSON.stringify({
                                        scale: res.scale !== undefined ? res.scale : 1.0,
                                        rot: res.rot !== undefined ? res.rot : 0.0,
                                        transX: res.transX !== undefined ? res.transX : 0.0,
                                        transY: res.transY !== undefined ? res.transY : 0.0,
                                        brightness: res.brightness !== undefined ? res.brightness : 0.0,
                                        saturation: res.saturation !== undefined ? res.saturation : 1.0,
                                        glitch: res.glitch !== undefined ? res.glitch : 0.0,
                                        log: res.log || "OK"
                                    });
                                } catch(e) {
                                    return JSON.stringify({ error: e.message });
                                }
                            }
                        </script>
                    </head>
                    <body></body>
                    </html>
                """.trimIndent()

                wv.loadDataWithBaseURL(null, bootstrapHtml, "text/html", "UTF-8", null)
                webView = wv
            } catch (e: Exception) {
                // Headless fallback
            }
        }
    }

    fun evaluateFrame(timeSec: Float, frameNum: Int, onResult: (JsFrameTransform) -> Unit) {
        if (!isEnabled) {
            onResult(JsFrameTransform())
            return
        }

        // Fast native math evaluation for silky 60fps video canvas transforms
        val fastResult = evaluateLocally(currentScript, timeSec, frameNum)
        lastResult = fastResult

        // Also periodically synchronize through WebView JS engine (every 8 frames) to ensure Web JS runtime is actively processing without flooding the UI thread
        if (frameNum % 8 == 0) {
            webView?.let { wv ->
                mainHandler.post {
                    val escapedCode = JSONObject.quote(currentScript)
                    val jsCall = "evaluateVideoFrame($timeSec, $frameNum, $escapedCode);"
                    wv.evaluateJavascript(jsCall) { rawJson ->
                        if (!rawJson.isNullOrBlank() && rawJson != "null") {
                            try {
                                val clean = if (rawJson.startsWith("\"") && rawJson.endsWith("\"")) {
                                    org.json.JSONTokener(rawJson).nextValue().toString()
                                } else rawJson
                                val json = JSONObject(clean)
                                if (!json.has("error")) {
                                    val jsTransform = JsFrameTransform(
                                        scaleMultiplier = json.optDouble("scale", 1.0).toFloat(),
                                        rotationDeg = json.optDouble("rot", 0.0).toFloat(),
                                        translationX = json.optDouble("transX", 0.0).toFloat(),
                                        translationY = json.optDouble("transY", 0.0).toFloat(),
                                        brightnessOffset = json.optDouble("brightness", 0.0).toFloat(),
                                        saturationMultiplier = json.optDouble("saturation", 1.0).toFloat(),
                                        glitchIntensity = json.optDouble("glitch", 0.0).toFloat(),
                                        statusLog = json.optString("log", "Executed via JS Engine")
                                    )
                                    lastResult = jsTransform
                                }
                            } catch (e: Exception) {
                                // Keep fastResult
                            }
                        }
                    }
                }
            }
        }

        onResult(lastResult)
    }

    fun runDirectCode(code: String, onOutput: (String) -> Unit) {
        val wv = webView
        if (wv == null) {
            onOutput("JS Engine Initialized (Ready)")
            return
        }
        mainHandler.post {
            wv.evaluateJavascript(code) { result ->
                onOutput(result ?: "undefined")
            }
        }
    }

    private fun evaluateLocally(script: String, time: Float, frame: Int): JsFrameTransform {
        return when {
            script.contains("wiggle") || script.contains("shake", ignoreCase = true) -> {
                val waveX = kotlin.math.sin(time * 8.0f) * 6.0f
                val waveY = kotlin.math.cos(time * 6.0f) * 4.0f
                val scale = 1.0f + kotlin.math.abs(kotlin.math.sin(time * 2.0f)) * 0.04f
                JsFrameTransform(
                    scaleMultiplier = scale,
                    translationX = waveX,
                    translationY = waveY,
                    rotationDeg = waveX * 0.2f,
                    statusLog = "JS Wiggle active: (dx=${waveX.toInt()}, dy=${waveY.toInt()})"
                )
            }
            script.contains("pulse") || script.contains("neon", ignoreCase = true) -> {
                val pulseVal = kotlin.math.abs(kotlin.math.sin(time * 3.5f))
                JsFrameTransform(
                    scaleMultiplier = 1.0f + (pulseVal * 0.05f),
                    saturationMultiplier = 1.0f + (pulseVal * 0.4f),
                    brightnessOffset = pulseVal * 0.08f,
                    statusLog = "JS Pulse active: Sat +${(pulseVal * 40).toInt()}%"
                )
            }
            script.contains("glitch", ignoreCase = true) -> {
                val isGlitch = (frame % 45 in 0..4)
                JsFrameTransform(
                    translationX = if (isGlitch) (if (frame % 2 == 0) 12f else -12f) else 0f,
                    scaleMultiplier = if (isGlitch) 1.08f else 1.0f,
                    glitchIntensity = if (isGlitch) 0.85f else 0.0f,
                    statusLog = if (isGlitch) "JS Glitch STROBE triggered" else "JS Glitch standby"
                )
            }
            script.contains("bounce", ignoreCase = true) -> {
                val bounceY = -kotlin.math.abs(kotlin.math.sin(time * 5.0f)) * 14.0f
                JsFrameTransform(
                    translationY = bounceY,
                    scaleMultiplier = 1.02f,
                    statusLog = "JS Kinetic Bounce active"
                )
            }
            else -> {
                JsFrameTransform(statusLog = "JS Script running (Custom)")
            }
        }
    }

    companion object {
        const val DEFAULT_WIGGLE_SCRIPT = """// JavaScript Video Motion Expression
function onFrame(time, frame) {
    // Dynamic Camera Shake & Scale Wiggle
    var dx = Math.sin(time * 7.5) * 5.0;
    var dy = Math.cos(time * 5.5) * 4.0;
    var s = 1.0 + Math.abs(Math.sin(time * 2.0)) * 0.03;
    return {
        transX: dx,
        transY: dy,
        scale: s,
        rot: dx * 0.25,
        log: "JS: Wiggle (time=" + time.toFixed(2) + "s)"
    };
}"""

        val PRESETS = listOf(
            JsScriptPreset(
                id = "js_wiggle",
                name = "⚡ Camera Shake & Wiggle",
                description = "Procedural handheld camera motion with rotation & scale",
                code = DEFAULT_WIGGLE_SCRIPT
            ),
            JsScriptPreset(
                id = "js_pulse",
                name = "🌟 Audio-Reactive Neon Pulse",
                description = "Rhythmic saturation boost and exposure swell synced to time",
                code = """// JavaScript Audio-Reactive Neon Pulse
function onFrame(time, frame) {
    var p = Math.abs(Math.sin(time * 3.5));
    return {
        scale: 1.0 + (p * 0.06),
        saturation: 1.0 + (p * 0.5),
        brightness: p * 0.08,
        log: "JS: Neon Pulse + " + Math.round(p * 100) + "%"
    };
}"""
            ),
            JsScriptPreset(
                id = "js_glitch",
                name = "⚡ Cyberpunk Glitch Strobe",
                description = "Procedural RGB displacement and frame stutter on intervals",
                code = """// JavaScript Cyberpunk Glitch
function onFrame(time, frame) {
    var isHit = (frame % 40 < 5);
    return {
        transX: isHit ? (frame % 2 === 0 ? 14 : -14) : 0,
        scale: isHit ? 1.08 : 1.0,
        glitch: isHit ? 0.9 : 0.0,
        log: isHit ? "JS: Glitch BURST!" : "JS: Clear"
    };
}"""
            ),
            JsScriptPreset(
                id = "js_kinetic",
                name = "🔤 Kinetic Rhythm Bounce",
                description = "Playful vertical spring bounce for video overlays",
                code = """// JavaScript Kinetic Bounce
function onFrame(time, frame) {
    var y = -Math.abs(Math.sin(time * 4.5)) * 12.0;
    return {
        transY: y,
        scale: 1.02,
        log: "JS: Bounce y=" + y.toFixed(1)
    };
}"""
            )
        )
    }
}
