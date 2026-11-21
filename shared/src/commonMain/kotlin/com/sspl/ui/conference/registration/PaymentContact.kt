package com.sspl.ui.conference.registration

/**
 * Exact port of the web's `utils/phoneUtils.js` (parsePhoneNumber / isValidPhoneNumber / toPkMsisdn)
 * and `constants/countryCodes.js`, used before POST /swich/initiate-payment.
 */
internal object PaymentContact {

    private data class Country(val code: String, val iso: String)

    private val COUNTRY_CODES = listOf(
        Country("+92", "PK"), Country("+1", "US"), Country("+44", "GB"), Country("+91", "IN"),
        Country("+971", "AE"), Country("+966", "SA"), Country("+974", "QA"), Country("+965", "KW"),
        Country("+968", "OM"), Country("+973", "BH"), Country("+20", "EG"), Country("+90", "TR"),
        Country("+86", "CN"), Country("+81", "JP"), Country("+82", "KR"), Country("+49", "DE"),
        Country("+33", "FR"), Country("+39", "IT"), Country("+34", "ES"), Country("+61", "AU"),
        Country("+64", "NZ"), Country("+55", "BR"), Country("+27", "ZA"), Country("+234", "NG"),
        Country("+254", "KE"), Country("+880", "BD"), Country("+94", "LK"), Country("+977", "NP"),
        Country("+98", "IR"), Country("+964", "IQ")
    )
    private val DEFAULT_COUNTRY = COUNTRY_CODES.first()
    private val SORTED_CODES = COUNTRY_CODES.sortedByDescending { it.code.length }
    private val PK_LOCAL = Regex("^03\\d{9}$")
    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    private fun parse(value: String?): Pair<Country, String> {
        if (value.isNullOrEmpty()) return DEFAULT_COUNTRY to ""
        val compact = value.trim().filterNot { it.isWhitespace() }
        if (compact.startsWith("+")) {
            SORTED_CODES.firstOrNull { compact.startsWith(it.code) }?.let { c ->
                return c to compact.drop(c.code.length).filter { it.isDigit() }
            }
        }
        val digits = compact.filter { it.isDigit() }
        val pk = COUNTRY_CODES.first { it.iso == "PK" }
        return when {
            digits.startsWith("92") && digits.length >= 12 -> pk to digits.drop(2).trimStart('0')
            PK_LOCAL.matches(digits) -> pk to digits.drop(1)
            digits.startsWith("0") -> DEFAULT_COUNTRY to digits.drop(1)
            else -> DEFAULT_COUNTRY to digits
        }
    }

    /** Web `isValidPhoneNumber(value, 10)`. */
    fun isValidPhone(value: String?, minLocalDigits: Int = 10): Boolean = parse(value).second.length >= minLocalDigits

    /** Web `toPkMsisdn`: PK → `03xxxxxxxxx`, otherwise country code + local without `+`. */
    fun toPkMsisdn(value: String?): String {
        val (country, local) = parse(value)
        if (country.iso == "PK" && local.isNotEmpty()) return "0$local"
        return if (local.isEmpty()) "" else (country.code + local).replace("+", "")
    }

    /** Web `/^[^\s@]+@[^\s@]+\.[^\s@]+$/`. */
    fun isValidEmail(value: String?): Boolean = !value.isNullOrBlank() && EMAIL.matches(value.trim())
}
