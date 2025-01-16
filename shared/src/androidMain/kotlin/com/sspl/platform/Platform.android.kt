package com.sspl.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.sspl.core.getBaseClient
import com.sspl.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getClient(): HttpClient {
    return getBaseClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
//            addInterceptor(interceptor)
//            addNetworkInterceptor(interceptor)
//            preconfigured = okHttpClientInstance
        }
    }
}

actual fun formatDateTime(dateTime: String, inFormat: String, outFormat: String): String {
    return try {
        println(">>>dateTime=$dateTime")
        val inputFormat = SimpleDateFormat(inFormat, Locale.getDefault())
        val date = inputFormat.parse(dateTime)

        val outputFormat = SimpleDateFormat(outFormat, Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

actual fun getStorage(): Storage {
    TODO("Not yet implemented")
}

actual fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()



