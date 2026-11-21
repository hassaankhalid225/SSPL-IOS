@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Long = 0,
    val name: String = "",
    val fee: Double = 0.0, // API sends Int ("fee": 5000) but Double is safer for money
    val description: String? = null,
    @SerialName("conference_id") val conferenceId: Long? = null,
    @SerialName("active_status") val activeStatus: String? = null
)

@Serializable
data class Registration(
    val id: Long,
    @SerialName("person_id") val personId: Long? = null,
    @SerialName("conference_id") val conferenceId: Long? = null,
    @SerialName("role_id") val roleId: Long? = null,
    @SerialName("fee_amount") val feeAmount: Double = 0.0,
    @SerialName("payment_status") val paymentStatus: String = "PENDING", // PENDING | SUCCESS | FAILED | REFUNDED
    @kotlinx.serialization.EncodeDefault @SerialName("payment_mode") val paymentMode: String? = null, // CASH | BANK | CARD | null
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("payment_date") val paymentDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null, // PENDING | APPROVED | REJECTED
    val status: String? = null, // CONFIRMED | CANCELLED | null
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("registration_number") val registrationNumber: String? = null,
    val role: Role? = null,
    val conference: Conference? = null, // For "My Registrations" list
    /** Web BadgeModal QR `uid` / `registration_id`; kept raw so the QR JSON matches the web byte-for-byte. */
    @SerialName("registration_id") val registrationIdRaw: kotlinx.serialization.json.JsonElement? = null,
    val person: RegistrationPerson? = null
) {
    val isConfirmed: Boolean
        get() = approvalStatus.equals("APPROVED", ignoreCase = true) ||
                status.equals("CONFIRMED", ignoreCase = true) ||
                ((paymentStatus.equals("SUCCESS", ignoreCase = true) || paymentStatus.equals("COMPLETED", ignoreCase = true)) &&
                        !approvalStatus.equals("REJECTED", ignoreCase = true))

    val isPendingApproval: Boolean
        get() = approvalStatus.equals("PENDING", ignoreCase = true) ||
                (paymentStatus.equals("PENDING", ignoreCase = true) && feeAmount == 0.0 && !isConfirmed)

    val needsPayment: Boolean
        get() = (paymentStatus.equals("PENDING", ignoreCase = true) || paymentStatus.equals("FAILED", ignoreCase = true)) && feeAmount > 0.0
}

@Serializable
data class RegistrationPerson(
    val id: Long? = null,
    val prefix: String? = null,
    val salutation: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val hospital: String? = null,
    val institution: String? = null,
    val city: String? = null
)

@Serializable
data class CheckRegistrationResponse(
    @SerialName("is_registered") val isRegistered: Boolean = false,
    val registration: Registration? = null
)

@Serializable
data class RegisterUserRequest(
    @SerialName("role_id") val roleId: Long
)

@Serializable
data class RegisterUserResponse(
    val message: String? = null,
    val registration: Registration? = null,
    val error: String? = null
)

@Serializable
data class RegistrationListResponse(
    val records: List<Registration>,
    @SerialName("_meta") val meta: PaginationMeta? = null
)

/**
 * POST /swich/initiate-payment — same keys as web `initiateSwichPayment` (eventsApi.js):
 * { amount, item, msisdn, name, email, userId, membershipId?, conferenceRegistrationId? }.
 * Null fields are dropped (explicitNulls = false).
 */
@Serializable
data class InitiatePaymentRequest(
    val amount: Double? = null,
    val item: String? = null,
    val msisdn: String? = null,
    val name: String? = null,
    val email: String? = null,
    val userId: Long? = null,
    val membershipId: Long? = null,
    val conferenceRegistrationId: Long? = null
)

@Serializable
data class InitiatePaymentResponse(
    @SerialName("payment_url") val paymentUrlSnake: String? = null,
    @SerialName("customer_transaction_id") val customerTransactionId: String? = null,
    val amount: Double? = null,
    val item: String? = null,
    
    // Legacy / Alternative format support
    val success: Boolean? = null,
    val paymentUrl: String? = null,
    val transactionId: String? = null,
    /** Old response shape: { payment: { paymentUrl, transactionId } } (web falls back to it). */
    val payment: InitiatePaymentNested? = null
) {
    val bestPaymentUrl: String?
        get() = paymentUrl ?: paymentUrlSnake ?: payment?.paymentUrl

    val bestTransactionId: String?
        get() = transactionId ?: customerTransactionId ?: payment?.transactionId
}

@Serializable
data class InitiatePaymentNested(
    val paymentUrl: String? = null,
    val transactionId: String? = null
)
@Serializable
data class ManualPaymentRequest(
    @kotlinx.serialization.EncodeDefault @SerialName("payment_status") val paymentStatus: String = "SUCCESS", // Or PENDING verification
    @kotlinx.serialization.EncodeDefault @SerialName("payment_mode") val paymentMode: String = "BANK",
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("bank_name") val bankName: String? = null
)
