package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.permissions.PermissionUiState
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.permissions.PermissionScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen

object Destinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun MusicProNavGraph(
    uiState: PermissionUiState,
    isOnboardingCompleted: Boolean?,
    onCompleteOnboarding: () -> Unit,
    onRequestPermissions: (Map<String, Boolean>) -> Unit,
    onManualCheck: () -> Unit,
    initialOpenNowPlaying: Boolean = false,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(280)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(280)
            )
        }
    ) {
        // Écran 1: Splash avec logo animé (1,5s)
        composable(Destinations.SPLASH) {
            val currentOnboardingCompleted by rememberUpdatedState(isOnboardingCompleted)
            val currentCanProceed by rememberUpdatedState(uiState.canProceedToApp)

            SplashScreen(
                isReadyToNavigate = (isOnboardingCompleted != null),
                splashDurationMillis = 1500L,
                onSplashFinished = {
                    val onboardingDone = currentOnboardingCompleted == true
                    if (!onboardingDone) {
                        navController.navigate(Destinations.ONBOARDING) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    } else {
                        if (currentCanProceed) {
                            navController.navigate(Destinations.HOME) {
                                popUpTo(Destinations.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Destinations.PERMISSIONS) {
                                popUpTo(Destinations.SPLASH) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // Écran 2: Onboarding (3 slides avec swipe + boutons)
        composable(Destinations.ONBOARDING) {
            OnboardingScreen(
                onFinishOnboarding = {
                    onCompleteOnboarding()
                    if (uiState.canProceedToApp) {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.ONBOARDING) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Destinations.PERMISSIONS) {
                            popUpTo(Destinations.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Écran 3: Demande et explications de permissions
        composable(Destinations.PERMISSIONS) {
            // Si les permissions viennent d'être accordées alors qu'on est sur cet écran
            LaunchedEffect(uiState.canProceedToApp) {
                if (uiState.canProceedToApp) {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.PERMISSIONS) { inclusive = true }
                    }
                }
            }

            PermissionScreen(
                state = uiState,
                onRequestPermissions = { result ->
                    onRequestPermissions(result)
                },
                onManualCheck = onManualCheck
            )
        }

        // Écran 4: Écran principal de la bibliothèque musicale
        composable(Destinations.HOME) {
            HomeScreen(
                initialOpenNowPlaying = initialOpenNowPlaying,
                onOpenSettings = {
                    navController.navigate(Destinations.SETTINGS)
                },
                onOpenOnboarding = {
                    navController.navigate(Destinations.ONBOARDING)
                }
            )
        }

        // Écran 5: Paramètres de l'application (Clé API Groq Whisper, sécurité, etc.)
        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPermissions = {
                    navController.navigate(Destinations.PERMISSIONS)
                }
            )
        }
    }
}
