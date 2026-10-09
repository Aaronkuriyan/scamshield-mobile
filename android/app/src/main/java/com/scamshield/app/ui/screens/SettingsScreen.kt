package com.scamshield.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.AppThemeMode
import com.scamshield.app.util.LocaleHelper
import com.scamshield.app.util.ThemeManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()

    var isProtectionActive by remember { mutableStateOf(ScamNotificationListenerService.isProtectionActive(context)) }
    var isVoiceAlertsEnabled by remember { mutableStateOf(TextToSpeechHelper.isVoiceAlertEnabled(context)) }
    var currentLanguage by remember { mutableStateOf(LocaleHelper.getLanguage(context)) }
    val currentThemeMode by ThemeManager.themeMode.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showEditUrlDialog by remember { mutableStateOf(false) }
    var baseUrl by remember { mutableStateOf(NetworkClient.getBaseUrl(context)) }

    val languages = listOf(
        Triple("en", "English", "🇬🇧"),
        Triple("kn", "ಕನ್ನಡ (Kannada)", "🇮🇳"),
        Triple("hi", "हिन्दी (Hindi)", "🇮🇳")
    )

    val isDark = AppTheme.colors.isDark
    val logoRes = if (isDark) R.drawable.ic_scamshield_logo_light else R.drawable.ic_scamshield_logo_dark

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background
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
                        tint = AppTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
            }

            // Section: Appearance & Theme
            Text(
                text = stringResource(R.string.setting_appearance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.theme_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val themeOptions = listOf(
                        Triple(AppThemeMode.SYSTEM, stringResource(R.string.theme_system), "⚙️"),
                        Triple(AppThemeMode.LIGHT, stringResource(R.string.theme_light), "☀️"),
                        Triple(AppThemeMode.DARK, stringResource(R.string.theme_dark), "🌙")
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = currentThemeMode == mode
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    ThemeManager.setThemeMode(context, mode)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    if (isDark) CardNavyElevated else AccentEmeraldLight
                                } else {
                                    AppTheme.colors.surfaceElevated
                                }
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, AccentEmerald) else BorderStroke(1.dp, AppTheme.colors.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = icon, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AccentEmerald else AppTheme.colors.textPrimary
                                    )
                                }
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Language Preference
            Text(
                text = stringResource(R.string.setting_language_header),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                            color = AppTheme.colors.textPrimary
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
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Real-time Protection
            Text(
                text = stringResource(R.string.settings_section_realtime),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                                text = stringResource(R.string.setting_monitoring_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.setting_monitoring_desc),
                                fontSize = 13.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = isProtectionActive,
                            onCheckedChange = { checked ->
                                isProtectionActive = checked
                                ScamNotificationListenerService.setProtectionActive(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppTheme.colors.background,
                                checkedTrackColor = AccentEmerald
                            )
                        )
                    }

                    HorizontalDivider(color = AppTheme.colors.divider, modifier = Modifier.padding(vertical = 12.dp))

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
                                text = stringResource(R.string.setting_permission_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isNotificationServiceEnabled(context)) stringResource(R.string.status_granted) else stringResource(R.string.status_not_granted),
                                fontSize = 13.sp,
                                color = if (isNotificationServiceEnabled(context)) AccentEmerald else CautionAmber
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.textSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Audio & Voice Warnings
            Text(
                text = stringResource(R.string.settings_section_audio),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.setting_voice_alert_desc),
                            fontSize = 13.sp,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                    Switch(
                        checked = isVoiceAlertsEnabled,
                        onCheckedChange = { checked ->
                            isVoiceAlertsEnabled = checked
                            TextToSpeechHelper.setVoiceAlertEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppTheme.colors.background,
                            checkedTrackColor = AccentEmerald
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Privacy Statement
            Text(
                text = stringResource(R.string.settings_section_privacy),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                            color = AppTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.setting_privacy_desc),
                        fontSize = 13.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Data & Reset
            Text(
                text = stringResource(R.string.settings_section_data),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = baseUrl,
                                fontSize = 13.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AccentEmerald)
                    }

                    HorizontalDivider(color = AppTheme.colors.divider, modifier = Modifier.padding(vertical = 12.dp))

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

            // Section: About SCAMSHIELD
            Card(
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "SCAMSHIELD Logo",
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SCAMSHIELD",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.app_tagline),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentEmerald
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_scamshield_desc),
                        fontSize = 13.sp,
                        color = AppTheme.colors.textSecondary,
                        lineHeight = 19.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Language Switcher Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                containerColor = AppTheme.colors.surface,
                titleContentColor = AppTheme.colors.textPrimary,
                textContentColor = AppTheme.colors.textPrimary,
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
                                        (context as? android.app.Activity)?.recreate()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        if (isDark) CardNavyElevated else AccentEmeraldLight
                                    } else {
                                        AppTheme.colors.surfaceElevated
                                    }
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, AccentEmerald) else BorderStroke(1.dp, AppTheme.colors.border),
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
                                        color = if (isSelected) AccentEmerald else AppTheme.colors.textPrimary,
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
                        Text(stringResource(R.string.btn_close), color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Clear History Confirmation Dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                containerColor = AppTheme.colors.surface,
                titleContentColor = AlertCrimson,
                textContentColor = AppTheme.colors.textPrimary,
                title = { Text(stringResource(R.string.dialog_clear_title)) },
                text = { Text(stringResource(R.string.dialog_clear_desc)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.clearHistory()
                                showClearConfirm = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_clear), color = AlertCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text(stringResource(R.string.btn_cancel), color = AppTheme.colors.textSecondary)
                    }
                }
            )
        }

        // Edit API URL Dialog
        if (showEditUrlDialog) {
            var tempUrl by remember { mutableStateOf(baseUrl) }
            AlertDialog(
                onDismissRequest = { showEditUrlDialog = false },
                containerColor = AppTheme.colors.surface,
                titleContentColor = AppTheme.colors.textPrimary,
                textContentColor = AppTheme.colors.textPrimary,
                title = { Text(stringResource(R.string.dialog_api_url_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.dialog_api_url_desc), fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentEmerald,
                                unfocusedBorderColor = AppTheme.colors.border,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
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
                        Text(stringResource(R.string.btn_save), color = AccentEmerald, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditUrlDialog = false }) {
                        Text(stringResource(R.string.btn_cancel), color = AppTheme.colors.textSecondary)
                    }
                }
            )
        }
    }
}
