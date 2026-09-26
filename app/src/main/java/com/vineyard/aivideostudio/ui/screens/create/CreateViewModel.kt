package com.vineyard.aivideostudio.ui.screens.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vineyard.aivideostudio.core.model.PipelineStatus
import com.vineyard.aivideostudio.core.model.Project
import com.vineyard.aivideostudio.core.model.TimelineMap
import com.vineyard.aivideostudio.core.model.VideoMetadata
import com.vineyard.aivideostudio.core.util.FileUtils
import com.vineyard.aivideostudio.core.util.JsonUtils
import com.vineyard.aivideostudio.core.util.UriUtils
import com.vineyard.aivideostudio.data.storage.ProjectStorageManager
import com.vineyard.aivideostudio.media.video.VideoMetadataReader
import com.vineyard.aivideostudio.media.video.VideoValidator
import com.vineyard.aivideostudio.project.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateUiState(
    val projectName: String = "",
    val selectedVideoUri: Uri? = null,
    val videoMetadata: VideoMetadata? = null,
    val targetAspectRatio: String = "ORIGINAL", // ORIGINAL, 9:16, 16:9, 1:1
    val youtubeUrl: String = "",
    val isYoutubeMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdProjectId: String? = null
)

class CreateViewModel(
    private val context: Context,
    private val projectRepository: ProjectRepository,
    private val storageManager: ProjectStorageManager,
    private val metadataReader: VideoMetadataReader
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun onProjectNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(projectName = name, errorMessage = null)
    }

    fun onAspectRatioChanged(ratio: String) {
        _uiState.value = _uiState.value.copy(targetAspectRatio = ratio)
    }

    fun onYoutubeUrlChanged(url: String) {
        _uiState.value = _uiState.value.copy(youtubeUrl = url, errorMessage = null)
    }

    fun setSourceMode(isYoutube: Boolean) {
        _uiState.value = _uiState.value.copy(isYoutubeMode = isYoutube, errorMessage = null)
    }

    fun onVideoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val metadata = metadataReader.readMetadata(uri)
            val validationError = VideoValidator.validateSourceVideo(metadata)
            if (validationError != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = validationError
                )
            } else {
                val fileName = UriUtils.getFileName(context, uri).substringBeforeLast(".")
                val defaultName = if (_uiState.value.projectName.isBlank()) fileName else _uiState.value.projectName
                _uiState.value = _uiState.value.copy(
                    selectedVideoUri = uri,
                    videoMetadata = metadata,
                    projectName = defaultName,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun createProject() {
        val state = _uiState.value
        val name = if (state.projectName.isBlank()) "Video Project ${System.currentTimeMillis() % 1000}" else state.projectName

        if (state.selectedVideoUri == null && state.youtubeUrl.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please select a local video or enter a YouTube URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
                storageManager.setupProjectStructure(projectId)

                val metadata = state.videoMetadata ?: VideoMetadata()

                val sourcePath: String
                val sourceUriString: String

                if (state.selectedVideoUri != null) {
                    val destFile = storageManager.getSourceFile(projectId)
                    FileUtils.copyUriToFile(context, state.selectedVideoUri, destFile)
                    sourcePath = destFile.absolutePath
                    sourceUriString = Uri.fromFile(destFile).toString()
                } else {
                    sourcePath = state.youtubeUrl
                    sourceUriString = state.youtubeUrl
                }

                val initialTimelineMap = TimelineMap.identity(projectId, metadata.durationSeconds)

                val project = Project(
                    id = projectId,
                    name = name,
                    sourceUri = sourceUriString,
                    sourcePath = sourcePath,
                    currentVideoUri = sourceUriString,
                    metadata = metadata,
                    currentStage = PipelineStatus.IDLE,
                    status = PipelineStatus.IDLE,
                    targetAspectRatio = state.targetAspectRatio,
                    timelineMapJson = JsonUtils.toJson(initialTimelineMap)
                )

                projectRepository.saveProject(project)
                projectRepository.saveTimelineMap(projectId, initialTimelineMap)

                _uiState.value = state.copy(
                    isLoading = false,
                    createdProjectId = projectId
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to create project: ${e.message}"
                )
            }
        }
    }

    fun resetCreatedState() {
        _uiState.value = _uiState.value.copy(createdProjectId = null)
    }
}
