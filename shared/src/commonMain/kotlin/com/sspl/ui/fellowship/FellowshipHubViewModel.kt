package com.sspl.ui.fellowship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.FellowshipStats
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.session.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    /** Programme the centre list is filtered by (null = all programmes). */
    val selectedProgramId: Long? = null,
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

    /** Web TrainingCenterDirectory `programId={selectedId}`: the centre list follows the selected programme. */
    fun selectCentreProgram(programId: Long?) {
        if (programId == _state.value.selectedProgramId) return
        _state.update { it.copy(selectedProgramId = programId) }
        loadCenters(programId)
    }

    private var centersJob: Job? = null

    private fun loadCenters(programId: Long?) {
        centersJob?.cancel()
        centersJob = viewModelScope.launch {
            _state.update { it.copy(centers = LoadState.Loading) }
            val c = runCatching {
                withContext(Dispatchers.Default) { repository.centersDirectory(programId).objects().mapNotNull(::parseDirectoryCenter) }
            }
            _state.update { st ->
                st.copy(
                    centers = c.fold({ LoadState.Ready(it.take(3)) }, { LoadState.Error(it.userMessage("Training centres could not be loaded.")) }),
                    totalCenters = c.getOrNull()?.size ?: st.totalCenters
                )
            }
        }
    }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                if (refresh) it.copy(isRefreshing = true)
                else it.copy(stats = LoadState.Loading, programs = LoadState.Loading, centers = LoadState.Loading)
            }
            // Web DynamicCounter: live figures over built-in base counts; never an error state.
            val stats = async { repository.publicStatsOrDefaults() }
            val programs = async {
                runCatching { withContext(Dispatchers.Default) { repository.openPrograms().objects().map(::parseProgram) } }
            }
            val p = programs.await()
            // Web FellowshipHubPage: a single open programme is auto-selected, which also filters the directory.
            val selected = p.getOrNull()?.singleOrNull()?.id ?: _state.value.selectedProgramId?.takeIf { id ->
                p.getOrNull()?.any { it.id == id } == true
            }
            centersJob?.cancel()
            val c = runCatching {
                withContext(Dispatchers.Default) { repository.centersDirectory(selected).objects().mapNotNull(::parseDirectoryCenter) }
            }
            val s = stats.await()
            _state.update { st ->
                st.copy(
                    isRefreshing = false,
                    stats = LoadState.Ready(s),
                    programs = p.fold({ LoadState.Ready(it) }, { LoadState.Error(it.userMessage("Fellowship programmes could not be loaded.")) }),
                    selectedProgramId = selected,
                    centers = c.fold({ LoadState.Ready(it.take(3)) }, { LoadState.Error(it.userMessage("Training centres could not be loaded.")) }),
                    totalCenters = c.getOrNull()?.size ?: st.totalCenters
                )
            }
        }
    }
}
