package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.FellowshipStats
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FellowshipHubUiState(
    val stats: LoadState<FellowshipStats> = LoadState.Loading,
    val programs: LoadState<List<Program>> = LoadState.Loading,
    val centers: LoadState<List<DirectoryCenter>> = LoadState.Loading,
    val totalCenters: Int = 0,
    val isRefreshing: Boolean = false,
    val isFellow: Boolean = false,
    val isTrainer: Boolean = false
)

class FellowshipHubViewModel(
    private val repository: FellowshipRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(FellowshipHubUiState())
    val state: StateFlow<FellowshipHubUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userSession.userFlow.collect { user ->
                _state.update {
                    it.copy(
                        isFellow = RoleUtils.hasUserRole(user, "FELLOWSHIP_FELLOW"),
                        isTrainer = RoleUtils.hasUserRole(user, "FELLOWSHIP_TRAINER")
                    )
                }
            }
        }
        load(refresh = false)
    }

    fun refresh() = load(refresh = true)
    fun retryPrograms() = load(refresh = false)

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                if (refresh) it.copy(isRefreshing = true)
                else it.copy(stats = LoadState.Loading, programs = LoadState.Loading, centers = LoadState.Loading)
            }
            val stats = async { runCatching { repository.publicStats() } }
            val programs = async {
                runCatching { withContext(Dispatchers.Default) { repository.openPrograms().objects().map(::parseProgram) } }
            }
            val centers = async {
                runCatching { withContext(Dispatchers.Default) { repository.centersDirectory().objects().mapNotNull(::parseDirectoryCenter) } }
            }
            val s = stats.await(); val p = programs.await(); val c = centers.await()
            _state.update { st ->
                st.copy(
                    isRefreshing = false,
                    stats = s.fold({ LoadState.Ready(it) }, { LoadState.Error(it.userMessage("Fellowship stats didn't load.")) }),
                    programs = p.fold({ LoadState.Ready(it) }, { LoadState.Error(it.userMessage("Fellowship programmes could not be loaded.")) }),
                    centers = c.fold({ LoadState.Ready(it.take(3)) }, { LoadState.Error(it.userMessage("Training centres could not be loaded.")) }),
                    totalCenters = c.getOrNull()?.size ?: st.totalCenters
                )
            }
        }
    }
}
