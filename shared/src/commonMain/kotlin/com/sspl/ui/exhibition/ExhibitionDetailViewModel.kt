package com.sspl.ui.exhibition

import androidx.lifecycle.ViewModel
import com.sspl.core.models.ExhibitionStall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

data class ExhibitionDetailUiState(
    val exhibition: ExhibitionStall? = null,
    val error: String? = null
)

class ExhibitionDetailViewModel(
    private val exhibitionJson: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExhibitionDetailUiState())
    val uiState: StateFlow<ExhibitionDetailUiState> = _uiState.asStateFlow()

    private val jsonHelper = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        loadData()
    }

    private fun loadData() {
        if (!exhibitionJson.isNullOrBlank()) {
            try {
                val data = jsonHelper.decodeFromString<ExhibitionStall>(exhibitionJson)
                _uiState.update { it.copy(exhibition = data, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to parse exhibition data: ${e.message}") }
            }
        } else {
            _uiState.update { it.copy(error = "No exhibition data found") }
        }
    }
}
