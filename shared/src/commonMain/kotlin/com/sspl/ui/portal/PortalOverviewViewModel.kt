package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.User
import com.sspl.core.models.UserMembershipDetails
import com.sspl.core.repositories.PortalRegistration
import com.sspl.core.repositories.PortalRepository
import com.sspl.session.UserSession
import com.sspl.utils.DisplayDates
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Counts are null while loading and when that call failed (rendered as "—"). */
data class PortalOverviewUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val registrations: Int? = null,
    val certificates: Int? = null,
    val submissions: Int? = null,
    val presentations: Int? = null,
    val membership: UserMembershipDetails? = null,
    val upcoming: List<PortalRegistration> = emptyList(),
    /** Sections still loading / failed (keys in [PortalOverviewViewModel.ALL_KEYS]). */
    val loading: Set<String> = emptySet(),
    val failed: Set<String> = emptySet(),
    /** Set when one or more sections failed to load. */
    val partialError: String? = null
)

class PortalOverviewViewModel(
    private val repository: PortalRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(PortalOverviewUiState())
    val state: StateFlow<PortalOverviewUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        viewModelScope.launch { userSession.userFlow.collect { u -> _state.update { it.copy(user = u) } } }
        load(initial = true)
    }

    fun refresh() = load(initial = false)

    private fun load(initial: Boolean) {
        job?.cancel()
        _state.update {
            if (initial) it.copy(isLoading = true, loading = ALL_KEYS, failed = emptySet())
            else it.copy(isRefreshing = true, loading = ALL_KEYS, failed = emptySet())
        }
        // Each section loads on its own: /abstractSubmission/ can take 30 s+, it must not block the rest.
        job = viewModelScope.launch {
            launch {
                val r = repository.getMyRegistrations()
                val today = DisplayDates.today()
                val upcoming = r.getOrNull().orEmpty().filter { it.isConfirmed }
                    .filter { reg -> DisplayDates.parse(reg.endDate ?: reg.startDate)?.let { it >= today } ?: false }
                    .sortedBy { it.startDate ?: "" }
                    .take(3)
                finish(KEY_REGS, r.isSuccess) { it.copy(registrations = r.getOrNull()?.size ?: it.registrations, upcoming = if (r.isSuccess) upcoming else it.upcoming, isLoading = false) }
            }
            launch {
                val r = repository.getMyCertificates()
                finish(KEY_CERTS, r.isSuccess) { it.copy(certificates = r.getOrNull()?.size ?: it.certificates) }
            }
            launch {
                val r = repository.getMyPresentations()
                finish(KEY_PRES, r.isSuccess) { it.copy(presentations = r.getOrNull()?.size ?: it.presentations) }
            }
            launch {
                val r = repository.getMySubmissions(page = 1, limit = 1)
                finish(KEY_SUBS, r.isSuccess) { it.copy(submissions = r.getOrNull()?.totalRecords ?: it.submissions) }
            }
            launch {
                // 403 for regular members (admin-only endpoint) — optional enrichment, never an error.
                repository.getMyMembership().getOrNull()?.let { m -> _state.update { it.copy(membership = m) } }
            }
        }
    }

    private fun finish(key: String, ok: Boolean, apply: (PortalOverviewUiState) -> PortalOverviewUiState) {
        _state.update { s ->
            val next = apply(s)
            val loading = s.loading - key
            val failed = if (ok) s.failed - key else s.failed + key
            next.copy(
                loading = loading,
                failed = failed,
                isRefreshing = if ((loading - KEY_SUBS).isEmpty()) false else s.isRefreshing,
                partialError = if (failed.isNotEmpty()) "Some of your portal data didn't load. Pull down to try again." else null
            )
        }
    }

    companion object {
        const val KEY_REGS = "regs"
        const val KEY_CERTS = "certs"
        const val KEY_SUBS = "subs"
        const val KEY_PRES = "pres"
        val ALL_KEYS = setOf(KEY_REGS, KEY_CERTS, KEY_SUBS, KEY_PRES)
    }
}
