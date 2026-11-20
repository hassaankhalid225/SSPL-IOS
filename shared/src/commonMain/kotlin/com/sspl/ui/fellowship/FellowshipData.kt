package com.sspl.ui.fellowship

import com.sspl.core.repositories.FellowshipApiException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// =====================================================================================
// JSON helpers (fellowship payloads are config-driven and loosely typed)
// =====================================================================================

internal fun JsonObject?.obj(key: String): JsonObject? = this?.get(key) as? JsonObject
internal fun JsonObject?.arr(key: String): JsonArray = (this?.get(key) as? JsonArray) ?: JsonArray(emptyList())
internal fun JsonObject?.str(key: String): String? =
    (this?.get(key) as? JsonPrimitive)?.takeIf { it !is JsonNull }?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
internal fun JsonObject?.long(key: String): Long? = (this?.get(key) as? JsonPrimitive)?.let { it.longOrNull ?: it.contentOrNull?.toLongOrNull() }
internal fun JsonObject?.int(key: String): Int? = long(key)?.toInt()
internal fun JsonObject?.dbl(key: String): Double? = (this?.get(key) as? JsonPrimitive)?.let { it.doubleOrNull ?: it.contentOrNull?.toDoubleOrNull() }
internal fun JsonObject?.bool(key: String): Boolean? = (this?.get(key) as? JsonPrimitive)?.let { it.booleanOrNull ?: it.contentOrNull?.toBooleanStrictOrNull() }
internal fun JsonArray.objects(): List<JsonObject> = mapNotNull { it as? JsonObject }
internal fun JsonArray.strings(): List<String> = mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { s -> s.isNotEmpty() } }

/** Strings or `{name}` objects → names. */
internal fun JsonArray.names(): List<String> = mapNotNull { el ->
    when (el) {
        is JsonPrimitive -> el.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        is JsonObject -> el.str("name")
        else -> null
    }
}

/** Some backend strings were double-encoded (UTF-8 read as Latin-1). Repair the common dash/quote cases. */
internal fun String.fixMojibake(): String = this
    .replace("â€”", "—")
    .replace("â€“", "–")
    .replace("â€™", "’")
    .replace("â€˜", "‘")

internal fun todayIso(): String =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

internal fun currentYear(): Int =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

/** "2026-10-01T15:55:50Z" → "01 Oct 2026". Falls back to the raw date part. */
internal fun prettyDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val d = raw.take(10)
    val parts = d.split("-")
    if (parts.size != 3) return raw
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val m = parts[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return d
    return "${parts[2]} ${months[m - 1]} ${parts[0]}"
}

internal fun String.humanize(): String = replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

/** User-facing error text from any throwable. */
internal fun Throwable.userMessage(fallback: String = "Something went wrong. Please try again."): String = when (this) {
    is FellowshipApiException -> message
    else -> {
        val m = message.orEmpty()
        if (m.contains("Unable to resolve host", true) || m.contains("timeout", true) ||
            m.contains("connect", true) || m.contains("network", true)
        ) "You appear to be offline. Check your connection and try again." else fallback
    }
}

// =====================================================================================
// Programme (resolveProgram in programModel.js)
// =====================================================================================

data class FieldDef(val key: String, val label: String, val required: Boolean, val help: String?)

data class QuestionDef(val id: String, val label: String, val type: String, val required: Boolean, val help: String?, val options: List<String>)

data class CategoryDef(
    val code: String,
    val name: String,
    val shortLabel: String,
    val description: String,
    val minimumExperience: String?,
    val questions: List<QuestionDef>
)

data class PreferenceDef(
    val id: String,
    val label: String,
    val type: String, // radio | dropdown | textarea | checkbox | text | number | date
    val required: Boolean,
    val help: String?,
    val options: List<String>,
    val showWhen: String?
)

data class DocumentDef(val id: String, val name: String, val required: Boolean, val fileTypes: List<String>, val maxSizeMb: Double)

