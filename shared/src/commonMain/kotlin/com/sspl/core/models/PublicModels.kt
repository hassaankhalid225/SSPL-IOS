package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocietyNewsItem(
    val id: String? = null,
    @SerialName("_id") val mongoId: String? = null,
    val title: String? = null,
    val message: String? = null,
    val description: String? = null,
    val image: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_visible") val isVisible: Boolean = true
) {
    val effectiveId: String get() = id ?: mongoId ?: ""
    val effectiveImage: String? get() = imageUrl ?: image
    val effectiveBody: String get() = message ?: description ?: ""
}

@Serializable
data class PublicEventItem(
    val id: Long? = null,
    @SerialName("_id") val mongoId: String? = null,
    val title: String = "",
    val description: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("eventStart") val eventStart: String? = null,
    @SerialName("eventEnd") val eventEnd: String? = null,
    val venue: String? = null,
    val location: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
) {
    val effectiveStart: String? get() = startDate ?: eventStart
    val effectiveEnd: String? get() = endDate ?: eventEnd
    val effectiveVenue: String get() = venue ?: location ?: "Venue TBA"
}

@Serializable
data class ElectionAttachment(
    val url: String = "",
    val name: String? = null,
    val type: String? = null
)

@Serializable
data class ElectionItem(
    val id: Long = 0,
    val title: String = "",
    val description: String? = null,
    val status: String? = "ACTIVE",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_type") val fileType: String? = null,
    val files: List<ElectionAttachment> = emptyList(),
    val attachments: List<ElectionAttachment> = emptyList()
) {
    val allFiles: List<ElectionAttachment>
        get() {
            val list = mutableListOf<ElectionAttachment>()
            list.addAll(files)
            list.addAll(attachments)
            if (!fileUrl.isNullOrBlank()) {
                list.add(ElectionAttachment(url = fileUrl, name = fileName ?: title, type = fileType))
            }
            return list.filter { it.url.isNotBlank() }.distinctBy { it.url }
        }
}

@Serializable
data class DownloadItem(
    val id: Long = 0,
    val title: String = "",
    val description: String? = null,
    val category: String? = null,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("file_size") val fileSize: Long? = null,
    @SerialName("file_type") val fileType: String? = null,
    @SerialName("previewable") val previewable: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class DownloadsMeta(
    val page: Int = 1,
    @SerialName("total_pages") val totalPages: Int = 1,
    val total: Int = 0,
    val limit: Int = 12
)

@Serializable
data class DownloadsResponse(
    val items: List<DownloadItem> = emptyList(),
    val meta: DownloadsMeta = DownloadsMeta()
)

@Serializable
data class CertificateData(
    val serial: String? = null,
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("event_name") val eventName: String? = null,
    @SerialName("program_name") val programName: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerialName("issued_at") val issuedAt: String? = null,
    @SerialName("issue_date") val issueDate: String? = null,
    val role: String? = null,
    val cme: String? = null
)

@Serializable
data class CertificateVerifyResponse(
    val verified: Boolean = false,
    @SerialName("isRevoked") val isRevoked: Boolean = false,
    val certificate: CertificateData? = null,
    val message: String? = null
)

@Serializable
data class DirectoryAccountUser(
    val prefix: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("profile_picture") val profilePicture: String? = null,
    @SerialName("pmdc_number") val pmdcNumber: String? = null,
    val designation: String? = null,
    val institute: String? = null,
    val city: String? = null
) {
    val fullName: String
        get() {
            val parts = listOfNotNull(
                prefix?.takeIf { it.isNotBlank() },
                firstName?.takeIf { it.isNotBlank() },
                lastName?.takeIf { it.isNotBlank() }
            )
            return if (parts.isNotEmpty()) parts.joinToString(" ") else "Member"
        }
}

@Serializable
data class DirectoryAccount(
    @SerialName("mem_no") val memNo: String? = null,
    val user: DirectoryAccountUser? = null
)

@Serializable
data class DirectoryMemberRecord(
    val id: Long = 0,
    val role: String? = null,
    val account: DirectoryAccount? = null
)

@Serializable
data class DirectoryStatistics(
    @SerialName("life_members") val lifeMembers: Int = 0,
    @SerialName("regular_members") val regularMembers: Int = 0,
    @SerialName("deceased_members") val deceasedMembers: Int = 0,
    @SerialName("number_of_speakers") val numberOfSpeakers: Int = 0
)

@Serializable
data class DirectoryMeta(
    @SerialName("total_records") val totalRecords: Int = 0,
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("total_pages") val totalPages: Int = 1,
    val limit: Int = 10
)

@Serializable
data class MembersDirectoryResponse(
    val records: List<DirectoryMemberRecord> = emptyList(),
    val statistics: DirectoryStatistics = DirectoryStatistics(),
    val meta: DirectoryMeta = DirectoryMeta()
)

@Serializable
data class SiteContactSettings(
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val whatsapp: String? = null,
    @SerialName("mapUrl") val mapUrl: String? = null
)

@Serializable
data class SiteSettingsResponse(
    @SerialName("siteName") val siteName: String? = null,
    val contacts: SiteContactSettings? = null
)

@Serializable
data class ContactInquiryRequest(
    val name: String,
    val email: String,
    val message: String
)

@Serializable
data class ContactInquiryResponse(
    val success: Boolean = true,
    val message: String? = null
)

