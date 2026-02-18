package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.ui.landing.LandingScreen
import com.example.weatherapp.ui.main.MainScreen
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherAppTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "landing"
                ) {

                    composable("landing") {
                        LandingScreen(navController)
                    }

                    composable("main") {
                        MainScreen()
                    }
                }
            }
        }
    }
}
