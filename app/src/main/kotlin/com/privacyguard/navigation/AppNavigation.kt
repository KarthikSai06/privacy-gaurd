package com.privacyguard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.privacyguard.ui.screens.aiinsights.AiInsightsScreen
import com.privacyguard.ui.screens.appdetail.AppDetailScreen
import com.privacyguard.ui.screens.biometrics.BiometricsScreen
import com.privacyguard.ui.screens.breach.BreachScreen
import com.privacyguard.ui.screens.camera.CameraUsageScreen
import com.privacyguard.ui.screens.clustering.ClusteringScreen
import com.privacyguard.ui.screens.dashboard.DashboardScreen
import com.privacyguard.ui.screens.imsicatcher.IMSICatcherScreen
import com.privacyguard.ui.screens.intentgraph.IntentGraphScreen
import com.privacyguard.ui.screens.keylogger.KeyloggerScreen
import com.privacyguard.ui.screens.location.LocationUsageScreen
import com.privacyguard.ui.screens.micusage.MicUsageScreen
import com.privacyguard.ui.screens.network.NetworkMonitorScreen
import com.privacyguard.ui.screens.nightactivity.NightActivityScreen
import com.privacyguard.ui.screens.onboarding.OnboardingScreen
import com.privacyguard.ui.screens.permissions.PermissionManagerScreen
import com.privacyguard.ui.screens.phishing.PhishingScreen
import com.privacyguard.ui.screens.report.ReportScreen
import com.privacyguard.ui.screens.settings.SettingsScreen
import com.privacyguard.ui.screens.timeline.TimelineScreen
import com.privacyguard.ui.screens.trends.TrendsScreen
import com.privacyguard.ui.screens.triggermap.TriggerMapScreen

sealed class Screen(val route: String) {
    object Onboarding    : Screen("onboarding")
    object Dashboard     : Screen("dashboard")
    object MicUsage      : Screen("mic_usage")
    object CameraUsage   : Screen("camera_usage")
    object LocationUsage : Screen("location_usage")
    object Keylogger     : Screen("keylogger")
    object NightActivity : Screen("night_activity")
    object TriggerMap    : Screen("trigger_map")
    object Settings      : Screen("settings")
    object Report        : Screen("report")
    object AiInsights    : Screen("ai_insights")
    object NetworkMonitor: Screen("network_monitor")
    object Permissions   : Screen("permissions")
    object Timeline      : Screen("timeline")
    object Phishing      : Screen("phishing")
    object Trends        : Screen("trends")
    object Breach        : Screen("breach")
    object IMSICatcher   : Screen("imsi_catcher")
    object Clustering    : Screen("clustering")
    object IntentGraph   : Screen("intent_graph")
    object Biometrics    : Screen("biometrics")
    object AppDetail     : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
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
                onNavigateMic       = { navController.navigate(Screen.MicUsage.route) },
                onNavigateCamera    = { navController.navigate(Screen.CameraUsage.route) },
                onNavigateLocation  = { navController.navigate(Screen.LocationUsage.route) },
                onNavigateKeylogger = { navController.navigate(Screen.Keylogger.route) },
                onNavigateNight     = { navController.navigate(Screen.NightActivity.route) },
                onNavigateTrigger   = { navController.navigate(Screen.TriggerMap.route) },
                onNavigateSettings  = { navController.navigate(Screen.Settings.route) },
                onNavigateReport    = { navController.navigate(Screen.Report.route) },
                onNavigateAi        = { navController.navigate(Screen.AiInsights.route) },
                onNavigateNetwork   = { navController.navigate(Screen.NetworkMonitor.route) },
                onNavigatePermissions = { navController.navigate(Screen.Permissions.route) },
                onNavigateTimeline  = { navController.navigate(Screen.Timeline.route) },
                onNavigatePhishing  = { navController.navigate(Screen.Phishing.route) },
                onNavigateTrends    = { navController.navigate(Screen.Trends.route) },
                onNavigateBreach    = { navController.navigate(Screen.Breach.route) },
                onNavigateImsi      = { navController.navigate(Screen.IMSICatcher.route) },
                onNavigateClustering = { navController.navigate(Screen.Clustering.route) },
                onNavigateIntentGraph = { navController.navigate(Screen.IntentGraph.route) },
                onNavigateBiometrics = { navController.navigate(Screen.Biometrics.route) }
            )
        }
        composable(Screen.MicUsage.route) {
            MicUsageScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.CameraUsage.route) {
            CameraUsageScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.LocationUsage.route) {
            LocationUsageScreen(onBack = { navController.popBackStack() })
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
        composable(Screen.Report.route) {
            ReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AiInsights.route) {
            AiInsightsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.NetworkMonitor.route) {
            NetworkMonitorScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Permissions.route) {
            PermissionManagerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Timeline.route) {
            TimelineScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Phishing.route) {
            PhishingScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Trends.route) {
            TrendsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Breach.route) {
            BreachScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.IMSICatcher.route) {
            IMSICatcherScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Clustering.route) {
            ClusteringScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.IntentGraph.route) {
            IntentGraphScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Biometrics.route) {
            BiometricsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.AppDetail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
