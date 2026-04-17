package com.privacyguard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.privacyguard.ui.screens.dashboard.DashboardScreen
import com.privacyguard.ui.screens.keylogger.KeyloggerScreen
import com.privacyguard.ui.screens.micusage.MicUsageScreen
import com.privacyguard.ui.screens.nightactivity.NightActivityScreen
import com.privacyguard.ui.screens.onboarding.OnboardingScreen
import com.privacyguard.ui.screens.settings.SettingsScreen
import com.privacyguard.ui.screens.triggermap.TriggerMapScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object MicUsage : Screen("mic_usage")
    object Keylogger : Screen("keylogger")
    object NightActivity : Screen("night_activity")
    object TriggerMap : Screen("trigger_map")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onPermissionGranted = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateMic = { navController.navigate(Screen.MicUsage.route) },
                onNavigateKeylogger = { navController.navigate(Screen.Keylogger.route) },
                onNavigateNight = { navController.navigate(Screen.NightActivity.route) },
                onNavigateTrigger = { navController.navigate(Screen.TriggerMap.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.MicUsage.route) {
            MicUsageScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Keylogger.route) {
            KeyloggerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NightActivity.route) {
            NightActivityScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.TriggerMap.route) {
            TriggerMapScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
