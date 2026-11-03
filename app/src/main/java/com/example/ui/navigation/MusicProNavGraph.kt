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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.permissions.PermissionUiState
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.permissions.PermissionScreen
import com.example.ui.settings.GlassSettingsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.splash.SplashScreen

object Destinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val SETTINGS_GLASS = "settings/glass"
}

@Composable
fun MusicProNavGraph(
    uiState: PermissionUiState,
    isOnboardingCompleted: Boolean?,
    onCompleteOnboarding: () -> Unit,
    onRequestPermissions: (Map<String, Boolean>) -> Unit,
    onManualCheck: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
    initialOpenNowPlaying: Boolean = false,
    initialOpenQueue: Boolean = false,
    initialOpenLyrics: Boolean = false,
    initialShufflePlay: Boolean = false,
    initialOpenSearch: Boolean = false,
    isWarmStart: Boolean = false,
    mediaUiRequestId: Long = 0L,
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
        // Écran 1: Splash avec logo animé (mise à jour 077 : 600 ms maximum).
        // Fondu enchaîné vers l'écran suivant ; entièrement sauté au redémarrage à chaud
        // (isWarmStart) et raccourci pour les ouvertures depuis une notification.
        composable(
            Destinations.SPLASH,
            enterTransition = { fadeIn(animationSpec = tween(260)) },
            exitTransition = { fadeOut(animationSpec = tween(260)) },
            popEnterTransition = { fadeIn(animationSpec = tween(260)) },
            popExitTransition = { fadeOut(animationSpec = tween(260)) }
        ) {
            val currentOnboardingCompleted by rememberUpdatedState(isOnboardingCompleted)
            val currentCanProceed by rememberUpdatedState(uiState.canProceedToApp)

            SplashScreen(
                isReadyToNavigate = (isOnboardingCompleted != null),
                splashDurationMillis = when {
                    isWarmStart -> 0L
                    initialOpenNowPlaying || initialOpenQueue || initialOpenLyrics -> 260L
                    else -> 600L
                },
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
                onManualCheck = onManualCheck,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Écran 4: Écran principal de la bibliothèque musicale
        composable(Destinations.HOME) {
            HomeScreen(
                settingsViewModel = settingsViewModel,
                initialOpenNowPlaying = initialOpenNowPlaying,
                initialOpenQueue = initialOpenQueue,
                initialOpenLyrics = initialOpenLyrics,
                initialShufflePlay = initialShufflePlay,
                initialOpenSearch = initialOpenSearch,
                mediaUiRequestId = mediaUiRequestId,
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
                settingsViewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPermissions = {
                    navController.navigate(Destinations.PERMISSIONS)
                },
                onOpenGlassSettings = {
                    navController.navigate(Destinations.SETTINGS_GLASS)
                }
            )
        }

        // Écran 6 (mise à jour 025) : Réglages › Glass
        composable(Destinations.SETTINGS_GLASS) {
            GlassSettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
