package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.CineProDatabase
import com.example.data.local.ProjectEntity
import com.example.data.model.AspectRatioType
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SampleVideoLibrary
import com.example.ui.ai.AiTextToVideoDialog
import com.example.ui.editor.EditorViewModel
import com.example.ui.theme.PlayheadColor
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceHover
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    editorViewModel: EditorViewModel,
    onOpenProject: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { CineProDatabase.getInstance(context) }
    val repository = remember { ProjectRepository(db.projectDao()) }

    val projects by repository.allProjects.collectAsStateWithLifecycle(initialValue = emptyList())
    val editorUiState by editorViewModel.uiState.collectAsStateWithLifecycle()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAiTextToVideoModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.ensureStarterProjects()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header Brand Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, PlayheadColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cinepro_app_icon_1790386382940),
                            contentDescription = "CinePro Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CinePro",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioGold.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioGold),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "WORLD'S BEST",
                                    color = StudioGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Next-Gen AI & 4K Studio Editor",
                            color = PlayheadColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Hero Card: AI Text to Video (Prominent feature requested by user!)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = StudioSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(StudioPrimary, PlayheadColor))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAiTextToVideoModal = true }
                    .testTag("hero_ai_text_to_video_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(StudioGold.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, StudioGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = StudioGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = " AI Text-to-Video Creator",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioPrimary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "CREATE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Type any story or scene idea — our AI instantly builds 3 cinematic scenes, dynamic camera motion, auto-captions, and soundtrack!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick prompt chip sample
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioSurfaceHover,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✨ \"Cyberpunk sports car drift in rain Tokyo...\"",
                                color = PlayheadColor,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = PlayheadColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: New Project & Templates
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // New Empty Project Button
                Button(
                    onClick = { showNewProjectDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("create_new_project_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Project", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // AI Inspiration Presets
                Button(
                    onClick = { showAiTextToVideoModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("ai_storyboard_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceHover)
                ) {
                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = PlayheadColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Storyboard", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Section: AI Storyboard Templates Carousel
        item {
            Column {
                Text(
                    text = "AI Video Presets & Styles",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any preset to generate instant cinematic clips",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SampleVideoLibrary.aiTextToVideoTemplates) { template ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StudioSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier
                                .width(200.dp)
                                .clickable {
                                    scope.launch {
                                        editorViewModel.generateTextToVideo(
                                            template.prompt,
                                            template.style,
                                            template.aspectRatio,
                                            template.cameraMovement
                                        )
                                        showAiTextToVideoModal = true
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StudioPrimary.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = template.aspectRatio.label,
                                            color = PlayheadColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = template.style,
                                    color = StudioGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = template.prompt,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Recent Projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects (${projects.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (projects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(StudioSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No projects yet. Tap 'New Project' or 'AI Text-to-Video' to create one!", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            items(projects) { project ->
                ProjectCardItem(
                    project = project,
                    onOpen = { onOpenProject(project.id) },
                    onDelete = {
                        scope.launch {
                            repository.deleteProject(project.id)
                        }
                    },
                    onDuplicate = {
                        scope.launch {
                            repository.duplicateProject(project.id)
                        }
                    }
                )
            }
        }
    }

    // New Project Custom Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { title, aspect ->
                showNewProjectDialog = false
                scope.launch {
                    val newId = repository.createNewProject(title, aspect)
                    onOpenProject(newId)
                }
            }
        )
    }

    // AI Text to Video Modal
    if (showAiTextToVideoModal) {
        AiTextToVideoDialog(
            isGenerating = editorUiState.isAiGenerating,
            progressText = editorUiState.aiProgressText,
            progressFraction = editorUiState.aiProgressFraction,
            onDismiss = { showAiTextToVideoModal = false },
            onGenerate = { prompt, style, aspect, motion ->
                editorViewModel.generateTextToVideo(prompt, style, aspect, motion)
            }
        )

        // When generation succeeds, open editor
        LaunchedEffect(editorUiState.projectId) {
            if (editorUiState.projectId.isNotBlank() && !editorUiState.isAiGenerating) {
                showAiTextToVideoModal = false
                onOpenProject(editorUiState.projectId)
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_item_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val drawableResId = when (project.thumbnailResName) {
                "scene_cyberpunk_1790386424324" -> R.drawable.scene_cyberpunk_1790386424324
                "scene_sunset_coast_1790386436886" -> R.drawable.scene_sunset_coast_1790386436886
                "scene_action_motion_1790386447549" -> R.drawable.scene_action_motion_1790386447549
                "scene_neon_portrait_1790386479877" -> R.drawable.scene_neon_portrait_1790386479877
                else -> R.drawable.scene_cyberpunk_1790386424324
            }

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = drawableResId),
                    contentDescription = project.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Play indicator overlay
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StudioSurfaceHover
                    ) {
                        Text(
                            text = project.aspectRatio.replace("RATIO_", "").replace("_", ":"),
                            color = PlayheadColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${project.clipCount} clips • ${project.durationMs / 1000f}s • ${project.resolution}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Quick Actions
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, aspect: AspectRatioType) -> Unit
) {
    var title by remember { mutableStateOf("My Video Project") }
    var selectedAspect by remember { mutableStateOf(AspectRatioType.RATIO_9_16) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Start New Project", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(14.dp))

                Text("Project Name", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlayheadColor,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Canvas Format", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AspectRatioType.RATIO_9_16,
                        AspectRatioType.RATIO_16_9,
                        AspectRatioType.RATIO_1_1
                    ).forEach { ratio ->
                        val isSel = ratio == selectedAspect
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) StudioPrimary else StudioSurfaceHover,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PlayheadColor else StudioBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAspect = ratio }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(ratio.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    if (ratio == AspectRatioType.RATIO_9_16) "Reels" else if (ratio == AspectRatioType.RATIO_16_9) "YouTube" else "Square",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceHover)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = { onCreate(title, selectedAspect) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
                    ) {
                        Text("Create Studio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
