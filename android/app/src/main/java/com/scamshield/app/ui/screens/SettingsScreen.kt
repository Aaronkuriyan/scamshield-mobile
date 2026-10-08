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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.data.network.NetworkClient
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.service.ScamNotificationListenerService
import com.scamshield.app.service.TextToSpeechHelper
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.LocaleHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()

    var isProtectionActive by remember { mutableStateOf(ScamNotificationListenerService.isProtectionActive(context)) }
    var isVoiceAlertsEnabled by remember { mutableStateOf(TextToSpeechHelper.isVoiceAlertEnabled(context)) }
    var currentLanguage by remember { mutableStateOf(LocaleHelper.getLanguage(context)) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showEditUrlDialog by remember { mutableStateOf(false) }
    var baseUrl by remember { mutableStateOf(NetworkClient.getBaseUrl(context)) }

    val languages = listOf(
        Triple("en", "English", "🇬🇧"),
        Triple("kn", "ಕನ್ನಡ (Kannada)", "🇮🇳"),
        Triple("hi", "हिन्दी (Hindi)", "🇮🇳")
    )

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
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Section 1: Language Preference
            Text(
                text = "LANGUAGE",
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
                    .clickable { showLanguageDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.setting_language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        val currentLabel = languages.firstOrNull { it.first == currentLanguage }?.second ?: "English"
                        Text(
                            text = currentLabel,
                            fontSize = 14.sp,
                            color = AccentEmerald,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Real-time Protection
            Text(
                text = "REAL-TIME PROTECTION",
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
                                text = "Active Message Monitoring",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Automatically checks incoming message notifications",
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

            // Section 3: Audio & Voice Warnings
            Text(
                text = "AUDIO & ACCESSIBILITY",
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
                            text = stringResource(R.string.setting_voice_alert),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.setting_voice_alert_desc),
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

            // Section 4: Privacy Statement
            Text(
                text = "PRIVACY & SECURITY",
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
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting_privacy_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.setting_privacy_desc),
                        fontSize = 13.sp,
                        color = TextWhite.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Data & Reset
            Text(
                text = "DATA MANAGEMENT",
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
                            .clickable { showEditUrlDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.setting_backend_url),
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
                            text = stringResource(R.string.setting_clear_history),
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
                Text(
                    text = "SCAMSHIELD Mobile v1.0",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_tagline),
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Language Switcher Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                containerColor = CardNavy,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text(
                        text = stringResource(R.string.choose_language_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        languages.forEach { (code, label, flag) ->
                            val isSelected = currentLanguage == code
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLanguage = code
                                        LocaleHelper.setLanguage(context, code)
                                        showLanguageDialog = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AccentEmeraldDark.copy(alpha = 0.5f) else SurfaceDark
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$flag  $label",
                                        fontSize = 16.sp,
                                        color = if (isSelected) AccentEmerald else TextWhite,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AccentEmerald,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("CLOSE", color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Clear History Confirmation Dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                containerColor = CardNavy,
                titleContentColor = AlertCrimson,
                textContentColor = TextWhite,
                title = { Text("Clear All Scan History?") },
                text = { Text("This will permanently remove all analyzed message logs from local storage.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.clearHistory()
                                showClearConfirm = false
                            }
                        }
                    ) {
                        Text("CLEAR", color = AlertCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("CANCEL", color = TextMuted)
                    }
                }
            )
        }

        // Edit API URL Dialog
        if (showEditUrlDialog) {
            var tempUrl by remember { mutableStateOf(baseUrl) }
            AlertDialog(
                onDismissRequest = { showEditUrlDialog = false },
                containerColor = CardNavy,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = { Text("Backend Server URL") },
                text = {
                    Column {
                        Text("Set the IP/URL of your running FastAPI service:", fontSize = 13.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentEmerald,
                                unfocusedBorderColor = CardNavyBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            NetworkClient.setBaseUrl(context, tempUrl)
                            baseUrl = tempUrl
                            showEditUrlDialog = false
                        }
                    ) {
                        Text("SAVE", color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditUrlDialog = false }) {
                        Text("CANCEL", color = TextMuted)
                    }
                }
            )
        }
    }
}
