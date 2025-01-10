package com.sspl.di

import com.sspl.core.repositories.PostRepositoryImpl
import com.sspl.core.repositories.PostsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */

internal val repositoryModules = module {
//    single { AuthenticationRepository(get(), get()) }
//    single { StreamRepository(get()) }
    singleOf(::PostRepositoryImpl).bind<PostsRepository>()
}
