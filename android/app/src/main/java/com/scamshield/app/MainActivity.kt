package com.scamshield.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.scamshield.app.ui.navigation.ScamShieldNavGraph
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.SCAMSHIELDTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.scamshield.app.util.LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if opened from threat notification
        val threatId = if (intent?.action == "com.scamshield.app.ACTION_VIEW_THREAT") {
            val id = intent.getLongExtra("EXTRA_THREAT_ID", -1L)
            if (id != -1L) id else null
        } else {
            null
        }

        com.scamshield.app.util.ThemeManager.init(this)

        val themeModeExtra = intent?.getStringExtra("EXTRA_THEME_MODE")
        if (themeModeExtra != null) {
            try {
                val mode = com.scamshield.app.util.AppThemeMode.valueOf(themeModeExtra.uppercase())
                com.scamshield.app.util.ThemeManager.setThemeMode(this, mode)
            } catch (_: Exception) {}
        }

        val navRouteExtra = intent?.getStringExtra("EXTRA_NAV_ROUTE")
        val startDest = when {
            threatId != null -> Screen.ThreatDetail.createRoute(threatId)
            navRouteExtra == "dashboard" -> Screen.Dashboard.route
            navRouteExtra == "settings" -> Screen.Settings.route
            navRouteExtra == "splash" -> Screen.Splash.route
            navRouteExtra == "onboarding" -> Screen.Onboarding.route
            navRouteExtra == "language" -> Screen.LanguageSelection.route
            navRouteExtra == "history" -> Screen.History.route
            navRouteExtra == "safety" -> Screen.SafetyGuide.route
            navRouteExtra == "protection" -> Screen.FamilyProtection.route
            else -> Screen.Splash.route
        }

        setContent {
            val isDark = com.scamshield.app.util.ThemeManager.isDarkTheme()
            SCAMSHIELDTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                ScamShieldNavGraph(
                    navController = navController,
                    startDestination = startDest
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val themeModeExtra = intent.getStringExtra("EXTRA_THEME_MODE")
        if (themeModeExtra != null) {
            try {
                val mode = com.scamshield.app.util.AppThemeMode.valueOf(themeModeExtra.uppercase())
                com.scamshield.app.util.ThemeManager.setThemeMode(this, mode)
            } catch (_: Exception) {}
        }

        val threatId = intent.getLongExtra("EXTRA_THREAT_ID", -1L)
        val navRouteExtra = intent.getStringExtra("EXTRA_NAV_ROUTE")
        val startDest = when {
            threatId != -1L -> Screen.ThreatDetail.createRoute(threatId)
            navRouteExtra == "dashboard" -> Screen.Dashboard.route
            navRouteExtra == "settings" -> Screen.Settings.route
            navRouteExtra == "splash" -> Screen.Splash.route
            navRouteExtra == "onboarding" -> Screen.Onboarding.route
            navRouteExtra == "language" -> Screen.LanguageSelection.route
            navRouteExtra == "history" -> Screen.History.route
            navRouteExtra == "safety" -> Screen.SafetyGuide.route
            navRouteExtra == "protection" -> Screen.FamilyProtection.route
            else -> null
        }

        if (startDest != null || themeModeExtra != null) {
            setContent {
                val isDark = com.scamshield.app.util.ThemeManager.isDarkTheme()
                SCAMSHIELDTheme(darkTheme = isDark) {
                    val navController = rememberNavController()
                    ScamShieldNavGraph(
                        navController = navController,
                        startDestination = startDest ?: Screen.Dashboard.route,
                        initialThreatId = if (threatId != -1L) threatId else null
                    )
                }
            }
        }
    }
}
