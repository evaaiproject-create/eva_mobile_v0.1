package com.example.eva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eva.app.data.repository.AuthRepository
import com.example.eva.app.data.repository.ChatRepository
import com.example.eva.ui.theme.chat.ChatScreen
import com.example.eva.ui.theme.login.LoginScreen
import com.example.eva.ui.theme.navigation.NavigationDrawerContent
import com.example.eva.ui.theme.spash.SplashScreen
import com.example.eva.ui.theme.templates.TemplateScreen
import com.example.eva.ui.theme.EvaTheme
import com.example.eva.app.utils.Constants
import kotlinx.coroutines.launch

/*
================================================================================
MAIN ACTIVITY - SINGLE ACTIVITY ARCHITECTURE
================================================================================

PURPOSE:
    This is the ONLY Activity in the app.
    All screens are Composable functions managed by Jetpack Navigation.

SINGLE ACTIVITY ARCHITECTURE:
    Benefits:
    - Simpler navigation (no Intent hassles)
    - Shared ViewModel scopes
    - Easier animations between screens
    - Better state preservation

STRUCTURE:
    MainActivity
    └── EvaTheme (theme wrapper)
        └── ModalNavigationDrawer (hamburger menu)
            └── NavHost (navigation container)
                ├── SplashScreen
                ├── LoginScreen
                ├── ChatScreen
                ├── LiveCallScreen
                ├── TemplateScreen (x3)
                └── SettingsScreen

NAVIGATION:
    Uses Jetpack Compose Navigation with string routes.
    Routes are defined in Constants.kt.

================================================================================
*/

class MainActivity : ComponentActivity() {

    /*
    ================================================================================
    PROPERTIES
    ================================================================================
    */

    /**
     * Repository for authentication operations.
     * Created lazily so context is available.
     */
    private val authRepository by lazy { AuthRepository(this) }

    /**
     * Repository for chat operations.
     */
    private val chatRepository by lazy { ChatRepository(authRepository) }

    /*
    ================================================================================
    LIFECYCLE
    ================================================================================
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display (content goes behind system bars)
        enableEdgeToEdge()

        // Set the Compose content
        setContent {
            EvaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EvaApp(
                        authRepository = authRepository,
                        chatRepository = chatRepository
                    )
                }
            }
        }
    }
}

/*
================================================================================
MAIN APP COMPOSABLE
================================================================================
*/

/**
 * Root composable for the Eva app.
 *
 * Sets up:
 * - Navigation controller
 * - Navigation drawer (hamburger menu)
 * - Screen navigation
 */
@Composable
fun EvaApp(
    authRepository: AuthRepository,
    chatRepository: ChatRepository
) {
    // Navigation controller - manages screen navigation
    val navController = rememberNavController()

    // Drawer state - controls hamburger menu open/close
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Coroutine scope for drawer operations
    val scope = rememberCoroutineScope()

    // Function to open the drawer
    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    // Function to close the drawer
    val closeDrawer: () -> Unit = {
        scope.launch { drawerState.close() }
    }

    // Modal Navigation Drawer (hamburger menu)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, // Only allow swipe when open
        drawerContent = {
            NavigationDrawerContent(
                authRepository = authRepository,
                chatRepository = chatRepository,
                onNavigate = { route ->
                    closeDrawer()
                    // Navigate to the selected route
                    navController.navigate(route) {
                        // Pop up to chat to avoid building up back stack
                        popUpTo(Constants.ROUTE_CHAT) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNewChat = {
                    closeDrawer()
                    // Navigate to chat with a new conversation
                    navController.navigate(Constants.ROUTE_CHAT) {
                        popUpTo(Constants.ROUTE_CHAT) { inclusive = true }
                    }
                },
                onLogout = {
                    closeDrawer()
                    authRepository.logout()
                    // Navigate to login and clear back stack
                    navController.navigate(Constants.ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClose = closeDrawer
            )
        }
    ) {
        // Navigation Host - contains all screens
        NavHost(
            navController = navController,
            startDestination = Constants.ROUTE_SPLASH
        ) {
            // Splash Screen
            composable(Constants.ROUTE_SPLASH) {
                SplashScreen(
                    authRepository = authRepository,
                    onNavigateToLogin = {
                        navController.navigate(Constants.ROUTE_LOGIN) {
                            popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToChat = {
                        navController.navigate(Constants.ROUTE_CHAT) {
                            popUpTo(Constants.ROUTE_SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable(Constants.ROUTE_LOGIN) {
                LoginScreen(
                    authRepository = authRepository,
                    onLoginSuccess = {
                        navController.navigate(Constants.ROUTE_CHAT) {
                            popUpTo(Constants.ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // Chat Screen
            composable(Constants.ROUTE_CHAT) {
                ChatScreen(
                    chatRepository = chatRepository,
                    authRepository = authRepository,
                    onOpenDrawer = openDrawer,
                    onStartCall = {
                        navController.navigate(Constants.ROUTE_CALL)
                    }
                )
            }

            // Live Call Screen
            composable(Constants.ROUTE_CALL) {
                // Assuming LiveCallScreen is in a similar path or you can add its import if you find it
                // For now, using TemplateScreen as placeholder if LiveCallScreen not found or just keeping the navigation
                TemplateScreen(
                    title = "Live Call",
                    message = "Live Call coming soon",
                    onOpenDrawer = openDrawer
                )
            }

            // Template Screens
            composable(Constants.ROUTE_TEMPLATE_1) {
                TemplateScreen(
                    title = "Template 1",
                    onOpenDrawer = openDrawer
                )
            }

            composable(Constants.ROUTE_TEMPLATE_2) {
                TemplateScreen(
                    title = "Template 2",
                    onOpenDrawer = openDrawer
                )
            }

            composable(Constants.ROUTE_TEMPLATE_3) {
                TemplateScreen(
                    title = "Template 3",
                    onOpenDrawer = openDrawer
                )
            }

            // Settings Screen (placeholder)
            composable(Constants.ROUTE_SETTINGS) {
                TemplateScreen(
                    title = "Settings",
                    message = "Settings coming soon",
                    onOpenDrawer = openDrawer
                )
            }
        }
    }
}
