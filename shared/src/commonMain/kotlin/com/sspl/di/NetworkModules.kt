package com.sspl.di


import org.koin.dsl.module

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */

internal val networkModules = module {
//    singleOf(::provideLoggingInterceptor)
//    singleOf(::provideCacheInterceptor)
//    singleOf(::provideHttpClient)
//    singleOf(::provideRetrofit)
//    singleOf(::provideService)
}

private const val BASE_URL = "https://ps1.redtelo.net"
const val PATH_TO_APPEND = "/api"
var cacheSize: Long = 5 * 1024 * 1024 // 5MB

//private fun provideLoggingInterceptor() = HttpLoggingInterceptor().apply {
//    this.level = HttpLoggingInterceptor.Level.BODY
//}
//
//private fun provideCacheInterceptor() = Interceptor { chain ->
//    val response: Response = chain.proceed(chain.request())
//    val cacheControl = CacheControl.Builder()
//        .maxAge(1, TimeUnit.DAYS)
//        .build()
//    response.newBuilder()
//        .header("Cache-Control", cacheControl.toString())
//        .build()
//}
//
//private fun provideHttpClient(
//    context: Context,
//    loggingInterceptor: HttpLoggingInterceptor,
//    cacheInterceptor: Interceptor
//): OkHttpClient {
//
//    return OkHttpClient
//        .Builder()
//        .cache(Cache(context.cacheDir, cacheSize))
//        .readTimeout(60, TimeUnit.SECONDS)
//        .connectTimeout(60, TimeUnit.SECONDS)
//        .addInterceptor(loggingInterceptor)
//        .addNetworkInterceptor(cacheInterceptor)
//        .build()
//}
//
//
//private fun provideRetrofit(
//    okHttpClient: OkHttpClient
//): Retrofit {
//    val networkJson = Json {
//        ignoreUnknownKeys = true
//        explicitNulls = false
//    }
//    val converter = networkJson.asConverterFactory("application/json; charset=utf-8".toMediaType())
//    return Retrofit.Builder()
//        .baseUrl(BASE_URL)
//        .client(okHttpClient)
//        .addConverterFactory(converter)
//        .build()
//}
//
//private fun provideService(retrofit: Retrofit): ApiService =
//    retrofit.create(ApiService::class.java)