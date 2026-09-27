package com.example.ui.editor.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.js.VideoJavaScriptEngine
import com.example.ui.theme.PlayheadColor
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
fun JavaScriptEngineDialog(
    isJsEnabled: Boolean,
    currentScript: String,
    onToggleJs: (Boolean) -> Unit,
    onSaveScript: (String) -> Unit,
    onTestRun: (code: String, onOutput: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var editableCode by remember(currentScript) { mutableStateOf(currentScript) }
    var testConsoleOutput by remember { mutableStateOf("Ready. Tap 'Run JS Test' to evaluate via Android WebView V8 engine.") }
    var isExecuting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(StudioGold, PlayheadColor)),
                    RoundedCornerShape(20.dp)
                ),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Brush.linearGradient(listOf(StudioGold, PlayheadColor)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "JavaScript Motion Engine",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Expressions & Procedural Video FX",
                                color = StudioGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Master JavaScript Enable Switch Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isJsEnabled) StudioGold.copy(alpha = 0.6f) else StudioBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "JavaScript Engine",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isJsEnabled) StudioGold.copy(alpha = 0.2f) else StudioSurfaceHover,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isJsEnabled) StudioGold else StudioBorder
                                    ),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = if (isJsEnabled) "ENABLED (ACTIVE)" else "DISABLED",
                                        color = if (isJsEnabled) StudioGold else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isJsEnabled) "V8 Engine running via Android WebView (settings.javaScriptEnabled = true)"
                                else "Enable to execute procedural camera shakes, pulses & kinetic animations",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Switch(
                            checked = isJsEnabled,
                            onCheckedChange = { onToggleJs(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioGold,
                                checkedTrackColor = StudioPrimary,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = StudioSurfaceHover
                            ),
                            modifier = Modifier.testTag("javascript_enable_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Carousel
                Text(
                    text = "JavaScript Motion Presets:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoJavaScriptEngine.PRESETS.forEach { preset ->
                        val isSelected = editableCode.contains(preset.id) || editableCode.contains(preset.name)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) StudioPrimary else StudioSurfaceHover,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PlayheadColor else StudioBorder
                            ),
                            modifier = Modifier.clickable {
                                editableCode = preset.code
                                testConsoleOutput = "Loaded preset: ${preset.name}"
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = preset.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = preset.description.take(24) + "...",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Code Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "JavaScript Expression Code (ES6):",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "onFrame(time, frame)",
                        color = PlayheadColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = editableCode,
                    onValueChange = { editableCode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("js_code_editor_field"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF80D8FF)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlayheadColor,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = Color(0xFF090A10),
                        unfocusedContainerColor = Color(0xFF090A10)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Console Output
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF06070B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2232)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = StudioGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = testConsoleOutput,
                            color = StudioGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Test Run & Apply Script
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Test Run JS in WebView
                    OutlinedButton(
                        onClick = {
                            isExecuting = true
                            testConsoleOutput = "Evaluating in WebView V8 runtime..."
                            onTestRun(editableCode) { output ->
                                isExecuting = false
                                testConsoleOutput = "V8 Output: $output"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("run_js_test_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = StudioGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run JS Test", fontSize = 12.sp, color = TextPrimary)
                    }

                    // Apply to Timeline
                    Button(
                        onClick = {
                            if (!isJsEnabled) onToggleJs(true)
                            onSaveScript(editableCode)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("apply_js_script_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply to Video", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