data class CaseVolumeRow(val procedure: String, val minimum: String?, val target: String?, val maximum: String?)

data class ObjectiveDef(val title: String, val description: String?)

data class Program(
    val id: Long?,
    val name: String,
    val shortName: String,
    val description: String,
    val programType: String?,
    val specialty: String?,
    val duration: String?,
    val location: String?,
    val centerType: String?,
    val experienceTitle: String,
    val posters: List<String>,
    val facilities: List<String>,
    /** Raw `facilities` string list from the programme record (centre wizard uses this). */
    val rawFacilities: List<String>,
    val procedures: List<String>,
    val caseVolume: List<CaseVolumeRow>,
    val objectives: List<ObjectiveDef>,
    val categories: List<CategoryDef>,
    val personalFields: List<FieldDef>,
    val trainingFields: List<FieldDef>,
    val preferences: List<PreferenceDef>,
    val documents: List<DocumentDef>,
    val declaration: String
)

const val DEFAULT_DECLARATION =
    "I confirm that the information provided is accurate. I understand that submission does not guarantee selection, and I agree to follow the Society’s training, logbook, and assessment requirements."

private val DEFAULT_PERSONAL = listOf(
    FieldDef("full_name", "Full name", true, null),
    FieldDef("father_spouse_name", "Father / spouse name", false, null),
    FieldDef("cnic", "CNIC", true, "Digits, with or without dashes."),
    FieldDef("pmdc_number", "PMDC registration number", true, "Example: 12345-P"),
    FieldDef("qualifications", "Qualification", false, null),
    FieldDef("designation", "Current designation", false, null),
    FieldDef("place_of_work", "Current place of work", false, null),
    FieldDef("mobile", "Mobile number", true, null),
    FieldDef("whatsapp", "WhatsApp number", false, null),
    FieldDef("email", "Email", true, null),
    FieldDef("postal_address", "Postal address", false, null),
)

private val DEFAULT_TRAINING = listOf(
    FieldDef("primary_institute", "Primary institute of surgical training", true, null),
    FieldDef("program_completed", "Residency / fellowship program completed", true, null),
    FieldDef("year_completion", "Year of completion", true, null),
    FieldDef("supervisor_name", "Supervisor / head of unit", true, null),
    FieldDef("current_practice", "Current surgical practice details", true, null),
)

private val DEFAULT_PREFERENCES = listOf(
    PreferenceDef("pref-move", "Willing to relocate?", "radio", true, "Training may be offered outside your current city.", listOf("Yes", "No"), null),
    PreferenceDef("pref-city", "Preferred city / region", "dropdown", true, null, listOf("Lahore", "Karachi", "Islamabad", "Other"), null),
    PreferenceDef("pref-duration", "Preferred duration", "radio", true, null, listOf("2 weeks", "4 weeks"), null),
    PreferenceDef("pref-goals", "Special learning objectives", "textarea", false, "Tell the faculty what you most want to practise.", emptyList(), null),
)

private fun parseFields(arr: JsonArray, fallback: List<FieldDef>): List<FieldDef> {
    if (arr.isEmpty()) return fallback
    return arr.objects()
        .filter { it.bool("enabled") != false }
        .mapNotNull { o ->
            val key = o.str("key") ?: return@mapNotNull null
            FieldDef(key, o.str("label")?.fixMojibake() ?: key.humanize(), o.bool("required") == true, o.str("help")?.fixMojibake())
        }
}

private fun parseQuestions(arr: JsonArray): List<QuestionDef> = arr.objects().mapIndexedNotNull { i, o ->
    val label = o.str("label")?.fixMojibake() ?: return@mapIndexedNotNull null
    QuestionDef(o.str("id") ?: "q-$i", label, o.str("type") ?: "number", o.bool("required") == true, o.str("help"), o.arr("options").strings())
}

