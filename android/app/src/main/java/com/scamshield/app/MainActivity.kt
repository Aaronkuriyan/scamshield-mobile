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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if opened from threat notification
        val threatId = if (intent?.action == "com.scamshield.app.ACTION_VIEW_THREAT") {
            val id = intent.getLongExtra("EXTRA_THREAT_ID", -1L)
            if (id != -1L) id else null
        } else {
            null
        }

        setContent {
            SCAMSHIELDTheme {
                val navController = rememberNavController()
                ScamShieldNavGraph(
                    navController = navController,
                    startDestination = if (threatId != null) Screen.ThreatDetail.createRoute(threatId) else Screen.Splash.route
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // If app was already open in background and threat notification clicked
        val threatId = intent?.getLongExtra("EXTRA_THREAT_ID", -1L)
        if (threatId != null && threatId != -1L) {
            setContent {
                SCAMSHIELDTheme {
                    val navController = rememberNavController()
                    ScamShieldNavGraph(
                        navController = navController,
                        initialThreatId = threatId
                    )
                }
            }
        }
    }
}
