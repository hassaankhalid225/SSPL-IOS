import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            //

            //Navigation
            implementation(libs.navigation.compose)
            //Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
            implementation(libs.koin.test)

            //Retrofit + OkHttp
//            implementation(libs.retrofit)
//            implementation(project.dependencies.platform(libs.okhttp.bom))
//            implementation(libs.okhttp)
//            implementation(libs.logging.interceptor)
            implementation(libs.kotlinx.serialization.json)
//            implementation(libs.retrofit2.kotlinx.serialization.converter)
            //Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.compose.core)


//            implementation(compose.components.resources)
//            implementation(compose.components.uiToolingPreview)
//
//            implementation(libs.androidx.lifecycle.viewmodel)
//            implementation(libs.androidx.lifecycle.runtime.compose)
//            implementation(libs.navigation.compose)
//            implementation(libs.libphonenumber)


            //implementation(libs.compose.ui.tooling)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.sspl"
    compileSdk = 35
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
}