private fun legacyQuestions(code: String, programName: String): List<QuestionDef> {
    val lap = Regex("laparoscop|cholecyst", RegexOption.IGNORE_CASE).containsMatchIn(programName)
    fun q(label: String) = QuestionDef("q-" + label.hashCode().toUInt().toString(36), label, "number", true, null, emptyList())
    return when (code) {
        "CATEGORY_B" -> if (lap) listOf(q("Number of laparoscopic procedures assisted"), q("Number of diagnostic laparoscopies performed"))
        else listOf(q("Number of procedures assisted"))
        "CATEGORY_C" -> if (lap) listOf(q("Number of laparoscopic cholecystectomies performed"), q("Number performed independently"))
        else listOf(q("Number performed independently"))
        else -> emptyList()
    }
}

private fun displayTitle(title: String?): String =
    title?.fixMojibake()?.replace(Regex("^Section\\s+[A-Z]\\s+[—-]\\s+"), "")?.trim()?.takeIf { it.isNotEmpty() } ?: "Experience"

/** Port of `resolveProgram(program)` (config v1 → config; else legacy fields). */
fun parseProgram(p: JsonObject): Program {
    val config = p.obj("config")
    val name = p.str("name")?.fixMojibake() ?: config.str("name") ?: "Fellowship programme"
    val rawFacilities = p.arr("facilities").names()
    val posters = (p.arr("posters").strings() + listOfNotNull(p.str("poster_url"))).distinct()
    if (config != null && config.long("version") == 1L && config["categories"] is JsonArray) {
        return Program(
            id = p.long("id"),
            name = name,
            shortName = p.str("short_name") ?: config.str("short_name") ?: "",
            description = (p.str("description") ?: config.str("description") ?: "").fixMojibake(),
            programType = config.str("program_type"),
            specialty = config.str("specialty"),
            duration = config.str("duration"),
            location = config.str("location"),
            centerType = config.str("center_type"),
            experienceTitle = displayTitle(config.str("experience_title")),
            posters = posters,
            facilities = config.arr("facilities").names(),
            rawFacilities = rawFacilities,
            procedures = config.arr("procedures").names(),
            caseVolume = config.arr("case_volume").objects().mapNotNull { r ->
                val proc = r.str("procedure") ?: return@mapNotNull null
                CaseVolumeRow(proc, r.str("minimum"), r.str("target"), r.str("maximum"))
            },
            objectives = config.arr("objectives").objects().mapNotNull { o ->
                o.str("title")?.let { ObjectiveDef(it.fixMojibake(), o.str("description")?.fixMojibake()) }
            },
            categories = config.arr("categories").objects().mapNotNull { c ->
                val code = c.str("code") ?: c.str("id") ?: return@mapNotNull null
                CategoryDef(
                    code = code,
                    name = c.str("name")?.fixMojibake() ?: code.humanize(),
                    shortLabel = c.str("short_label")?.fixMojibake() ?: "",
                    description = (c.str("description") ?: c.str("short_label") ?: "").fixMojibake(),
                    minimumExperience = c.str("minimum_experience"),
                    questions = parseQuestions(c.arr("questions"))
                )
            },
            personalFields = parseFields(config.arr("personal_fields"), DEFAULT_PERSONAL),
            trainingFields = parseFields(config.arr("training_fields"), DEFAULT_TRAINING),
            preferences = config.arr("preferences").objects().mapIndexedNotNull { i, o ->
                val label = o.str("label")?.fixMojibake() ?: return@mapIndexedNotNull null
                PreferenceDef(o.str("id") ?: "pref-$i", label, o.str("type") ?: "text", o.bool("required") == true,
                    o.str("help")?.fixMojibake(), o.arr("options").strings(), o.str("show_when"))
            }.ifEmpty { if (config["preferences"] is JsonArray) emptyList() else DEFAULT_PREFERENCES },
            documents = config.arr("documents").objects().mapIndexedNotNull { i, o ->
                val n = o.str("name") ?: return@mapIndexedNotNull null
                DocumentDef(o.str("id") ?: "doc-$i", n, o.bool("required") == true,
                    o.arr("file_types").strings().map { it.uppercase() }, o.dbl("max_size_mb") ?: 5.0)
            },
            declaration = config.str("declaration")?.fixMojibake() ?: DEFAULT_DECLARATION
        )
    }
    // Legacy programme shape
    return Program(
        id = p.long("id"),
        name = name,
        shortName = p.str("short_name") ?: "",
        description = (p.str("description") ?: "").fixMojibake(),
        programType = "Mini Fellowship",
        specialty = if (name.contains("hernia", true)) "Hernia Surgery" else "General Surgery",
        duration = null,
        location = null,
        centerType = "Accredited training center",
        experienceTitle = displayTitle(p.str("experience_section_title")),
        posters = posters,
        facilities = rawFacilities,
        rawFacilities = rawFacilities,
        procedures = p.arr("procedures").strings(),
        caseVolume = emptyList(),
        objectives = emptyList(),
        categories = p.arr("categories").objects().mapNotNull { c ->
            val code = c.str("code") ?: return@mapNotNull null
            val label = c.str("label")?.fixMojibake() ?: code
            val parts = label.split("—").map { it.trim() }.filter { it.isNotEmpty() }
            val (n, rest) = if (parts.size >= 2) parts[0] to parts.drop(1).joinToString(" — ") else label to ""
            CategoryDef(code, n, rest, rest, null, legacyQuestions(code, name))
        },
        personalFields = DEFAULT_PERSONAL,
        trainingFields = DEFAULT_TRAINING,
        preferences = DEFAULT_PREFERENCES,
        documents = emptyList(),
        declaration = DEFAULT_DECLARATION
    )
}

