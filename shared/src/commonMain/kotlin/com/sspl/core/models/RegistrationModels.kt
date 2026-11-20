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
    val conference: Conference? = null // For "My Registrations" list
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

@Serializable
data class InitiatePaymentRequest(
    @kotlinx.serialization.EncodeDefault @SerialName("item_type") val itemType: String = "conference_registration",
    @SerialName("conference_registration_id") val conferenceRegistrationId: Long? = null,
    @SerialName("membership_id") val membershipId: Long? = null,
    val amount: Double? = null,
    val item: String? = null,
    val email: String? = null,
    val name: String? = null,
    @SerialName("msisdn") val msisdn: String? = null,
    val userId: Long? = null
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
    val transactionId: String? = null
) {
    val bestPaymentUrl: String?
        get() = paymentUrlSnake ?: paymentUrl
}
@Serializable
data class ManualPaymentRequest(
    @kotlinx.serialization.EncodeDefault @SerialName("payment_status") val paymentStatus: String = "SUCCESS", // Or PENDING verification
    @kotlinx.serialization.EncodeDefault @SerialName("payment_mode") val paymentMode: String = "BANK",
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("bank_name") val bankName: String? = null
)
