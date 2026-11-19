package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Models for Phase 6: Fellowship (E1–E8)
 */

@Serializable
data class FellowshipProgram(
    val id: Long,
    val title: String,
    val description: String? = null,
    val duration: String? = null,
    val specialty: String? = null,
    val eligibility: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("application_deadline") val applicationDeadline: String? = null,
    @SerialName("is_open") val isOpen: Boolean = true,
    val fee: Double? = null
)

@Serializable
data class FellowshipProgramsResponse(
    val success: Boolean = true,
    val records: List<FellowshipProgram> = emptyList()
)

@Serializable
data class FellowshipStats(
    @SerialName("active_fellows") val activeFellows: Int = 0,
    @SerialName("training_centers") val trainingCenters: Int = 0,
    @SerialName("graduated_fellows") val graduatedFellows: Int = 0,
    @SerialName("master_trainers") val masterTrainers: Int = 0,
    @SerialName("total_cases_logged") val totalCasesLogged: Int = 0
)

@Serializable
data class TrainingCenter(
    val id: Long,
    val name: String,
    val city: String? = null,
    val hospital: String? = null,
    val address: String? = null,
    @SerialName("supervisor_name") val supervisorName: String? = null,
    @SerialName("supervisor_email") val supervisorEmail: String? = null,
    @SerialName("supervisor_phone") val supervisorPhone: String? = null,
    val slots: Int? = null,
    @SerialName("available_slots") val availableSlots: Int? = null,
    val status: String? = "ACTIVE",
    val specialty: String? = null
)

@Serializable
data class TrainingCentersResponse(
    val success: Boolean = true,
    val records: List<TrainingCenter> = emptyList()
)

@Serializable
data class FellowApplicationRequest(
    @SerialName("program_id") val programId: Long,
    @SerialName("applicant_name") val applicantName: String,
    val email: String,
    val phone: String,
    val pmdc: String? = null,
    val qualifications: String? = null,
    @SerialName("current_institution") val currentInstitution: String? = null,
    @SerialName("preferred_center_id") val preferredCenterId: Long? = null,
    @SerialName("document_url") val documentUrl: String? = null,
    val statement: String? = null
)

@Serializable
data class TrainingCenterApplicationRequest(
    @SerialName("center_name") val centerName: String,
    val city: String,
    val address: String,
    @SerialName("head_of_department") val headOfDepartment: String,
    @SerialName("supervisor_name") val supervisorName: String,
    @SerialName("supervisor_email") val supervisorEmail: String,
    @SerialName("supervisor_phone") val supervisorPhone: String,
    @SerialName("bed_capacity") val bedCapacity: Int? = null,
    @SerialName("monthly_opd_volume") val monthlyOpdVolume: Int? = null,
    @SerialName("annual_surgeries") val annualSurgeries: Int? = null,
    val facilities: String? = null
)

@Serializable
data class FellowDashboardResponse(
    val success: Boolean = true,
    val fellow: FellowProfile? = null,
    val stats: FellowStats? = null,
    val cases: List<FellowCase> = emptyList(),
    val attendance: List<FellowAttendance> = emptyList(),
    val assessments: List<FellowAssessment> = emptyList(),
    val research: List<FellowResearch> = emptyList(),
    val certificates: List<UserCertificate> = emptyList()
)

@Serializable
data class TrainerDashboardResponse(
    val success: Boolean = true,
    val trainer: TrainerProfile? = null,
    val fellows: List<FellowProfile> = emptyList(),
    @SerialName("pending_cases") val pendingCases: List<FellowCase> = emptyList(),
    @SerialName("pending_attendance") val pendingAttendance: List<FellowAttendance> = emptyList(),
    @SerialName("pending_research") val pendingResearch: List<FellowResearch> = emptyList()
)

@Serializable
data class FellowProfile(
    val id: Long,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("training_center") val trainingCenter: String? = null,
    @SerialName("supervisor_name") val supervisorName: String? = null,
    @SerialName("enrollment_date") val enrollmentDate: String? = null,
    val status: String? = "ACTIVE",
    val progress: Double? = 0.0
)

@Serializable
data class TrainerProfile(
    val id: Long,
    val name: String,
    val center: String? = null,
    val designation: String? = null
)

@Serializable
data class FellowStats(
    @SerialName("total_cases") val totalCases: Int = 0,
    @SerialName("approved_cases") val approvedCases: Int = 0,
    @SerialName("attendance_percentage") val attendancePercentage: Double = 0.0,
    @SerialName("assessments_completed") val assessmentsCompleted: Int = 0,
    @SerialName("research_status") val researchStatus: String = "NOT_STARTED"
)

@Serializable
data class FellowCase(
    val id: Long,
    @SerialName("mrn_number") val mrnNumber: String? = null,
    @SerialName("patient_initials") val patientInitials: String? = null,
    val diagnosis: String,
    val procedure: String,
    val role: String? = "ASSISTANT", // PRIMARY, ASSISTANT, OBSERVER
    val date: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    @SerialName("supervisor_remarks") val supervisorRemarks: String? = null
)

@Serializable
data class CreateCaseRequest(
    @SerialName("mrn_number") val mrnNumber: String,
    @SerialName("patient_initials") val patientInitials: String? = null,
    val diagnosis: String,
    val procedure: String,
    val role: String,
    val date: String,
    val notes: String? = null
)

@Serializable
data class FellowAttendance(
    val id: Long,
    val date: String,
    val status: String = "PRESENT", // PRESENT, ABSENT, LEAVE
    @SerialName("verified_by_trainer") val verifiedByTrainer: Boolean = false
)

@Serializable
data class FellowAssessment(
    val id: Long,
    val title: String,
    val score: Double? = null,
    @SerialName("max_score") val maxScore: Double = 100.0,
    val feedback: String? = null,
    val date: String? = null
)

@Serializable
data class FellowResearch(
    val id: Long,
    val title: String,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, SUBMITTED, APPROVED, REVISION_REQUIRED
    @SerialName("document_url") val documentUrl: String? = null,
    @SerialName("supervisor_comments") val supervisorComments: String? = null
)

@Serializable
data class FellowshipFeedbackData(
    val part1Submitted: Boolean = false,
    val part2Submitted: Boolean = false
)

