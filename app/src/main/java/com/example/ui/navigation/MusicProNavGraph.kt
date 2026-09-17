package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.permissions.PermissionUiState
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.permissions.PermissionScreen
import com.example.ui.splash.SplashScreen

object Destinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
}

@Composable
fun MusicProNavGraph(
    uiState: PermissionUiState,
    isOnboardingCompleted: Boolean?,
    onCompleteOnboarding: () -> Unit,
    onRequestPermissions: (Map<String, Boolean>) -> Unit,
    onManualCheck: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH,
        modifier = modifier
    ) {
        // Écran 1: Splash avec logo animé (1,5s)
        composable(Destinations.SPLASH) {
            SplashScreen(
                splashDurationMillis = 1500L,
                onSplashFinished = {
                    val onboardingDone = isOnboardingCompleted ?: false
                    if (!onboardingDone) {
                        navController.navigate(Destinations.ONBOARDING) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    } else {
                        if (uiState.canProceedToApp) {
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
                onOpenSettings = {
                    navController.navigate(Destinations.PERMISSIONS)
                },
                onOpenOnboarding = {
                    navController.navigate(Destinations.ONBOARDING)
                }
            )
        }
    }
}
