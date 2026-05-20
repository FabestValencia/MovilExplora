package com.example.movilexplora

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.example.movilexplora.features.map.MapSelectorScreen
import com.example.movilexplora.core.navigation.MapSelector
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.movilexplora.data.model.UserSession
import com.example.movilexplora.domain.model.enum.UserRole
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.movilexplora.core.component.BottomNavigationBar

import com.example.movilexplora.features.moderator.ModeratorHistoryScreen
import com.example.movilexplora.features.onboarding.OnboardingScreen
import com.example.movilexplora.features.onboarding.OnboardingViewModel
import com.google.firebase.messaging.FirebaseMessaging

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getFcmToken()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()
        getFcmToken()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val isFirstLaunch by onboardingViewModel.state.collectAsState()

            MovilExploraTheme(darkTheme = isDarkMode) {
                if (isFirstLaunch.isFirstLaunch) {
                    OnboardingScreen(
                        onFinished = { /* State will update and show AppNavigation */ },
                        viewModel = onboardingViewModel
                    )
                } else {
                    AppNavigation()
                }
            }
        }
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                println("FCM TOKEN: $token")
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
    
    NavHost(navController = navController, startDestination = if (session.role == UserRole.ADMIN) ModeratorFeed else MainDashboard) {
        if (session.role == UserRole.ADMIN) {
            // Admin Routes
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
            // User Dashboard (Nested Navigation according to doc5.md)
            composable<MainDashboard> {
                UserDashboard(
                    onLogout = onLogout,
                    onNavigateToPostDetail = { postId -> navController.navigate(PostDetail(postId)) },
                    onNavigateToEventDetail = { eventId -> navController.navigate(EventDetail(eventId)) },
                    onNavigateToCreatePost = { postId -> navController.navigate(CreatePost(postId)) },
                    onNavigateToCreateEvent = { eventId -> navController.navigate(CreateEditEvent(eventId)) },
                    onNavigateToEditProfile = { navController.navigate(EditProfile) },
                    onNavigateToReputation = { navController.navigate(Reputation) },
                    onNavigateToBadges = { navController.navigate(Badges) },
                    onNavigateToStatistics = { navController.navigate(Statistics) }
                )
            }

            // Detail Screens (Outside dashboard to hide bottom bar)
            composable<CreatePost> { backStackEntry ->
                val args = backStackEntry.toRoute<CreatePost>()
                CreatePostScreen(
                    postId = args.postId,
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
            composable<EditProfile> {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUpdateSuccess = { navController.popBackStack() }
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
            composable<MapSelector> {
                MapSelectorScreen(
                    onNavigateBack = { _ ->
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun UserDashboard(
    onLogout: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToCreatePost: (String?) -> Unit,
    onNavigateToCreateEvent: (String?) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToReputation: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onCreateClick = { onNavigateToCreatePost(null) },
                onHomeClick = { navController.navigate(Feed) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                } },
                onEventsClick = { navController.navigate(Events) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                } },
                onAlertsClick = { navController.navigate(Notifications) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                } },
                onProfileClick = { navController.navigate(Profile) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                } },
                selectedItem = when (currentDestination?.route?.substringAfterLast('.')) {
                    "Feed" -> "Inicio"
                    "Events" -> "Eventos"
                    "Notifications" -> "Alertas"
                    "Profile" -> "Perfil"
                    "MapRoute" -> "Mapa"
                    else -> "Inicio"
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Feed,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Feed> {
                FeedScreen(
                    onNavigateToDetail = onNavigateToPostDetail,
                    onNavigateToMap = { navController.navigate(MapRoute) }
                )
            }
            composable<Events> {
                EventsScreen(
                    onNavigateToEventDetail = onNavigateToEventDetail,
                    onNavigateToCreateEvent = { onNavigateToCreateEvent(null) },
                    onNavigateToMap = { navController.navigate(MapRoute) }
                )
            }
            composable<MapRoute> {
                MapScreen(
                    onNavigateToCreatePost = { onNavigateToCreatePost(null) },
                    onNavigateToDetail = onNavigateToPostDetail,
                    onNavigateToEventDetail = onNavigateToEventDetail
                )
            }
            composable<Profile> {
                ProfileScreen(
                    onEditData = onNavigateToEditProfile,
                    onNavigateToEditEvent = onNavigateToCreateEvent,
                    onNavigateToEditPost = onNavigateToCreatePost,
                    onNavigateToReputation = onNavigateToReputation,
                    onNavigateToBadges = onNavigateToBadges,
                    onNavigateToStatistics = onNavigateToStatistics,
                    onLogout = onLogout
                )
            }
            composable<Notifications> {
                NotificationsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Statistics> {
                StatisticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

