package com.vineyard.aivideostudio.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vineyard.aivideostudio.ai.model.ModelPurpose
import com.vineyard.aivideostudio.core.util.TimeUtils
import com.vineyard.aivideostudio.ui.components.AppTopBar
import com.vineyard.aivideostudio.ui.components.ModelDropdown
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
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputKey by remember(state.apiKey) { mutableStateOf(state.apiKey) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(title = "Settings", subtitle = "Gemini Models & Studio Preferences")
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
            // Notifications / Status Banners
            if (state.statusMessage != null) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(state.statusMessage!!, color = EmeraldSuccess, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoseError.copy(alpha = 0.15f))
                            .border(1.dp, RoseError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(state.errorMessage!!, color = RoseError, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // GEMINI API SECTION
            item {
                Text(
                    text = "GEMINI API CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini API Key", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stored securely. Also injected via Secrets panel.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            placeholder = { Text("Paste API Key: AIzaSy...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_api_key_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VioletPrimary,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.onApiKeyChanged(inputKey) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_save_api_key_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Key", color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fetch Models Button
                        Button(
                            onClick = { viewModel.fetchModels() },
                            enabled = !state.isFetchingModels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_fetch_models_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isFetchingModels) {
                                CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Discovering Models...", color = TextPrimary)
                            } else {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, tint = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetch Available Models", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (state.lastSyncTime > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Last model sync: ${TimeUtils.formatTimestamp(state.lastSyncTime)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            // MODEL ASSIGNMENTS PER PURPOSE
            item {
                Text(
                    text = "AI MODEL ASSIGNMENTS PER PURPOSE",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ModelPurpose.values().forEach { purpose ->
                            val selectedId = state.modelConfigs[purpose.name] ?: when (purpose) {
                                ModelPurpose.VIDEO_ANALYSIS -> "gemini-3.5-flash"
                                ModelPurpose.AUDIO_TRANSCRIPTION -> "gemini-3.5-flash"
                                ModelPurpose.EDITING_DIRECTOR -> "gemini-3.5-flash"
                                ModelPurpose.COMMENTARY -> "gemini-3.5-flash"
                                ModelPurpose.TEXT_TO_SPEECH -> "gemini-2.5-flash-preview-tts"
                                ModelPurpose.LIVE_VOICE -> "gemini-2.5-flash-native-audio-preview-12-2025"
                            }

                            val eligibleModels = state.availableModels.filter {
                                it.supportedPurposes.contains(purpose) || it.capabilities.supportsText
                            }

                            ModelDropdown(
                                label = purpose.displayName,
                                selectedModelId = selectedId,
                                availableModels = eligibleModels,
                                onModelSelected = { model ->
                                    viewModel.setModelForPurpose(purpose, model)
                                }
                            )
                        }
                    }
                }
            }

            // PROCESSING CONFIGURATION
            item {
                Text(
                    text = "PRODUCTION PIPELINE PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Keep Intermediate Videos", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Preserve output from trim, crop, zoom stages", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = state.keepIntermediateVideos,
                                onCheckedChange = { viewModel.onKeepIntermediateVideosChanged(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = VioletPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automatic Final QA", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Verify source retention & media coherence before export", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = state.autoFinalQa,
                                onCheckedChange = { viewModel.onAutoFinalQaChanged(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = VioletPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Maximum QA Retries: ${state.maxQaRetries}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Slider(
                            value = state.maxQaRetries.toFloat(),
                            onValueChange = { viewModel.onMaxRetriesChanged(it.toInt()) },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = VioletAccent,
                                activeTrackColor = VioletPrimary,
                                inactiveTrackColor = BorderSubtle
                            )
                        )
                    }
                }
            }

            // VOICE REPLICATION & CONSENT
            item {
                Text(
                    text = "VOICE MANAGEMENT & REPLICATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = VioletAccent,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verified Voice Profiles", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Requires verified consent recording to prevent unauthorized cloning.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        state.voices.forEach { voice ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioCardBg)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(voice.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text(if (voice.consentVerified) "Consent Verified" else "Pending Consent", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Button(
                            onClick = {
                                viewModel.createVoiceProfile("Director Voice", "Authorized narrator profile")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register Authorized Voice Profile", color = TextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
