package com.example.ui.export

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHover
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExportDialog(
    durationMs: Long,
    isExporting: Boolean,
    exportProgress: Float,
    exportComplete: Boolean,
    onDismiss: () -> Unit,
    onStartExport: (resolution: String, fps: Int, quality: String) -> Unit
) {
    var selectedRes by remember { mutableStateOf("1080p Full HD") }
    var selectedFps by remember { mutableIntStateOf(60) }
    var selectedQuality by remember { mutableStateOf("High") }

    val resolutions = listOf("720p HD", "1080p Full HD", "4K Ultra HD")
    val fpsOptions = listOf(24, 30, 60)

    val estimatedMb = when (selectedRes) {
        "4K Ultra HD" -> (durationMs / 1000f * 6.5f).toInt()
        "1080p Full HD" -> (durationMs / 1000f * 2.8f).toInt()
        else -> (durationMs / 1000f * 1.2f).toInt()
    }.coerceAtLeast(4)

    Dialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, StudioBorder, RoundedCornerShape(20.dp)),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (exportComplete) "Export Complete! 🎉" else if (isExporting) "Rendering Video..." else "Export Settings",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isExporting) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (exportComplete) {
                    // Export Complete Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceVariant, RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(StudioGreen.copy(alpha = 0.2f), CircleShape)
                                .border(1.5.dp, StudioGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = StudioGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Rendered in $selectedRes ($selectedFps FPS)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Size: ~$estimatedMb MB • Saved to internal media gallery",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(" Done", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else if (isExporting) {
                    // Rendering Progress
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceVariant, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PlayheadColor,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Encoding $selectedRes Master Video",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StudioPrimary,
                            trackColor = StudioSurfaceHover
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(exportProgress * 100).toInt()}% • Applying LUTs, audio mixing & hardware encode",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    // Settings configuration
                    Text("Resolution", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resolutions.forEach { res ->
                            val isSel = res == selectedRes
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRes = res }
                            ) {
                                Text(
                                    text = res,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Frame Rate (FPS)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fpsOptions.forEach { fps ->
                            val isSel = fps == selectedFps
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFps = fps }
                            ) {
                                Text(
                                    text = "${fps} FPS",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceHover, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated File Size:", color = TextSecondary, fontSize = 12.sp)
                        Text("~$estimatedMb MB", color = PlayheadColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onStartExport(selectedRes, selectedFps, selectedQuality) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_export_render_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Render & Export Video", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
