package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.MembershipPlan
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MembershipPlansUiState(
    val plans: LoadState<List<MembershipPlan>> = LoadState.Loading,
    val isRefreshing: Boolean = false,
    val selectedPlanId: Long? = null,
    val isApplying: Boolean = false,
    val paymentUrl: String? = null,
    val message: String? = null,
    val isError: Boolean = false
) {
    val selectedPlan: MembershipPlan?
        get() = (plans as? LoadState.Ready)?.data?.firstOrNull { it.id == selectedPlanId }
}

/** Web `isPopular`: yearly / ordinary / regular plans get the "Most chosen" tag. */
fun MembershipPlan.isPopular(): Boolean =
    type.equals("YEAR", true) || Regex("ordinary|regular", RegexOption.IGNORE_CASE).containsMatchIn(name)

/** Web `getDurationText`. */
fun MembershipPlan.durationText(): String = when (type?.uppercase()) {
    "LIFE" -> "Lifetime"
    "YEAR" -> "Per year"
    "MONTH" -> "Per month"
    else -> duration ?: type?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
}

/** Web `getFeatures` (plans have no feature list in the API). */
fun MembershipPlan.features(): List<String> {
    benefits?.takeIf { it.isNotEmpty() }?.let { return it }
    val key = (type ?: name).uppercase()
    val nameKey = name.uppercase()
    val base = listOf("Access to society events", "Member directory listing", "Newsletter subscription")
    return when {
        key == "LIFE" || nameKey.contains("LIFE") ->
            base + listOf("Lifetime membership", "Priority event registration", "Exclusive networking", "Voting rights")
        key == "STUDENT" || nameKey.contains("STUDENT") ->
            base + listOf("Student discounted rate", "Career development resources", "Mentorship opportunities")
        nameKey.contains("ASSOCIATE") ->
            base + listOf("Associate member benefits", "Event access", "Professional networking")
        else -> base + listOf("Annual membership renewal", "Event discounts", "Voting rights")
    }
}

class MembershipPlansViewModel(private val repository: PortalRepository) : ViewModel() {

    private val _state = MutableStateFlow(MembershipPlansUiState())
    val state: StateFlow<MembershipPlansUiState> = _state.asStateFlow()

    init { load(true) }

    fun retry() = load(true)
    fun refresh() = load(false)

    private fun load(initial: Boolean) {
        _state.update { if (initial) it.copy(plans = LoadState.Loading) else it.copy(isRefreshing = true) }
        viewModelScope.launch {
            repository.getMembershipPlans()
                .onSuccess { list ->
                    val active = list.filter { !it.name.lowercase().contains("deceased") && it.isActive }
                    _state.update { s ->
                        s.copy(
                            plans = LoadState.Ready(active),
                            isRefreshing = false,
                            selectedPlanId = s.selectedPlanId?.takeIf { id -> active.any { it.id == id } }
                                ?: active.firstOrNull { it.isPopular() }?.id ?: active.firstOrNull()?.id
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { s ->
                        if (!initial && s.plans is LoadState.Ready) s.copy(isRefreshing = false, message = e.message, isError = true)
                        else s.copy(plans = LoadState.Error(e.message ?: "Failed to load membership plans"), isRefreshing = false)
                    }
                }
        }
    }

    fun select(id: Long) = _state.update { it.copy(selectedPlanId = id, message = null) }

    /** POST /membership/register → payment page (same endpoint as before). */
    fun apply() {
        val plan = _state.value.selectedPlan ?: return
        if (_state.value.isApplying) return
        _state.update { it.copy(isApplying = true, message = null) }
        viewModelScope.launch {
            repository.registerMembership(plan.id)
                .onSuccess { url ->
                    _state.update {
                        it.copy(
                            isApplying = false,
                            paymentUrl = url,
                            message = if (url == null) "Your application for ${plan.name} was submitted. You'll be notified after review." else null,
                            isError = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isApplying = false, message = e.message ?: "Application failed. Please try again.", isError = true) }
                }
        }
    }

    fun onPaymentFinished(success: Boolean) = _state.update {
        it.copy(
            paymentUrl = null,
            message = if (success) "Payment received. Your membership application is now under review."
            else "Payment was not completed. You can try again any time.",
            isError = !success
        )
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }
}
