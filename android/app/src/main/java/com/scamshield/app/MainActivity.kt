package com.scamshield.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.scamshield.app.service.ScamNotificationListenerService
import com.scamshield.app.ui.navigation.ScamShieldNavGraph
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.SCAMSHIELDTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.scamshield.app.util.LocaleHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure notification listener service is bound
        ScamNotificationListenerService.ensureServiceBound(this)

        // Request runtime permissions for SMS and Notifications if needed
        requestRequiredRuntimePermissions()

        // Check if opened from threat notification
        val threatId = if (intent?.action == "com.scamshield.app.ACTION_VIEW_THREAT") {
            val id = intent.getLongExtra("EXTRA_THREAT_ID", -1L)
            if (id != -1L) id else null
        } else {
            null
        }

        com.scamshield.app.util.ThemeManager.init(this)

        val langExtra = intent?.getStringExtra("EXTRA_LANGUAGE")
        if (langExtra != null) {
            com.scamshield.app.util.LocaleHelper.setLanguage(this, langExtra)
        }

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

    override fun onResume() {
        super.onResume()
        // Re-confirm service is bound whenever app returns to foreground
        ScamNotificationListenerService.ensureServiceBound(this)
    }

    private fun requestRequiredRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsToRequest = mutableListOf<String>()

            if (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.RECEIVE_SMS)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), 1001)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val langExtra = intent.getStringExtra("EXTRA_LANGUAGE")
        if (langExtra != null) {
            com.scamshield.app.util.LocaleHelper.setLanguage(this, langExtra)
            recreate()
            return
        }

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
