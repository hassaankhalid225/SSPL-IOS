package com.sspl.di

import com.sspl.ui.conferencescreen.ConferenceDetailViewModel
import com.sspl.ui.conferencescreen.ConferenceViewModel
import com.sspl.ui.kyc.SignInViewModel
import com.sspl.ui.kyc.SignUpViewModel
import com.sspl.ui.otp.OtpViewModel
import com.sspl.ui.postcreation.PostCreationViewModel
import com.sspl.ui.posts.PostViewModel
import com.sspl.ui.userdetails.ProfileViewModel
import com.sspl.ui.userdetails.UserDetailViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */

/**
 * factory -> gives new instance each time you ask for this object type.
 *
 * single -> gives a single instance across the entire lifetime of the app.
 * (singleton used especially for Database or Network Managers)
 *
 * get() -> this will resolve the dependency, koin will find out the matching dependency
 */

internal val viewModelModules = module {
    singleOf(::ConferenceViewModel)
    viewModel { ProfileViewModel(get()) }
    viewModelOf(::ConferenceDetailViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf( ::PostViewModel)
    viewModelOf(::OtpViewModel)
    viewModelOf(::UserDetailViewModel)
    viewModelOf(::PostCreationViewModel)


//    viewModelOf(::PlayerScreenViewModel)
//    viewModelOf(::SearchScreenViewModel)
}