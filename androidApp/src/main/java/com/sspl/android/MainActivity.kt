package com.sspl.android

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sspl.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.light(
//                Color.WHITE, Color.WHITE
//            ),
//            navigationBarStyle = SystemBarStyle.light(
//                Color.WHITE, Color.WHITE
//            )
//        )
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
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