/** `applicationSteps(program)` */
fun Program.applicationSteps(): List<Pair<String, String>> = buildList {
    if (personalFields.isNotEmpty()) add("personal" to "Personal")
    if (trainingFields.isNotEmpty()) add("training" to "Training")
    if (categories.isNotEmpty()) add("experience" to "Experience")
    add("preferences" to "Preferences") // centre choice always lives here
    if (documents.isNotEmpty()) add("documents" to "Documents")
    add("declaration" to "Declaration")
}

/** `answerKey(label)` */
fun answerKey(label: String): String =
    label.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').take(64)

/** `preferenceKey(field)` */
fun preferenceKey(p: PreferenceDef): String {
    val l = p.label.lowercase()
    return when {
        "relocate" in l -> "willing_relocate"
        "city" in l || "region" in l -> "preferred_city"
        "duration" in l -> "preferred_duration"
        "learning" in l -> "learning_objectives"
        else -> answerKey(p.label)
    }
}

// =====================================================================================
// Centres
// =====================================================================================

data class Supervisor(val name: String, val role: String?, val designation: String?, val qualifications: String?, val pmdc: String?)

data class DirectoryCenter(
    val id: Long,
    val institution: String,
    val unit: String?,
    val masterTrainer: String?,
    val supervisors: List<Supervisor>,
    val email: String?,
    val address: String?,
    val city: String?,
    val facilities: List<String>,
    val capacity: Int?,
    val activeFellows: Int?,
    val availability: String,
    val programId: Long?,
    val programName: String?
) {
    val isBooked get() = availability == "BOOKED" || availability == "FULL"
    val seatsLabel: String
        get() = if (capacity != null) "${activeFellows ?: 0}/$capacity" else "—"
    /** Lowercased haystack for search (precomputed once). */
    val searchText: String = buildString {
        append(institution); append(' '); append(address.orEmpty()); append(' '); append(masterTrainer.orEmpty())
        supervisors.forEach { append(' '); append(it.name) }
        facilities.forEach { append(' '); append(it) }
    }.lowercase()
}

