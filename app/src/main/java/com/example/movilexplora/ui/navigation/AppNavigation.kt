package com.example.movilexplora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movilexplora.features.home.HomeScreen
import com.example.movilexplora.features.login.LoginScreen
import com.example.movilexplora.features.registrer.RegisterScreen
import com.example.movilexplora.features.passwordRecovery.PasswordRecoveryScreen
import com.example.movilexplora.features.passwordReset.PasswordResetScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object PasswordRecovery : Screen("password_recovery")
    object PasswordReset : Screen("password_reset")
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToRecovery = { navController.navigate(Screen.PasswordRecovery.route) }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PasswordRecovery.route) {
            PasswordRecoveryScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.PasswordReset.route) {
            PasswordResetScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        
        composable(Screen.Dashboard.route) {
            // Placeholder para la pantalla principal post-login
            // Reemplazar con DashboardScreen() cuando se implemente
        }
    }
}
