package com.sspl.ui.council

/**
 * Cabinet & executive roster 2026–2028 — same static data the web uses
 * (`sspl-frotend/src/data/homepage/cabinet2026.ts`). Photos are the web's public images.
 */
data class CouncilMember(
    val name: String,
    val role: String,
    val imageUrl: String? = null
)

const val CABINET_TERM_LABEL = "2026–2028"

private const val WEB_IMAGES = "https://ssplahore.com/images/"

private fun img(fileName: String): String = WEB_IMAGES + fileName.replace(" ", "%20")

val CABINET_AND_EXECUTIVES: List<CouncilMember> = listOf(
    CouncilMember("Prof. Farooq Ahmad Rana", "President", img("President Prof. Farooq Ahmad Rana.jpeg")),
    CouncilMember("Dr. Awais Amjad Malik", "Vice President", img("Vice President Dr. Awais Amjad Malik.jpg")),
    CouncilMember("Dr. Ahmad Naeem Akhtar", "General Secretary", img("General Secretary Dr. Ahmad Naeem Akhtar.jpeg")),
    CouncilMember("Dr. Muhammad Aamir Jameel", "Joint Secretary", img("Joint Secretary Dr. Muhammad Aamir Jameel.jpeg")),
    CouncilMember("Dr. Hafiz Mahmood Ahmed", "Finance Secretary", img("Finance Secretary Dr. Hafiz Mahmood Ahmed.jpg")),
    CouncilMember("Dr. Zubair Ahmed Khan", "Publication Secretary", img("Publication Secretary Dr. Zubair Ahmed Khan.jpg")),
    CouncilMember("Prof. Muhammad Imran Khokhar", "Executive Member", img("1. Prof. Muhammad Imran Khokhar.png")),
    CouncilMember("Prof. Abul Fazal Ali Khan", "Executive Member", img("2. Prof. Abul Fazal Ali Khan.jpeg")),
    CouncilMember("Prof. Faisal Masood", "Executive Member", img("3. Prof. Faisal Masood.jpeg")),
    CouncilMember("Prof. Syed Asghar Naqi", "Executive Member", img("4. Prof. Syed Asghar Naqi.jpeg")),
    CouncilMember("Prof. Moeed Iqbal Qureshi", "Executive Member", img("5. Prof. Moeed Iqbal Qureshi.jpeg")),
    CouncilMember("Prof. Muhammad Imran Anwar", "Executive Member", img("6. Prof. Muhammad Imran Anwar.jpeg")),
    CouncilMember("Dr. Muhammad Arshad Cheema", "Executive Member", img("7.  Dr. Muhammad Arshad Cheema.jpeg")),
    CouncilMember("Dr. Muhammad Umar Warraich", "Executive Member", img("8. Dr. Muhammad Umar Warraich.jpg")),
    CouncilMember("Prof. Shahzad Alam Shah", "Executive Member", img("9. Prof. Shahzad Alam Shah.jpeg")),
    CouncilMember("Prof. Ch. Mohammad Kamran", "Executive Member", img("10. Prof. Ch. Mohammad Kamran.jpeg")),
    CouncilMember("Prof. Tayyab Abbas", "Executive Member", img("11. Prof. Tayyab Abbas.jpeg")),
    CouncilMember("Prof. Muhammad Zeeshan Sarwar", "Executive Member", img("12. Prof. Muhammad Zeeshan Sarwar.jpeg")),
    CouncilMember("Prof. Syed Shahzad Hussain Shah", "Executive Member", img("13. Prof. Syed Shahzad Hussain Shah.jpeg")),
    CouncilMember("Prof. Yar Muhammad", "Executive Member", img("14. Prof. Yar Muhammad.jpeg")),
    CouncilMember("Prof. Abdul Hameed", "Executive Member", img("15. Prof. Abdul Hameed.jpeg")),
    CouncilMember("Prof. Muhammad Waris Farooka", "Ex-Officio Member", img("Ex-Officio Members Prof. Muhammad Waris Farooka.jpeg")),
    CouncilMember("Dr. Muhammad Shahid Farooq", "Ex-Officio Member", img("Ex-Officio Members Dr. Muhammad Shahid Farooq.jpeg"))
)

/** Society Secretary — shown on Contact only (not part of the cabinet roster, same as web). */
val SOCIETY_SECRETARY = CouncilMember("Mr. Ahmad Qasim", "Society Secretary", img("Mr. Ahmad Qasim.jpeg"))

/** Office bearers in display order (web: President, Vice President, General Secretary …). */
val OFFICE_BEARER_ROLES = listOf(
    "President", "Vice President", "General Secretary", "Joint Secretary", "Finance Secretary", "Publication Secretary"
)

/** Short role descriptions shown in the member sheet (society roles, not personal bios). */
fun roleDescription(role: String): String = when (role) {
    "President" -> "Leads the Society and chairs the cabinet and executive council for the $CABINET_TERM_LABEL term."
    "Vice President" -> "Supports the President and represents the Society in academic and professional forums."
    "General Secretary" -> "Runs the Society's day-to-day affairs, meetings, correspondence and records."
    "Joint Secretary" -> "Assists the General Secretary with meetings, events and member communication."
    "Finance Secretary" -> "Oversees the Society's accounts, membership fees and event finances."
    "Publication Secretary" -> "Coordinates the Society's publications, notices and academic material."
    "Ex-Officio Member" -> "Serves on the council by virtue of a previous office, bringing continuity and experience."
    else -> "Member of the executive council, guiding the Society's academic and clinical programme."
}
