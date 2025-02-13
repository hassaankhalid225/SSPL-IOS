package com.sspl.android

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.sspl.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                getColor(R.color.primary), getColor(R.color.primary)
//            ),
//            navigationBarStyle = SystemBarStyle.light(
//                getColor(R.color.primary), getColor(R.color.primary)
//            )
//        )
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                //true // mean will continue to show splash screen
                false
            }
        }
        setContent {
            App()
        }
    }
}

//@Composable
//fun GreetingView(text: String) {
//    Text(text = text)
//}
//
//@Preview
//@Composable
//fun DefaultPreview() {
//    MyApplicationTheme {
//        GreetingView("Hello, Android!")
//    }
//}
