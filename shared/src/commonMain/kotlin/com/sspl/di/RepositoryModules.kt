package com.sspl.di

import com.sspl.core.repositories.AuthRepository
import com.sspl.core.repositories.ConferenceRepository
import com.sspl.core.repositories.PostRepositoryImpl
import com.sspl.core.repositories.PostsRepository
import com.sspl.core.repositories.UserRepository
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
    singleOf(::AuthRepository)
    singleOf(::UserRepository)
    singleOf(::ConferenceRepository)
    singleOf(::PostRepositoryImpl).bind<PostsRepository>()
}
