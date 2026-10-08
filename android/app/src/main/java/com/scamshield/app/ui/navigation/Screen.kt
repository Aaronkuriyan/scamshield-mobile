package com.scamshield.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LanguageSelection : Screen("language_selection")
    object Onboarding : Screen("onboarding")
    object Permission : Screen("permission")
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object SafetyGuide : Screen("safety_guide")
    object Settings : Screen("settings")
    object FamilyProtection : Screen("family_protection")
    object ThreatDetail : Screen("threat_detail/{threatId}") {
        fun createRoute(threatId: Long) = "threat_detail/$threatId"
    }
}
