package com.scamshield.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.service.ScamNotificationListenerService
import com.scamshield.app.service.TextToSpeechHelper
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.DemoSimulator
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()
    val ttsHelper = remember { TextToSpeechHelper(context) }

    val totalScans by repository.totalScansCount.collectAsState(initial = 0)
    val totalThreats by repository.threatsCount.collectAsState(initial = 0)
    val recentThreat by repository.recentThreat.collectAsState(initial = null)

    var isPermissionActive by remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    var isProtectionOn by remember { mutableStateOf(ScamNotificationListenerService.isProtectionActive(context)) }
    var showDemoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Bar / Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCAMSHIELD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { navController.navigate(Screen.Settings.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Central Protection Status Card
            val isFullyActive = isPermissionActive && isProtectionOn
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFullyActive) AccentEmeraldDark.copy(alpha = 0.5f) else CautionAmberDark.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isPermissionActive) {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = if (isFullyActive) AccentEmerald else CautionAmber,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyActive) Icons.Default.VerifiedUser else Icons.Default.Warning,
                            contentDescription = null,
                            tint = SurfaceDark,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isFullyActive) "PROTECTION ACTIVE" else "PERMISSION NEEDED",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullyActive) AccentEmerald else CautionAmber,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isFullyActive) "You are protected automatically." else "Tap to grant notification access.",
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Checked",
                    count = totalScans.toString(),
                    icon = Icons.Default.Security,
                    color = AccentEmerald
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Threats Blocked",
                    count = totalThreats.toString(),
                    icon = Icons.Default.GppBad,
                    color = if (totalThreats > 0) AlertCrimson else TextMuted
                )
            }

            // Recent Threat Card (if any exists)
            recentThreat?.let { threat ->
                Spacer(modifier = Modifier.height(20.dp))
                RecentThreatCard(
                    threat = threat,
                    onClick = {
                        navController.navigate(Screen.ThreatDetail.createRoute(threat.id))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Navigation Buttons (Large touch targets for elderly accessibility)
            Button(
                onClick = { navController.navigate(Screen.History.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardNavy)
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = AccentEmerald)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "VIEW THREAT HISTORY",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate(Screen.SafetyGuide.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardNavy)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = AccentEmerald)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SAFETY GUIDE FOR ELDERLY",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Hackathon Demo Simulator Button
            OutlinedButton(
                onClick = { showDemoDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentEmerald)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AccentEmerald)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SIMULATE LIVE DEMO TEST",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Demo Simulator Modal Dialog
        if (showDemoDialog) {
            AlertDialog(
                onDismissRequest = { showDemoDialog = false },
                containerColor = CardNavy,
                titleColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text(
                        text = "Live Hackathon Test Cases",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Choose a scenario to simulate automatic detection, alert notification, and voice warning:",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        DemoSimulator.PRELOADED_TEST_CASES.forEach { testCase ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDemoDialog = false
                                        scope.launch {
                                            DemoSimulator.runSimulation(context, testCase, ttsHelper)
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = testCase.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = testCase.expectedType,
                                            fontSize = 12.sp,
                                            color = if (testCase.expectedType.contains("HIGH")) AlertCrimson else AccentEmerald,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = testCase.content,
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDemoDialog = false }) {
                        Text("CLOSE", color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = count, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, color = TextMuted)
        }
    }
}

@Composable
fun RecentThreatCard(
    threat: ScanRecordEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AlertCrimsonDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AlertCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECENT THREAT DETECTED",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertCrimson
                    )
                }

                Text(
                    text = "${threat.riskScore}/100",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = threat.category,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = threat.recommendation,
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.9f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TAP TO VIEW FULL SAFETY ADVICE →",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
        }
    }
}
