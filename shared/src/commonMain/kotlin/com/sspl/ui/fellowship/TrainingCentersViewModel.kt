package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val ALL = "All"

data class TrainingCentersUiState(
    val centers: LoadState<List<DirectoryCenter>> = LoadState.Loading,
    val cities: List<String> = listOf(ALL),
    val availabilities: List<String> = listOf(ALL, "Open", "Limited", "Booked"),
    val query: String = "",
    val city: String = ALL,
    val availability: String = ALL,
    val filtered: List<DirectoryCenter> = emptyList(),
    val isRefreshing: Boolean = false
)

/** Mirrors TrainingCenterDirectory.jsx: one fetch, client-side city / availability / free-text filters. */
class TrainingCentersViewModel(private val repository: FellowshipRepository) : ViewModel() {

    private val _state = MutableStateFlow(TrainingCentersUiState())
    val state: StateFlow<TrainingCentersUiState> = _state.asStateFlow()

    init { load(false) }

    fun refresh() = load(true)
    fun retry() = load(false)

    fun setQuery(q: String) { _state.update { it.copy(query = q) }; applyFilters() }
    fun setCity(c: String) { _state.update { it.copy(city = c) }; applyFilters() }
    fun setAvailability(a: String) { _state.update { it.copy(availability = a) }; applyFilters() }
    fun resetFilters() { _state.update { it.copy(query = "", city = ALL, availability = ALL) }; applyFilters() }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update { if (refresh) it.copy(isRefreshing = true) else it.copy(centers = LoadState.Loading) }
            val result = runCatching {
                withContext(Dispatchers.Default) { repository.centersDirectory().objects().mapNotNull(::parseDirectoryCenter) }
            }
            result.onSuccess { list ->
                val cities = listOf(ALL) + list.mapNotNull { it.city?.trim()?.takeIf(String::isNotEmpty) }.distinct().sorted()
                _state.update {
                    it.copy(
                        centers = LoadState.Ready(list),
                        cities = cities,
                        city = if (it.city in cities) it.city else ALL,
                        isRefreshing = false
                    )
                }
                applyFilters()
            }.onFailure { e ->
                _state.update {
                    if (refresh && it.centers is LoadState.Ready) it.copy(isRefreshing = false)
                    else it.copy(centers = LoadState.Error(e.userMessage("Failed to load centers directory.")), isRefreshing = false)
                }
            }
        }
    }

    private fun applyFilters() {
        val s = _state.value
        val all = (s.centers as? LoadState.Ready)?.data ?: return
        viewModelScope.launch {
            val q = s.query.trim().lowercase()
            val filtered = withContext(Dispatchers.Default) {
                all.filter { c ->
                    (s.city == ALL || c.city.equals(s.city, ignoreCase = true)) &&
                        when (s.availability) {
                            "Open" -> c.availability == "OPEN"
                            "Limited" -> c.availability == "LIMITED"
                            "Booked" -> c.availability == "BOOKED"
                            else -> true
                        } &&
                        (q.isEmpty() || c.searchText.contains(q))
                }
            }
            _state.update { it.copy(filtered = filtered) }
        }
    }
}
