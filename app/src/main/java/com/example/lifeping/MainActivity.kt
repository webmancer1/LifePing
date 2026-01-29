package com.example.lifeping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.lifeping.ui.auth.LoginScreen
import com.example.lifeping.ui.home.HomeScreen
import com.example.lifeping.ui.profile.ProfileScreen
import com.example.lifeping.ui.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.lifeping.ui.theme.LifePingTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: com.example.lifeping.ui.theme.ThemeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            
            android.util.Log.d("ThemeDebug", "MainActivity: Recomposing with isDarkTheme = $isDarkTheme")

            LifePingTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val startDestination = if (auth.currentUser != null) "home" else "login"
                    
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onCreateAccountClick = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            com.example.lifeping.ui.auth.RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onSignInClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { themeViewModel.toggleTheme() }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}