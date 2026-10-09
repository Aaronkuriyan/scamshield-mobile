package com.scamshield.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*

@Composable
fun PermissionScreen(navController: NavController) {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

    // Re-check permission automatically when user returns from system Settings screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = isNotificationServiceEnabled(context)
                if (isGranted) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Permission.route) { inclusive = true }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.permission_desc),
                    fontSize = 17.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 25.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.permission_how_it_works_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.permission_how_it_works_desc),
                            fontSize = 15.sp,
                            color = AppTheme.colors.textPrimary,
                            lineHeight = 23.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 32.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Text(
                        text = stringResource(R.string.btn_enable_protection),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (AppTheme.colors.isDark) SurfaceDark else Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        // Directly proceed to Dashboard
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Permission.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.textSecondary),
                    border = BorderStroke(1.2.dp, AppTheme.colors.border)
                ) {
                    Text(
                        text = stringResource(R.string.btn_continue_to_app),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
