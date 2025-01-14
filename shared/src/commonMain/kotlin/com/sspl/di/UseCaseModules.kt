package com.sspl.di

import com.sspl.core.usecases.CommentOnPostUseCase
import com.sspl.core.usecases.DislikePostUseCase
import com.sspl.core.usecases.EmailValidationUseCase
import com.sspl.core.usecases.GetPostsUseCase
import com.sspl.core.usecases.LikePostUseCase
import com.sspl.core.usecases.PasswordValidationUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.ValidateUserFirstNameUseCase
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
    single { GetPostsUseCase(get()) }
    single { LikePostUseCase() }
    single { DislikePostUseCase() }
    single { CommentOnPostUseCase() }
    single { EmailValidationUseCase() }
    single { PasswordValidationUseCase() }
    single { PhoneNumberValidationUseCase() }
    single { PhoneNumberValidationUseCase() }
    single { ValidateUserFirstNameUseCase() }

}
