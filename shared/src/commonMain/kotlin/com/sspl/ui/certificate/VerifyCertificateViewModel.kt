package com.sspl.ui.certificate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.repositories.PublicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/** Normalised certificate facts (conference + fellowship verify responses use different keys). */
data class VerifiedCertificate(
    val issuedTo: String,
    val eventName: String,
    val trainingCenter: String?,
    val serial: String,
    val issuedAt: String?,
    val type: String,
    val fileUrl: String?,
    val isRevoked: Boolean
)

sealed interface VerifyResult {
    data object Idle : VerifyResult
    data object Loading : VerifyResult
    data class Found(val certificate: VerifiedCertificate) : VerifyResult
    /** Server answered: not found / invalid serial. */
    data class Invalid(val message: String) : VerifyResult
    /** Could not reach the server. */
    data class Failed(val message: String) : VerifyResult
}

data class VerifyUiState(
    val serial: String = "",
    val inputError: String? = null,
    val result: VerifyResult = VerifyResult.Idle
)

/** Web: pages/VerifyCertificatePage.jsx (FELLOW- prefix → fellowship endpoint). */
class VerifyCertificateViewModel(
    private val repo: PublicRepository,
    initialSerial: String?
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState(serial = initialSerial.orEmpty()))
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()
    private var job: Job? = null

    init {
        if (!initialSerial.isNullOrBlank()) verify()
    }

    fun onSerial(v: String) = _state.update { it.copy(serial = v, inputError = null) }

    fun verify() {
        val serial = _state.value.serial.trim()
        if (serial.isEmpty()) {
            _state.update { it.copy(inputError = "Enter the serial number printed on the certificate.") }
            return
        }
        if (_state.value.result is VerifyResult.Loading) return
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { it.copy(result = VerifyResult.Loading, inputError = null) }
            val res = repo.verifyCertificate(serial)
            _state.update {
                it.copy(
                    result = res.fold(
                        onSuccess = { data -> interpret(data, serial) },
                        onFailure = { e -> VerifyResult.Failed(e.message ?: "Failed to verify certificate. Please try again.") }
                    )
                )
            }
        }
    }

    private fun JsonObject.s(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject
    private fun JsonObject.b(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull == true

    private fun personName(o: JsonObject?): String? = o?.let {
        listOfNotNull(it.s("first_name"), it.s("last_name")).joinToString(" ").takeIf { n -> n.isNotBlank() }
    }

    private fun interpret(data: JsonObject, typed: String): VerifyResult {
        val cert = data.obj("certificate")
        val revoked = data.b("isRevoked") || cert?.s("status")?.uppercase() == "REVOKED"
        val verified = data.b("verified")
        if (cert == null || (!verified && !revoked)) {
            return VerifyResult.Invalid(data.s("message") ?: "Certificate not found or invalid serial number.")
        }
        val issuedTo = cert.s("attendeeName") ?: cert.s("fellow_name") ?: cert.s("recipient_name")
            ?: personName(cert.obj("user")) ?: personName(cert.obj("person"))
            ?: "ID: ${cert.s("personId") ?: "N/A"}"
        val event = cert.s("conferenceName") ?: cert.s("program_name") ?: cert.s("event_name")
            ?: cert.obj("conference")?.s("title") ?: cert.obj("session")?.s("title")
            ?: "Session ID: ${cert.s("sessionId") ?: "N/A"}"
        return VerifyResult.Found(
            VerifiedCertificate(
                issuedTo = issuedTo,
                eventName = event,
                trainingCenter = cert.s("training_center"),
                serial = cert.s("serialNumber") ?: cert.s("serial_number") ?: cert.s("serial") ?: typed,
                issuedAt = cert.s("issuedAt") ?: cert.s("issued_at") ?: cert.s("issue_date"),
                type = data.s("type") ?: cert.s("type") ?: "CONFERENCE",
                fileUrl = cert.s("fileUrl") ?: cert.s("file_url"),
                isRevoked = revoked
            )
        )
    }
}
