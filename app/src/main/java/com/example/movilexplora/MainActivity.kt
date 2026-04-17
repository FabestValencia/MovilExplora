package com.example.movilexplora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.example.movilexplora.core.navigation.*
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

import com.example.movilexplora.features.moderator.ModeratorHistoryScreen

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
            is SessionState.NotAuthenticated -> AuthNavigation()
            is SessionState.Authenticated -> MainNavigation(
                session = state.session,
                onLogout = sessionViewModel::logout
            )
        }
    }
}

@Composable
private fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onNavigateToRegister = { navController.navigate(Register) },
                onNavigateToLogin = { navController.navigate(Login) }
            )
        }
        composable<Login> {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForgotPassword = { navController.navigate(ForgotPassword) },
                onNavigateToModerator = { navController.navigate(Moderator) }
            )
        }
        composable<Register> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Login) }
            )
        }
        composable<ForgotPassword> {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerifyCode = { navController.navigate(VerificationCode) }
            )
        }
        composable<VerificationCode> {
            VerificationCodeScreen(
                onNavigateBack = { navController.popBackStack() },
                onVerifySuccess = { navController.navigate(ResetPassword) }
            )
        }
        composable<ResetPassword> {
            ResetPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = { navController.navigate(Success) }
            )
        }
        composable<Success> {
            SuccessScreen(
                onNavigateToLogin = {
                    navController.navigate(Login) { popUpTo(Home) { inclusive = false } }
                }
            )
        }
        composable<Moderator> {
            ModeratorScreen(
                onNavigateBack = { navController.popBackStack() },
                onAccessGranted = {
                    // Session is saved directly by ModeratorViewModel, handled by AppNavigation
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
    
    NavHost(navController = navController, startDestination = if (session.role == UserRole.MODERATOR || session.role == UserRole.ADMIN) ModeratorFeed else Feed) {
        if (session.role == UserRole.MODERATOR || session.role == UserRole.ADMIN) {
            // Admin / Moderator Routes
            composable<ModeratorFeed> {
                ModeratorFeedScreen(
                    onLogout = { onLogout() },
                    onNavigateToHistory = { navController.navigate(ModeratorHistory) }
                )
            }
            composable<ModeratorHistory> {
                ModeratorHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        } else {
            // User Routes
            composable<Feed> {
                FeedScreen(
                    onNavigateToDetail = { postId -> navController.navigate(PostDetail(postId)) },
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToMap = { navController.navigate(MapRoute) },
                    onNavigateToEvents = { navController.navigate(Events) },
                    onNavigateToNotifications = { navController.navigate(Notifications) },
                    onNavigateToProfile = { navController.navigate(Profile) }
                )
            }
            composable<Events> {
                EventsScreen(
                    onNavigateToEventDetail = { eventId -> navController.navigate(EventDetail(eventId)) },
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToCreateEvent = { navController.navigate(CreateEditEvent(null)) },
                    onNavigateToHome = { navController.navigate(Feed) },
                    onNavigateToMap = { navController.navigate(MapRoute) },
                    onNavigateToNotifications = { navController.navigate(Notifications) },
                    onNavigateToProfile = { navController.navigate(Profile) }
                )
            }
            composable<MapRoute> {
                MapScreen(
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToFeed = { navController.navigate(Feed) },
                    onNavigateToDetail = { postId -> navController.navigate(PostDetail(postId)) }
                )
            }
            composable<CreatePost> {
                CreatePostScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPublishSuccess = { navController.popBackStack() }
                )
            }
            composable<CreateEditEvent> { backStackEntry ->
                val args = backStackEntry.toRoute<CreateEditEvent>()
                com.example.movilexplora.features.events.CreateEditEventScreen(
                    eventId = args.eventId,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable<PostDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<PostDetail>()
                PostDetailScreen(
                    postId = args.postId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<EventDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<EventDetail>()
                EventDetailScreen(
                    eventId = args.eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Statistics> {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToHome = { navController.navigate(Feed) },
                    onNavigateToEvents = { navController.navigate(Events) },
                    onNavigateToNotifications = { navController.navigate(Notifications) },
                    onNavigateToProfile = { navController.navigate(Profile) }
                )
            }
            composable<Profile> {
                ProfileScreen(
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToHome = { navController.navigate(Feed) },
                    onNavigateToEvents = { navController.navigate(Events) },
                    onNavigateToNotifications = { navController.navigate(Notifications) },
                    onEditData = { navController.navigate(EditProfile) },
                    onNavigateToEditEvent = { eventId -> navController.navigate(CreateEditEvent(eventId)) },
                    onNavigateToReputation = { navController.navigate(Reputation) },
                    onNavigateToBadges = { navController.navigate(Badges) },
                    onNavigateToStatistics = { navController.navigate(Statistics) },
                    onLogout = onLogout
                )
            }
            composable<EditProfile> {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUpdateSuccess = { navController.popBackStack() }
                )
            }
            composable<Notifications> {
                NotificationsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreatePost = { navController.navigate(CreatePost) },
                    onNavigateToHome = { navController.navigate(Feed) },
                    onNavigateToEvents = { navController.navigate(Events) },
                    onNavigateToProfile = { navController.navigate(Profile) }
                )
            }
            composable<Reputation> {
                ReputationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Badges> {
                BadgesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
