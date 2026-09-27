package com.example.ui.ai

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AspectRatioType
import com.example.data.repository.SampleVideoLibrary
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHover
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AiTextToVideoDialog(
    isGenerating: Boolean,
    progressText: String,
    progressFraction: Float,
    onDismiss: () -> Unit,
    onGenerate: (prompt: String, style: String, aspect: AspectRatioType, motion: String) -> Unit
) {
    var prompt by remember {
        mutableStateOf("Futuristic supercar racing through neon Tokyo with glowing violet reflections and rain")
    }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Neon 4K") }
    var selectedMotion by remember { mutableStateOf("Low-Angle Speed Track & Flyby") }
    var selectedAspect by remember { mutableStateOf(AspectRatioType.RATIO_9_16) }

    val styles = listOf(
        "Cyberpunk Neon 4K",
        "Cinematic Movie Master",
        "Hyper-Action 60FPS",
        "Vintage 70s Film",
        "Anime Ghibli Dream",
        "Sci-Fi Hollywood VFX"
    )

    val motions = listOf(
        "Low-Angle Speed Track & Flyby",
        "Smooth Forward Drone Orbit",
        "Dramatic Slow-Mo Zoom In",
        "Whip Pan & Chase Cam",
        "Floating Steadycam Glide"
    )

    Dialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, Brush.horizontalGradient(listOf(StudioPrimary, PlayheadColor)), RoundedCornerShape(20.dp)),
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
                                .background(Brush.linearGradient(listOf(StudioPrimary, PlayheadColor)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "AI Text to Video Studio",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Turn ideas into cinematic video in seconds",
                                color = PlayheadColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!isGenerating) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isGenerating) {
                    // Generating State View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioSurfaceVariant, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PlayheadColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = progressText.ifBlank { "Synthesizing AI Video..." },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StudioPrimary,
                            trackColor = StudioSurfaceHover
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(progressFraction * 100).toInt()}% • Multi-scene Storyboard & Timeline assembly",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    // Prompt Input
                    Text(
                        text = "1. What video do you want to create?",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("ai_prompt_input"),
                        placeholder = {
                            Text("Describe your dream video (e.g. Cyberpunk sports car drifting in rain, Sunset drone over ocean...)", fontSize = 13.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlayheadColor,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Preset Prompts
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Quick Inspiration:", color = TextSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SampleVideoLibrary.aiTextToVideoTemplates.forEach { template ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                                modifier = Modifier.clickable {
                                    prompt = template.prompt
                                    selectedStyle = template.style
                                    selectedMotion = template.cameraMovement
                                    selectedAspect = template.aspectRatio
                                }
                            ) {
                                Text(
                                    text = "✨ ${template.title}",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Visual Style Selector
                    Text(
                        text = "2. Visual Style",
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
                        styles.forEach { style ->
                            val isSel = style == selectedStyle
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier.clickable { selectedStyle = style }
                            ) {
                                Text(
                                    text = style,
                                    color = Color.White,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Camera Motion Selector
                    Text(
                        text = "3. Camera Movement",
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
                        motions.forEach { motion ->
                            val isSel = motion == selectedMotion
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier.clickable { selectedMotion = motion }
                            ) {
                                Text(
                                    text = motion,
                                    color = Color.White,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Aspect Ratio Selector
                    Text(
                        text = "4. Aspect Ratio",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AspectRatioType.RATIO_9_16,
                            AspectRatioType.RATIO_16_9,
                            AspectRatioType.RATIO_1_1,
                            AspectRatioType.RATIO_21_9
                        ).forEach { ratio ->
                            val isSel = ratio == selectedAspect
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) StudioPrimary else StudioSurfaceHover,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAspect = ratio }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = ratio.label,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (ratio == AspectRatioType.RATIO_9_16) "Reels" else if (ratio == AspectRatioType.RATIO_16_9) "YouTube" else if (ratio == AspectRatioType.RATIO_1_1) "Square" else "Cinema",
                                        color = TextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            if (prompt.isNotBlank()) {
                                onGenerate(prompt, selectedStyle, selectedAspect, selectedMotion)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("ai_generate_video_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate AI Video (3 Scenes + Audio)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
