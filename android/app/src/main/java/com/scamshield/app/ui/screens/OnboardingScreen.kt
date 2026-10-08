package com.scamshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
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
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to\nSCAMSHIELD",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your quiet guardian against fraud, bank scams, and malicious links.",
                    fontSize = 18.sp,
                    color = TextMuted,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Automatic Protection",
                    description = "No copying or pasting required. SCAMSHIELD alerts you the moment a dangerous message arrives."
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy by Default",
                    description = "Normal family messages and daily conversations stay on your phone. Only suspicious signals are checked."
                )

                Spacer(modifier = Modifier.height(20.dp))

                OnboardingFeatureItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Simple, Clear Warnings",
                    description = "No confusing technical terms. We tell you plainly: Do not click the link, and do not share your OTP."
                )
            }

            Column(modifier = Modifier.padding(top = 32.dp)) {
                Button(
                    onClick = { navController.navigate(Screen.Permission.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text(
                        text = "GET STARTED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceDark
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
                .background(CardNavy, shape = RoundedCornerShape(12.dp)),
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
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = TextMuted,
                lineHeight = 22.sp
            )
        }
    }
}
