package com.sspl.di

import com.sspl.core.models.usecases.GetPostsUseCase
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

}