fun parseDirectoryCenter(o: JsonObject): DirectoryCenter? {
    val id = o.long("id") ?: return null
    val program = o.obj("program")
    return DirectoryCenter(
        id = id,
        institution = (o.str("institution_name") ?: o.str("name") ?: "Training centre").fixMojibake(),
        unit = o.str("unit_department"),
        masterTrainer = o.str("master_trainer_name"),
        supervisors = o.arr("supervisors").objects().mapNotNull { s ->
            s.str("name")?.let { Supervisor(it, s.str("role"), s.str("designation"), s.str("qualifications"), s.str("pmdc_number")) }
        },
        email = o.str("email"),
        address = o.str("hospital_address"),
        city = o.str("city"),
        facilities = o.arr("facilities").names(),
        capacity = o.int("capacity"),
        activeFellows = o.int("active_fellows"),
        availability = o.str("availability_status")?.uppercase() ?: "OPEN",
        programId = program.long("id") ?: o.long("program_id"),
        programName = program.str("short_name") ?: program.str("name")
    )
}

data class ActiveCenter(val id: Long, val name: String, val city: String?, val label: String)

fun parseActiveCenter(o: JsonObject): ActiveCenter? {
    val id = o.long("id") ?: return null
    val name = o.str("institution_name") ?: return null
    return ActiveCenter(id, name, o.str("city"), o.str("label") ?: (name + (o.str("city")?.let { " ($it)" } ?: "")))
}

// =====================================================================================
// Clinical (logbook / attendance / assessments / research)
// =====================================================================================

val CASE_ROLES = listOf(
    "OBSERVER" to "Observer",
    "CAMERA_HANDLER" to "Camera Handler",
    "PORT_PLACEMENT" to "Port Placement",
    "ASSISTANT" to "Assistant",
    "DISSECTION_SUPERVISED" to "Dissection (Supervised)",
    "PRIMARY_SUPERVISED" to "Primary (Supervised)",
)

val ATTENDANCE_TYPES = listOf(
    "OPERATING_THEATRE" to "Operating Theatre",
    "ACADEMIC_SESSION" to "Academic Session",
    "MORBIDITY_MORTALITY" to "M&M Meeting",
    "JOURNAL_CLUB" to "Journal Club",
    "SKILLS_WORKSHOP" to "Skills Workshop",
    "CASE_DISCUSSION" to "Case Discussion",
    "OTHER" to "Other",
)

val PUBLICATION_TYPES = listOf(
    "ARTICLE" to "Article / Journal Paper",
    "POSTER" to "Poster Presentation",
    "ABSTRACT" to "Conference Abstract",
    "CASE_REPORT" to "Case Report",
    "OTHER" to "Other Publication",
)

data class CaseItem(
    val id: Long, val procedure: String, val date: String?, val role: String?, val status: String,
    val diagnosis: String?, val patientRef: String?, val feedback: String?, val fellowName: String?
)

fun parseCase(o: JsonObject): CaseItem? = o.long("id")?.let {
    CaseItem(
        id = it,
        procedure = o.str("procedure_name") ?: o.str("procedure") ?: "Procedure",
        date = o.str("procedure_date") ?: o.str("date"),
        role = o.str("role_in_case"),
        status = o.str("status")?.uppercase() ?: "PENDING",
        diagnosis = o.str("diagnosis"),
        patientRef = o.str("patient_ref"),
        feedback = o.str("trainer_feedback"),
        fellowName = o.obj("enrollment").obj("fellow_application").str("full_name")
    )
}

data class AttendanceItem(val id: Long, val type: String, val hours: String?, val date: String?, val status: String, val fellowName: String?)

fun parseAttendance(o: JsonObject): AttendanceItem? = o.long("id")?.let {
    AttendanceItem(
        it, o.str("session_type") ?: "SESSION", o.str("hours"), o.str("session_date"),
        o.str("status")?.uppercase() ?: "PENDING", o.obj("enrollment").obj("fellow_application").str("full_name")
    )
}

