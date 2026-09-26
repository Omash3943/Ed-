package com.vineyard.aivideostudio.ui.screens.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vineyard.aivideostudio.core.util.FileUtils
import com.vineyard.aivideostudio.core.util.TimeUtils
import com.vineyard.aivideostudio.ui.components.AppTopBar
import com.vineyard.aivideostudio.ui.components.VideoPreviewPlayer
import com.vineyard.aivideostudio.ui.theme.AmberAccent
import com.vineyard.aivideostudio.ui.theme.BorderSubtle
import com.vineyard.aivideostudio.ui.theme.EmeraldSuccess
import com.vineyard.aivideostudio.ui.theme.RoseError
import com.vineyard.aivideostudio.ui.theme.StudioCardBg
import com.vineyard.aivideostudio.ui.theme.StudioDarkBg
import com.vineyard.aivideostudio.ui.theme.StudioSurface
import com.vineyard.aivideostudio.ui.theme.StudioSurfaceElevated
import com.vineyard.aivideostudio.ui.theme.TextPrimary
import com.vineyard.aivideostudio.ui.theme.TextSecondary
import com.vineyard.aivideostudio.ui.theme.TextTertiary
import com.vineyard.aivideostudio.ui.theme.VioletAccent
import com.vineyard.aivideostudio.ui.theme.VioletPrimary

@Composable
fun CreateScreen(
    viewModel: CreateViewModel,
    onProjectCreated: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onVideoSelected(uri)
        }
    }

    LaunchedEffect(state.createdProjectId) {
        state.createdProjectId?.let { id ->
            viewModel.resetCreatedState()
            onProjectCreated(id)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "New Project", subtitle = "Select source video for AI production")
        },
        containerColor = StudioDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project Name Input
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.projectName,
                    onValueChange = { viewModel.onProjectNameChanged(it) },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g., Epic Highlight Reel") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_project_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = VioletAccent,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // Source Mode Selection (Local Video vs YouTube Analysis)
            item {
                TabRow(
                    selectedTabIndex = if (state.isYoutubeMode) 1 else 0,
                    containerColor = StudioSurfaceElevated,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (state.isYoutubeMode) 1 else 0]),
                            color = VioletPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = !state.isYoutubeMode,
                        onClick = { viewModel.setSourceMode(false) },
                        text = { Text("Local Video (Editable)", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.isYoutubeMode,
                        onClick = { viewModel.setSourceMode(true) },
                        text = { Text("YouTube URL (Analysis)", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Source Input Content
            item {
                if (!state.isYoutubeMode) {
                    // Local Video Picker
                    if (state.selectedVideoUri != null && state.videoMetadata != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                VideoPreviewPlayer(
                                    videoUriString = state.selectedVideoUri.toString(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${state.videoMetadata?.width}x${state.videoMetadata?.height}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = TimeUtils.formatDuration(state.videoMetadata?.durationSeconds ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VioletAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = FileUtils.formatBytes(state.videoMetadata?.fileSize ?: 0L),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { filePickerLauncher.launch("video/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioCardBg),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Change Video", color = TextPrimary)
                                }
                            }
                        }
                    } else {
                        // Empty picker card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudioSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { filePickerLauncher.launch("video/*") }
                                .padding(16.dp)
                                .testTag("create_select_video_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Upload,
                                    contentDescription = null,
                                    tint = VioletAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to choose video from device",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Supports MP4, MOV, MKV via Storage Access Framework",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    // YouTube URL Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioSurfaceElevated)
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = state.youtubeUrl,
                            onValueChange = { viewModel.onYoutubeUrlChanged(it) },
                            label = { Text("Public YouTube URL") },
                            placeholder = { Text("https://www.youtube.com/watch?v=...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YouTube URLs provide semantic AI analysis & reference. To perform physical cuts/renders with Media3, authorized local video bytes are required.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Aspect Ratio Selector
            item {
                Text(
                    text = "TARGET ASPECT RATIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val ratios = listOf("ORIGINAL", "9:16", "16:9", "1:1")
                    ratios.forEach { ratio ->
                        val selected = state.targetAspectRatio == ratio
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onAspectRatioChanged(ratio) },
                            label = { Text(ratio) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPrimary,
                                selectedLabelColor = TextPrimary,
                                containerColor = StudioCardBg,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = BorderSubtle,
                                selectedBorderColor = VioletAccent
                            )
                        )
                    }
                }
            }

            // Error display
            if (state.errorMessage != null) {
                item {
                    Text(
                        text = state.errorMessage!!,
                        color = RoseError,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Create button
            item {
                Button(
                    onClick = { viewModel.createProject() },
                    enabled = !state.isLoading && (state.selectedVideoUri != null || state.youtubeUrl.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletPrimary,
                        disabledContainerColor = StudioSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "CREATE & OPEN STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
