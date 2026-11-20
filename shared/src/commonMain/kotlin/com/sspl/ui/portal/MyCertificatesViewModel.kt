package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.UserCertificate
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyCertificatesUiState(
    val certificates: LoadState<List<UserCertificate>> = LoadState.Loading,
    val isRefreshing: Boolean = false,
    /** "All" + distinct certificate types, precomputed. */
    val filters: List<String> = listOf(ALL),
    val filter: String = ALL,
    val visible: List<UserCertificate> = emptyList(),
    val copiedSerial: String? = null
) {
    companion object { const val ALL = "All" }
}

fun UserCertificate.typeLabel(): String =
    type?.takeIf { it.isNotBlank() }?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Certificate"

class MyCertificatesViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _state = MutableStateFlow(MyCertificatesUiState())
    val state: StateFlow<MyCertificatesUiState> = _state.asStateFlow()

    init { load(true) }

    fun retry() = load(true)
    fun refresh() = load(false)

    private fun load(initial: Boolean) {
        _state.update { if (initial) it.copy(certificates = LoadState.Loading) else it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.getMyCertificates()
                .onSuccess { list ->
                    val sorted = list.sortedByDescending { it.issueDate ?: "" }
                    val types = sorted.map { it.typeLabel() }.distinct()
                    _state.update { s ->
                        val filters = if (types.size > 1) listOf(MyCertificatesUiState.ALL) + types else listOf(MyCertificatesUiState.ALL)
                        val f = s.filter.takeIf { it in filters } ?: MyCertificatesUiState.ALL
                        s.copy(
                            certificates = LoadState.Ready(sorted),
                            isRefreshing = false,
                            filters = filters,
                            filter = f,
                            visible = sorted.filterBy(f)
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { s ->
                        if (!initial && s.certificates is LoadState.Ready) s.copy(isRefreshing = false)
                        else s.copy(certificates = LoadState.Error(e.message ?: "Failed to load certificates"), isRefreshing = false)
                    }
                }
        }
    }

    private fun List<UserCertificate>.filterBy(f: String) =
        if (f == MyCertificatesUiState.ALL) this else filter { it.typeLabel() == f }

    fun setFilter(f: String) = _state.update { s ->
        val all = (s.certificates as? LoadState.Ready)?.data.orEmpty()
        s.copy(filter = f, visible = all.filterBy(f))
    }

    fun onCopied(serial: String) = _state.update { it.copy(copiedSerial = serial) }
    fun dismissCopied() = _state.update { it.copy(copiedSerial = null) }
}
