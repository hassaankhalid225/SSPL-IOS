package com.sspl.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.ContactInquiryRequest
import com.sspl.core.repositories.PublicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContactForm(
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val messageError: String? = null
)

data class ContactUiState(
    val loading: Boolean = true,
    val isRefreshing: Boolean = false,
    val contact: ContactInfo = ContactInfo(),
    val form: ContactForm = ContactForm(),
    val submitting: Boolean = false,
    /** Result banner after submit: (message, isError). */
    val submitResult: Pair<String, Boolean>? = null
)

/** Web: components/contact/Contact.jsx — GET /settings (with fallback) + POST /config/contact. */
class ContactViewModel(private val publicRepository: PublicRepository) : ViewModel() {

    private val _state = MutableStateFlow(ContactUiState())
    val state: StateFlow<ContactUiState> = _state.asStateFlow()

    init { loadSettings(false) }

    fun refresh() = loadSettings(true)

    private fun loadSettings(refresh: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(loading = !refresh, isRefreshing = refresh) }
            val result = publicRepository.fetchSiteSettings()
            _state.update {
                it.copy(loading = false, isRefreshing = false, contact = ContactInfo.from(result.getOrNull()))
            }
        }
    }

    fun onName(v: String) = _state.update { it.copy(form = it.form.copy(name = v, nameError = null), submitResult = null) }
    fun onEmail(v: String) = _state.update { it.copy(form = it.form.copy(email = v, emailError = null), submitResult = null) }
    fun onMessage(v: String) = _state.update { it.copy(form = it.form.copy(message = v, messageError = null), submitResult = null) }

    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun submit() {
        val s = _state.value
        if (s.submitting) return
        val f = s.form
        val checked = f.copy(
            nameError = if (f.name.isBlank()) "Please enter your name." else null,
            emailError = when {
                f.email.isBlank() -> "Please enter your email."
                !emailRegex.matches(f.email.trim()) -> "Please enter a valid email address."
                else -> null
            },
            messageError = if (f.message.isBlank()) "Please write your message." else null
        )
        if (checked.nameError != null || checked.emailError != null || checked.messageError != null) {
            _state.update {
                it.copy(form = checked, submitResult = "Please fill in all required fields." to true)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, submitResult = null) }
            val result = publicRepository.submitContactInquiry(
                ContactInquiryRequest(name = f.name.trim(), email = f.email.trim(), message = f.message.trim())
            )
            result.fold(
                onSuccess = { msg ->
                    _state.update {
                        it.copy(
                            submitting = false,
                            form = ContactForm(),
                            submitResult = (msg ?: "Thank you! Your message has been delivered to the SSPL Secretariat. We will get back to you shortly.") to false
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            submitting = false,
                            // The inquiry endpoint may be unavailable on this backend (404); keep the
                            // typed message and point to email (the screen offers a mail hand-off).
                            submitResult = "Unable to send message right now. Please reach us at ${it.contact.email}." to true
                        )
                    }
                }
            )
        }
    }
}
