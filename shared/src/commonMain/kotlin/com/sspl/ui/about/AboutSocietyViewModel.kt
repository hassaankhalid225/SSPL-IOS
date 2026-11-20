package com.sspl.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PublicRepository
import com.sspl.ui.contact.ContactInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AboutUiState(
    val contactLoading: Boolean = true,
    val contact: ContactInfo = ContactInfo(),
    val isRefreshing: Boolean = false
)

/**
 * About SSP Lahore. Static content lives in the screen (web: pages/About.jsx);
 * the official contact block comes from GET /settings with the web's fallback values.
 */
class AboutSocietyViewModel(
    private val publicRepository: PublicRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AboutUiState())
    val state: StateFlow<AboutUiState> = _state.asStateFlow()

    init {
        load(refresh = false)
    }

    fun refresh() = load(refresh = true)

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = refresh, contactLoading = !refresh) }
            val result = publicRepository.fetchSiteSettings()
            _state.update {
                it.copy(
                    contact = ContactInfo.from(result.getOrNull()),
                    contactLoading = false,
                    isRefreshing = false
                )
            }
        }
    }
}
