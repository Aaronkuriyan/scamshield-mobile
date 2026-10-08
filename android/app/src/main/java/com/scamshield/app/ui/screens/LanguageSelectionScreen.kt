package com.scamshield.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import androidx.compose.ui.graphics.Color
import com.scamshield.app.util.LocaleHelper

@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LocaleHelper.getLanguage(context)) }

    val languages = listOf(
        Triple("en", "English", "🇬🇧"),
        Triple("kn", "ಕನ್ನಡ (Kannada)", "🇮🇳"),
        Triple("hi", "हिन्दी (Hindi)", "🇮🇳")
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.choose_language_title),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.choose_language_subtitle),
                    fontSize = 17.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Language Option Cards (Large touch targets for elderly accessibility)
                languages.forEach { (code, label, flag) ->
                    val isSelected = selectedLanguage == code

                    val cardContainerColor = if (isSelected) {
                        if (AppTheme.colors.isDark) AccentEmeraldDark.copy(alpha = 0.45f) else AccentEmeraldLight
                    } else {
                        AppTheme.colors.surface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedLanguage = code },
                        colors = CardDefaults.cardColors(
                            containerColor = cardContainerColor
                        ),
                        border = if (isSelected) BorderStroke(2.dp, AccentEmerald) else BorderStroke(1.dp, AppTheme.colors.border),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = flag,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = label,
                                    fontSize = 20.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AccentEmerald else AppTheme.colors.textPrimary
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Continue Action
            Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                Button(
                    onClick = {
                        LocaleHelper.setLanguage(context, selectedLanguage)
                        val isPermissionGranted = isNotificationServiceEnabled(context)
                        if (isPermissionGranted) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text(
                        text = stringResource(R.string.btn_continue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) SurfaceDark else Color(0xFF0F172A),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
