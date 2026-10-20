package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ModelInfo
import com.example.engine.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModelManagerUiState(
    val availableRamMb: Long = 0L,
    val totalRamMb: Long = 0L,
    val isImporting: Boolean = false,
    val statusMessage: String? = null,
    val isError: Boolean = false
)

class ModelManagerViewModel(
    private val modelRepository: ModelRepository
) : ViewModel() {

    val currentModel: StateFlow<ModelInfo?> = modelRepository.currentModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ModelManagerUiState())
    val uiState: StateFlow<ModelManagerUiState> = _uiState.asStateFlow()

    init {
        refreshRamStats()
    }

    fun refreshRamStats() {
        val (avail, total) = modelRepository.getDeviceMemoryInfo()
        _uiState.value = _uiState.value.copy(
            availableRamMb = avail,
            totalRamMb = total
        )
    }

    fun importModel(uri: Uri) {
        _uiState.value = _uiState.value.copy(isImporting = true, statusMessage = "Reading local model file...", isError = false)
        viewModelScope.launch {
            val result = modelRepository.importModelFromUri(uri)
            refreshRamStats()
            if (result.isSuccess) {
                val model = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    statusMessage = if (model?.isCompatible == true)
                        "Model '${model.fileName}' imported successfully."
                    else
                        model?.errorMessage ?: "Imported with compatibility warning.",
                    isError = model?.isCompatible == false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    statusMessage = "Import failed: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown file error"}",
                    isError = true
                )
            }
        }
    }

    fun loadModel() {
        val success = modelRepository.loadModel()
        _uiState.value = _uiState.value.copy(
            statusMessage = if (success) "Local model loaded into active memory." else "Unable to load model (incompatible format or memory constraint).",
            isError = !success
        )
    }

    fun unloadModel() {
        modelRepository.unloadModel()
        _uiState.value = _uiState.value.copy(
            statusMessage = "Model unloaded. Reverted to Offline Heuristic Engine.",
            isError = false
        )
    }

    fun deleteModel() {
        val success = modelRepository.removeModel()
        _uiState.value = _uiState.value.copy(
            statusMessage = if (success) "Local model file removed from app storage." else "Could not delete file.",
            isError = !success
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, isError = false)
    }
}
