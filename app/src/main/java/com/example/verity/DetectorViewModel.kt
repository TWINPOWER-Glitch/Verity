package com.example.verity

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface DetectorUiState {
    data object Empty : DetectorUiState
    data object Loading : DetectorUiState
    data class Result(val result: AnalysisResult) : DetectorUiState
    data class Error(val message: String) : DetectorUiState
}

class DetectorViewModel(private val reader: MediaMetadataReader) : ViewModel() {
    private val _state = MutableStateFlow<DetectorUiState>(DetectorUiState.Empty)
    val state: StateFlow<DetectorUiState> = _state.asStateFlow()

    fun analyze(uri: Uri) {
        _state.value = DetectorUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                DetectorUiState.Result(DetectorCore.analyzeMetadata(reader.read(uri)))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                DetectorUiState.Error(error.message ?: "Unable to read this image.")
            }
        }
    }
}
