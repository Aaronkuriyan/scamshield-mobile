package com.scamshield.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.scamshield.app.ui.screens.*

@Composable
fun ScamShieldNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    initialThreatId: Long? = null
) {
    NavHost(
        navController = navController,
        startDestination = if (initialThreatId != null) Screen.ThreatDetail.createRoute(initialThreatId) else startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Permission.route) {
            PermissionScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.SafetyGuide.route) {
            SafetyGuideScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.FamilyProtection.route) {
            FamilyProtectionScreen(navController = navController)
        }
        composable(
            route = Screen.ThreatDetail.route,
            arguments = listOf(navArgument("threatId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threatId = backStackEntry.arguments?.getLong("threatId") ?: 0L
            ThreatDetailScreen(navController = navController, threatId = threatId)
        }
    }
}
