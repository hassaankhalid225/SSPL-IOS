package com.sspl.di

import com.sspl.core.repositories.AuthRepository
import com.sspl.core.repositories.BannerRepository
import com.sspl.core.repositories.ConferenceRepository
import com.sspl.core.repositories.NotificationRepository
import com.sspl.core.repositories.ScenarioRepository
import com.sspl.core.repositories.UserRepository
import com.sspl.core.repositories.DeviceTokenRepository
import com.sspl.core.push.DeviceTokenManager
import com.sspl.core.push.PushNotificationService
import com.sspl.core.repositories.PaymentRepository
import com.sspl.core.repositories.PublicRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */

internal val repositoryModules = module {
    factory { AuthRepository(get()) }
    factoryOf(::UserRepository)
    factoryOf(::ConferenceRepository)
    factoryOf(::ScenarioRepository)
    factoryOf(::BannerRepository)
    single { NotificationRepository(get()) } // Singleton for notification state with Storage persistence

    // Push Notifications
    factoryOf(::DeviceTokenRepository)
    factoryOf(::DeviceTokenManager)
    singleOf(::PushNotificationService)
    
    // Payment
    factoryOf(::PaymentRepository)
    
    // Public / CMS
    factoryOf(::PublicRepository)
}
