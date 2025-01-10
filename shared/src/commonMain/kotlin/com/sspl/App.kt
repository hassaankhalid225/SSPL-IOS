package com.sspl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import com.sspl.theme.MyApplicationTheme
import org.koin.compose.KoinContext

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 01/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun App() {
    KoinContext {
        setSingletonImageLoaderFactory { context ->
            getAsyncImageLoader(context)
        }
        MyApplicationTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
//                Surface(modifier = Modifier.padding(it)) {
                    MainScreen(it)
//                }
            }
        }
    }

}
fun getAsyncImageLoader(context: PlatformContext)=
    ImageLoader.Builder(context).build()