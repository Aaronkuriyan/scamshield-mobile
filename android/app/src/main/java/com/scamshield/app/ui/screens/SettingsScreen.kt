package com.scamshield.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.data.network.NetworkClient
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.service.ScamNotificationListenerService
import com.scamshield.app.service.TextToSpeechHelper
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()

    var isProtectionActive by remember {
        mutableStateOf(ScamNotificationListenerService.isProtectionActive(context))
    }
    var isVoiceAlertsEnabled by remember {
        mutableStateOf(TextToSpeechHelper.isVoiceAlertEnabled(context))
    }

    var baseUrl by remember { mutableStateOf(NetworkClient.getBaseUrl(context)) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(baseUrl) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Protection Controls Section
            Text(
                text = "PROTECTION",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Shield Monitoring",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Automatically scans incoming notifications",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = isProtectionActive,
                            onCheckedChange = { checked ->
                                isProtectionActive = checked
                                ScamNotificationListenerService.setProtectionActive(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceDark,
                                checkedTrackColor = AccentEmerald
                            )
                        )
                    }

                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "System Notification Permission",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isNotificationServiceEnabled(context)) "Granted" else "Action Needed: Tap to allow",
                                fontSize = 13.sp,
                                color = if (isNotificationServiceEnabled(context)) AccentEmerald else CautionAmber
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Accessibility & Voice Warnings
            Text(
                text = "ACCESSIBILITY & AUDIO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voice Warnings (Text-to-Speech)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reads alerts aloud for elderly safety",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = isVoiceAlertsEnabled,
                        onCheckedChange = { checked ->
                            isVoiceAlertsEnabled = checked
                            TextToSpeechHelper.setVoiceAlertEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SurfaceDark,
                            checkedTrackColor = AccentEmerald
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Family Protection Section
            Text(
                text = "FAMILY SAFETY",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.FamilyProtection.route) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Family Protection (Opt-In)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Notify trusted contacts when high threat is flagged",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Developer & Server Configuration
            Text(
                text = "SERVER & DATA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempUrl = baseUrl
                                showUrlDialog = true
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Backend API Server",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = baseUrl,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AccentEmerald)
                    }

                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearConfirm = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clear All Scan History",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertCrimson
                        )
                        Icon(Icons.Default.Delete, contentDescription = null, tint = AlertCrimson)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Version Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "SCAMSHIELD Mobile v1.0.0", fontSize = 14.sp, color = TextMuted)
                Text(text = "Privacy First Architecture", fontSize = 12.sp, color = TextMuted.copy(alpha = 0.7f))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Base URL Dialog
        if (showUrlDialog) {
            AlertDialog(
                onDismissRequest = { showUrlDialog = false },
                containerColor = CardNavy,
                title = { Text("Configure Backend Host", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Use http://10.0.2.2:8000 for Android emulator or http://YOUR_PC_IP:8000 for physical device:",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        baseUrl = tempUrl
                        NetworkClient.setBaseUrl(context, tempUrl)
                        showUrlDialog = false
                    }) {
                        Text("SAVE", color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUrlDialog = false }) {
                        Text("CANCEL", color = TextWhite)
                    }
                }
            )
        }

        // Clear History Confirm Dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                containerColor = CardNavy,
                title = { Text("Clear All Scan Records?", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove past scan history from this device.", color = TextMuted) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repository.clearHistory()
                            showClearConfirm = false
                        }
                    }) {
                        Text("CONFIRM CLEAR", color = AlertCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("CANCEL", color = TextWhite)
                    }
                }
            )
        }
    }
}
