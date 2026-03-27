package com.example.movilexplora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movilexplora.features.createpost.CreatePostScreen
import com.example.movilexplora.features.editprofile.EditProfileScreen
import com.example.movilexplora.features.feed.FeedScreen
import com.example.movilexplora.features.forgotpassword.ForgotPasswordScreen
import com.example.movilexplora.features.home.HomeScreen
import com.example.movilexplora.features.login.LoginScreen
import com.example.movilexplora.features.map.MapScreen
import com.example.movilexplora.features.moderator.ModeratorScreen
import com.example.movilexplora.features.moderator.ModeratorFeedScreen
import com.example.movilexplora.features.notifications.NotificationsScreen
import com.example.movilexplora.features.postdetail.PostDetailScreen
import com.example.movilexplora.features.profile.ProfileScreen
import com.example.movilexplora.features.registrer.RegisterScreen
import com.example.movilexplora.features.resetpassword.ResetPasswordScreen
import com.example.movilexplora.features.success.SuccessScreen
import com.example.movilexplora.features.verificationcode.VerificationCodeScreen
import com.example.movilexplora.ui.theme.MovilExploraTheme
import com.example.movilexplora.features.events.EventsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovilExploraTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUsers = { navController.navigate("feed") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onNavigateToModerator = { navController.navigate("moderator") }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerifyCode = { navController.navigate("verification_code") }
            )
        }
        composable("verification_code") {
            VerificationCodeScreen(
                onNavigateBack = { navController.popBackStack() },
                onVerifySuccess = { navController.navigate("reset_password") }
            )
        }
        composable("reset_password") {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = { navController.navigate("success") }
            )
        }
        composable("success") {
            SuccessScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        composable("moderator") {
            ModeratorScreen(
                onNavigateBack = { navController.popBackStack() },
                onAccessGranted = { 
                    navController.navigate("moderator_feed") {
                        popUpTo("moderator") { inclusive = true }
                    }
                }
            )
        }
        composable("moderator_feed") {
            ModeratorFeedScreen(
                onLogout = { 
                    navController.navigate("home") {
                        popUpTo("moderator_feed") { inclusive = true }
                    }
                }
            )
        }
        composable("feed") {
            FeedScreen(
                onNavigateToDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToEvents = { navController.navigate("events") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("events") {
            EventsScreen(
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToCreateEvent = { navController.navigate("create_edit_event") },
                onNavigateToHome = { navController.navigate("feed") },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("map") {
            MapScreen(
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToFeed = { navController.navigate("feed") },
                onNavigateToDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }
        composable("create_post") {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() },
                onPublishSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = "create_edit_event?eventId={eventId}",
            arguments = listOf(navArgument("eventId") { nullable = true })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            com.example.movilexplora.features.events.CreateEditEventScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable("post_detail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToHome = { navController.navigate("feed") },
                onNavigateToEvents = { navController.navigate("events") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onEditData = { navController.navigate("edit_profile") },
                onNavigateToEditEvent = { eventId -> navController.navigate("create_edit_event?eventId=$eventId") }
            )
        }
        composable("edit_profile") {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onUpdateSuccess = { navController.popBackStack() }
            )
        }
        composable("notifications") {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
