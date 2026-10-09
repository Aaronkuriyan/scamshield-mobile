package com.scamshield.app.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.75f) }
    val isDark = AppTheme.colors.isDark
    val logoRes = if (isDark) R.drawable.ic_scamshield_logo_light else R.drawable.ic_scamshield_logo_dark

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 650)
        )
        delay(1100)

        val hasSelectedLanguage = com.scamshield.app.util.LocaleHelper.isLanguageSelected(context)
        val isPermissionGranted = isNotificationServiceEnabled(context)

        if (!hasSelectedLanguage) {
            navController.navigate(Screen.LanguageSelection.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else if (isPermissionGranted) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "SCAMSHIELD Logo",
                modifier = Modifier.size(116.dp)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "SCAMSHIELD",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.app_tagline),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colors.textSecondary
            )
        }
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(pkgName)
}
