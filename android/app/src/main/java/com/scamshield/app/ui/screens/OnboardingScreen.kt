package com.scamshield.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(
                        if (AppTheme.colors.isDark) R.drawable.ic_scamshield_logo_light
                        else R.drawable.ic_scamshield_logo_dark
                    ),
                    contentDescription = "SCAMSHIELD",
                    modifier = Modifier.size(68.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.onboarding_title),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    fontSize = 17.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 25.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.onboarding_auto_title),
                    description = stringResource(R.string.onboarding_auto_desc)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.onboarding_privacy_title),
                    description = stringResource(R.string.onboarding_privacy_desc)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.CheckCircle,
                    title = stringResource(R.string.onboarding_clear_title),
                    description = stringResource(R.string.onboarding_clear_desc)
                )
            }

            Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                Button(
                    onClick = { navController.navigate(Screen.Permission.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text(
                        text = stringResource(R.string.btn_get_started),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) SurfaceDark else Color(0xFF0F172A),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun OnboardingFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(AppTheme.colors.surface, shape = RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, AppTheme.colors.border), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentEmerald,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = AppTheme.colors.textSecondary,
                lineHeight = 22.sp
            )
        }
    }
}
