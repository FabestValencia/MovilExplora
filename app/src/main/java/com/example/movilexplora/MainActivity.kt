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
import com.example.movilexplora.features.eventdetail.EventDetailScreen
import com.example.movilexplora.features.reputation.ReputationScreen
import com.example.movilexplora.features.badges.BadgesScreen
import com.example.movilexplora.features.statistics.StatisticsScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.navigation.SessionViewModel
import com.example.movilexplora.core.navigation.SessionState
import com.example.movilexplora.core.navigation.ThemeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.movilexplora.data.model.UserSession
import com.example.movilexplora.domain.model.UserRole

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            MovilExploraTheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        when (val state = sessionState) {
            is SessionState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SessionState.NotAuthenticated -> AuthNavigation(sessionViewModel)
            is SessionState.Authenticated -> MainNavigation(
                session = state.session,
                onLogout = sessionViewModel::logout
            )
        }
    }
}

@Composable
private fun AuthNavigation(sessionViewModel: SessionViewModel) {
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
                    navController.navigate("login") { popUpTo("home") { inclusive = false } }
                }
            )
        }
        composable("moderator") {
            ModeratorScreen(
                onNavigateBack = { navController.popBackStack() },
                onAccessGranted = {
                    // Force login as moderator using Session ViewModel
                    sessionViewModel.login("admin", UserRole.MODERATOR)
                }
            )
        }
    }
}

@Composable
private fun MainNavigation(
    session: UserSession,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val startDestination = when (session.role) {
        UserRole.MODERATOR, UserRole.ADMIN -> "moderator_feed"
        else -> "feed"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        if (session.role == UserRole.MODERATOR || session.role == UserRole.ADMIN) {
            // Admin / Moderator Routes
            composable("moderator_feed") {
                ModeratorFeedScreen(
                    onLogout = { onLogout() }
                )
            }
        } else {
            // User Routes
            composable("feed") {
                FeedScreen(
                    onNavigateToDetail = { postId -> navController.navigate("post_detail/$postId") },
                    onNavigateToCreatePost = { navController.navigate("create_post") },
                    onNavigateToMap = { navController.navigate("map") },
                    onNavigateToEvents = { navController.navigate("events") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            composable("events") {
                EventsScreen(
                    onNavigateToEventDetail = { eventId -> navController.navigate("event_detail/$eventId") },
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
                    onNavigateToDetail = { postId -> navController.navigate("post_detail/$postId") }
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
            composable("event_detail/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("statistics") {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreatePost = { navController.navigate("create_post") },
                    onNavigateToHome = { navController.navigate("feed") },
                    onNavigateToEvents = { navController.navigate("events") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToCreatePost = { navController.navigate("create_post") },
                    onNavigateToHome = { navController.navigate("feed") },
                    onNavigateToEvents = { navController.navigate("events") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onEditData = { navController.navigate("edit_profile") },
                    onNavigateToEditEvent = { eventId -> navController.navigate("create_edit_event?eventId=$eventId") },
                    onNavigateToReputation = { navController.navigate("reputation") },
                    onNavigateToBadges = { navController.navigate("badges") },
                    onNavigateToStatistics = { navController.navigate("statistics") }
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreatePost = { navController.navigate("create_post") },
                    onNavigateToHome = { navController.navigate("feed") },
                    onNavigateToEvents = { navController.navigate("events") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            composable("reputation") {
                ReputationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("badges") {
                BadgesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
