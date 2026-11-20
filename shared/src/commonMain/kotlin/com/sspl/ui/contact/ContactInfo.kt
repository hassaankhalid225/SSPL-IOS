package com.sspl.ui.contact

import com.sspl.core.models.SiteSettingsResponse

/**
 * Official contact details. Values come from GET /settings `contacts`; any missing field falls
 * back to the same hard-coded values the web uses (`data/homepage/site.ts`, Contact.jsx).
 */
data class ContactInfo(
    val address: String = FALLBACK_ADDRESS,
    val phone: String = FALLBACK_PHONE,
    val email: String = FALLBACK_EMAIL,
    val location: String = "Lahore, Pakistan",
    val mapUrl: String = MAP_URL,
    /** True when the values are the built-in fallback (settings call failed). */
    val isFallback: Boolean = false
) {
    val telUri: String get() = "tel:" + phone.filter { it.isDigit() || it == '+' }
    val mailUri: String get() = "mailto:$email"

    /** Web labels the phone as "Phone and WhatsApp". */
    val whatsappUri: String get() = "https://wa.me/" + phone.filter { it.isDigit() }

    companion object {
        const val FALLBACK_ADDRESS = "Office # 8, 2nd Floor, PMA Trade Center, 66 Ferozepur Road, Lahore"
        const val FALLBACK_PHONE = "+92 333 425 4002"
        const val FALLBACK_EMAIL = "societyofsurgeons1@gmail.com"
        const val MAP_URL = "https://maps.app.goo.gl/maZaaQaahah4R7Vi6?g_st=ic"

        fun from(settings: SiteSettingsResponse?): ContactInfo {
            val c = settings?.contacts ?: return ContactInfo(isFallback = true)
            // The settings `mapUrl` is usually an <iframe> embed (web renders it as HTML);
            // only a plain link is usable here, otherwise keep the official Maps link.
            val map = c.mapUrl?.trim()?.takeIf { it.startsWith("http") } ?: MAP_URL
            return ContactInfo(
                address = c.address?.takeIf { it.isNotBlank() } ?: FALLBACK_ADDRESS,
                phone = c.phone?.takeIf { it.isNotBlank() } ?: FALLBACK_PHONE,
                email = c.email?.takeIf { it.isNotBlank() } ?: FALLBACK_EMAIL,
                mapUrl = map
            )
        }
    }
}

data class SocialLink(val label: String, val url: String)

/** Official social profiles (web `siteSocialLinks`). */
val SOCIAL_LINKS = listOf(
    SocialLink("Facebook", "https://www.facebook.com/SocietyOfSurgeonLahoreChapter/"),
    SocialLink("Instagram", "https://www.instagram.com/society_of_surgeons_pakistan/"),
    SocialLink("YouTube", "https://youtube.com/@societyofsurgeonsofpakistan?si=p5DvcJRSn5LIWrJj")
)
