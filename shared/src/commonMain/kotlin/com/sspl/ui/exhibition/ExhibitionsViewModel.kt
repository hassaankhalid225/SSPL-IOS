package com.sspl.ui.exhibition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.ExhibitionStall
import com.sspl.core.repositories.ExhibitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ExhibitionsUiState(
    val isLoading: Boolean = false,
    val exhibitions: List<ExhibitionStall> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false
)

class ExhibitionsViewModel(
    private val repository: ExhibitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExhibitionsUiState())
    val uiState: StateFlow<ExhibitionsUiState> = _uiState.asStateFlow()

    init {
        loadExhibitions()
    }

    fun loadExhibitions(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // Fetch status=ACCEPTED as per requirement
                val response = repository.getExhibitionStalls(status = "ACCEPTED")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false,
                        exhibitions = response.records,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }
}
