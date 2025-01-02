package com.sspl.di

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

internal val storageModules = module {
//    single { LocalStorage(get()) }
//    single { UserSession(get(),get()) }
}