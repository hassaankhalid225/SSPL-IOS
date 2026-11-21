package com.sspl.core.usecases

import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.User
import com.sspl.core.repositories.UserRepository
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Every field the web "My profile" form (ProfilePage.jsx) saves, plus the app's phone / org no. / title. */
data class ProfileUpdateForm(
    val firstName: String,
    val lastName: String,
    val gender: String,
    val phone: String?,
    val pmdcNumber: String,
    val hospital: String,
    val address: String,
    val city: String,
    val country: String,
    val institute: String,
    val title: String,
    val orgNumber: String,
    val mbbsInstitute: String,
    val mbbsPassingYear: String,
    val fcpsInstitute: String,
    val fcpsPassingYear: String,
    val msInstitute: String,
    val msPassingYear: String,
    val whatsapp: String
)

sealed interface ProfileUpdateState {
    data object Loading : ProfileUpdateState
    data class Success(val user: User?) : ProfileUpdateState
    /** [fieldErrors] are keyed by form field (web `mapProfileFieldErrors`). */
    data class Failure(val message: String, val fieldErrors: Map<String, String>) : ProfileUpdateState
}

class PostUserDetailsUseCase(
    private val userRepository: UserRepository
) {

    /**
     * Sign-up step 2 (professional details). Only filled fields are sent so blanks never wipe
     * existing values.
     */
    operator fun invoke(
        pmdcNo: String? = null,
        instituteName: String? = null,
        country: String? = null,
        designation: String? = null,
        registrationNo: String? = null,
        city: String? = null,
    ) = flow<ApiStates<User>> {
        emit(ApiStates.Loading)
        try {
            val body = buildJsonObject {
                put("profile", buildJsonObject {
                    putIfNotBlank("pmdc_number", pmdcNo)
                    putIfNotBlank("institute", instituteName)
                    putIfNotBlank("city", city)
                    putIfNotBlank("country", country)
                    putIfNotBlank("title", designation)
                    putIfNotBlank("org_number", registrationNo)
                })
            }
            val response = userRepository.updateUserProfile(body)
            if (response.status.isSuccess()) {
                emit(ApiStates.Success(runCatching { response.body<User>() }.getOrNull()))
            } else {
                emit(ApiStates.Failure(error = response.parseApiError().toApiError()))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(ApiStates.Failure(ApiError(message = "Couldn't save your details. Check your connection and try again.")))
        }
    }

    /**
     * PUT /profiles/me with the same body as web ProfilePage.jsx `handleSave`:
     * first_name, last_name, gender (omitted when blank), profile{ pmdc_number, hospital, address,
     * city, country, institute, mbbs_*, fcps_* / ms_* (omitted when blank), contact{whatsapp}
     * (omitted when blank) }. The app additionally keeps account.phone, profile.title and
     * profile.org_number which it already edited before.
     */
    fun updateProfile(form: ProfileUpdateForm): Flow<ProfileUpdateState> = flow {
        emit(ProfileUpdateState.Loading)
        try {
            val body = buildJsonObject {
                put("first_name", form.firstName.trim())
                put("last_name", form.lastName.trim())
                putIfNotBlank("gender", form.gender)
                if (!form.phone.isNullOrBlank()) {
                    put("account", buildJsonObject { put("phone", form.phone.trim()) })
                }
                put("profile", buildJsonObject {
                    put("pmdc_number", form.pmdcNumber.trim())
                    put("hospital", form.hospital.trim())
                    put("address", form.address.trim())
                    put("city", form.city.trim())
                    put("country", form.country.trim())
                    put("institute", form.institute.trim())
                    put("title", form.title.trim())
                    put("org_number", form.orgNumber.trim())
                    put("mbbs_institute", form.mbbsInstitute.trim())
                    put("mbbs_passing_year", form.mbbsPassingYear.trim())
                    putIfNotBlank("fcps_institute", form.fcpsInstitute)
                    putIfNotBlank("fcps_passing_year", form.fcpsPassingYear)
                    putIfNotBlank("ms_institute", form.msInstitute)
                    putIfNotBlank("ms_passing_year", form.msPassingYear)
                    if (form.whatsapp.isNotBlank()) {
                        put("contact", buildJsonObject { put("whatsapp", form.whatsapp.trim()) })
                    }
                })
            }
            val response = userRepository.updateUserProfile(body)
            if (response.status.isSuccess()) {
                emit(ProfileUpdateState.Success(runCatching { response.body<User>() }.getOrNull()))
            } else {
                val parsed = response.parseApiError()
                val fields = mapProfileFieldErrors(parsed.fieldErrors)
                // Web applyFormApiError: when field errors exist the form-level message is cleared.
                emit(ProfileUpdateState.Failure(if (fields.isNotEmpty()) "" else parsed.message, fields))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            emit(ProfileUpdateState.Failure("Couldn't save your profile. Check your connection and try again.", emptyMap()))
        }
    }
}

private fun JsonObjectBuilder.putIfNotBlank(key: String, value: String?) {
    if (!value.isNullOrBlank()) put(key, value.trim())
}