data class AssessmentItem(val id: Long, val title: String, val type: String?, val score: Double?, val date: String?, val strengths: String?)

fun parseAssessment(o: JsonObject): AssessmentItem? = o.long("id")?.let {
    AssessmentItem(it, o.str("title") ?: "Assessment", o.str("assessment_type"), o.dbl("overall_score"), o.str("assessment_date"), o.str("strengths"))
}

data class ResearchItem(
    val id: Long, val title: String, val authors: String?, val type: String?, val venue: String?, val year: String?,
    val status: String, val abstract: String?, val createdAt: String?, val feedback: String?, val fileUrl: String?
)

fun parseResearch(o: JsonObject): ResearchItem? = o.long("id")?.let {
    ResearchItem(
        it, o.str("title") ?: "Untitled", o.str("authors"), o.str("publication_type"), o.str("journal_or_event"),
        o.str("publication_year"), o.str("status")?.uppercase() ?: "PENDING", o.str("abstract_text"),
        o.str("created_at"), o.str("reviewer_feedback"), o.str("file_url")
    )
}

data class TrainerFellow(val enrollmentId: Long, val name: String, val pmdc: String?, val category: String?)

fun parseTrainerFellow(o: JsonObject): TrainerFellow? = o.long("id")?.let {
    val app = o.obj("fellow_application")
    TrainerFellow(it, app.str("full_name") ?: "Fellow #$it", app.str("pmdc_number"), o.str("category_assigned")?.replace("CATEGORY_", "Category "))
}

data class ClinicalStats(
    val casesAccepted: Int, val casesPending: Int, val casesPendingReview: Int, val verifiedHours: Double, val fellows: Int
)

fun parseClinicalStats(o: JsonObject): ClinicalStats {
    val cases = o.obj("cases"); val att = o.obj("attendance")
    return ClinicalStats(
        cases.int("accepted") ?: 0, cases.int("pending") ?: 0, cases.int("pending_review") ?: 0,
        att.dbl("verified_hours") ?: 0.0, o.int("fellows") ?: 0
    )
}

data class CertificateInfo(val serial: String?, val issuedAt: String?, val programName: String?, val fileUrl: String?)

fun parseCertificate(o: JsonObject?): CertificateInfo? {
    if (o == null) return null
    val serial = o.str("serial_number")
    if (serial == null && o.str("file_url") == null) return null
    return CertificateInfo(serial, o.str("issued_at"), o.str("program_name"), o.str("file_url"))
}

data class CompletionProgress(
    val casesApproved: Int, val minCases: Int, val casesMet: Boolean,
    val hours: Double, val minHours: Double, val hoursMet: Boolean,
    val evaluationApproved: Boolean, val feedback1: Boolean, val feedback2: Boolean
)

fun parseProgress(o: JsonObject): CompletionProgress? {
    val r = o.obj("requirements") ?: return null
    return CompletionProgress(
        o.int("cases_approved") ?: 0, r.int("min_cases") ?: 0, r.bool("cases_met") == true,
        o.dbl("attendance_hours") ?: 0.0, r.dbl("min_hours") ?: 0.0, r.bool("hours_met") == true,
        r.bool("evaluation_approved") == true, r.bool("feedback_part1_met") == true, r.bool("feedback_part2_met") == true
    )
}

/** Unwraps `{data:{…}}` envelopes when present. */
internal fun JsonElement.unwrap(): JsonObject {
    val o = this as? JsonObject ?: return JsonObject(emptyMap())
    return (o["data"] as? JsonObject) ?: o
}

/** In-memory hand-off for deep links between fellowship screens (routes have no args). */
object FellowshipNavArgs {
    var programId: Long? = null
    var centerId: Long? = null
}
