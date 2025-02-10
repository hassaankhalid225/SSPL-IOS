package com.sspl.di

import com.sspl.core.usecases.CommentOnPostUseCase
import com.sspl.core.usecases.DeleteAccountUseCase
import com.sspl.core.usecases.DislikePostUseCase
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.GetConferencesDetailsUseCase
import com.sspl.core.usecases.GetConferencesUseCase
import com.sspl.core.usecases.GetPostDetailsUseCase
import com.sspl.core.usecases.GetPostsUseCase
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.core.usecases.LikePostUseCase
import com.sspl.core.usecases.LoginUserUseCase
import com.sspl.core.usecases.MarkAttendanceViaImageUseCase
import com.sspl.core.usecases.MarkAttendanceViaLocationUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.PostUserDetailsUseCase
import com.sspl.core.usecases.SignUpUserUseCase
import com.sspl.core.usecases.UpdateUserBasicInfoUseCase
import com.sspl.core.usecases.ValidateUserCityUseCase
import com.sspl.core.usecases.ValidateUserCountryUseCase
import com.sspl.core.usecases.ValidateUserDesignationUseCase
import com.sspl.core.usecases.ValidateUserNameUseCase
import com.sspl.core.usecases.ValidateUserInstitutionUseCase
import com.sspl.core.usecases.ValidateUserPmdcNoUseCase
import com.sspl.core.usecases.ValidateUserRegistrationNumberUseCase
import org.koin.dsl.module

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */

internal val useCaseModules = module {
//    single { Authenticate(get(), get()) }
//    single { CheckIsAuthenticated(get()) }
//    single { GetLiveStream(get(), get()) }
//    single { GetLiveStreamCategories(get(), get()) }
    single { EmailValidationUseCase() }
    single { PasswordValidationUseCase() }
    single { PhoneNumberValidationUseCase() }
    single { PhoneNumberValidationUseCase() }
    single { ValidateUserNameUseCase() }
    single { ValidateUserCityUseCase() }
    single { ValidateUserCountryUseCase() }
    single { ValidateUserInstitutionUseCase() }
    single { ValidateUserDesignationUseCase() }
    single { ValidateUserPmdcNoUseCase() }
    single { ValidateUserRegistrationNumberUseCase() }

    factory { GetPostsUseCase(get()) }
    factory { LikePostUseCase() }
    factory { DislikePostUseCase() }
    factory { CommentOnPostUseCase() }
    factory { SignUpUserUseCase(get(), get()) }
    factory { LoginUserUseCase(get(), get()) }
    factory { GetConferencesUseCase(get()) }
    factory { GetConferencesDetailsUseCase(get()) }
    factory { PostUserDetailsUseCase(get()) }
    factory { GetUserDetailsUseCase(get()) }
    factory { UpdateUserBasicInfoUseCase(get()) }
    factory { GetPostDetailsUseCase(get()) }
    factory { MarkAttendanceViaImageUseCase(get()) }
    factory { MarkAttendanceViaLocationUseCase(get()) }
    factory { DeleteAccountUseCase(get(), get()) }
}
