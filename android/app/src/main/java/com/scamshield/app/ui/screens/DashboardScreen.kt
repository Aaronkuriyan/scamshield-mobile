package com.scamshield.app.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.service.ScamNotificationListenerService
import com.scamshield.app.service.TextToSpeechHelper
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.AppFormatters
import com.scamshield.app.util.DemoSimulator
import kotlinx.coroutines.launch

enum class InboxFilter {
    ALL,
    HIGH_RISK,
    SUSPICIOUS,
    SAFE
}

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()
    val ttsHelper = remember { TextToSpeechHelper(context) }

    val allScans by repository.allScans.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf(InboxFilter.ALL) }
    var showDemoDialog by remember { mutableStateOf(false) }

    var isPermissionActive by remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    val isProtectionOn by remember { mutableStateOf(ScamNotificationListenerService.isProtectionActive(context)) }
    val isFullyActive = isPermissionActive && isProtectionOn

    // Re-check permission on composition and auto-seed initial scenarios
    LaunchedEffect(Unit) {
        isPermissionActive = isNotificationServiceEnabled(context)
        val prefs = context.getSharedPreferences("scamshield_prefs", Context.MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_initial_scans_v2", false)
        if (!hasSeeded) {
            prefs.edit().putBoolean("has_seeded_initial_scans_v2", true).apply()
            DemoSimulator.PRELOADED_TEST_CASES.forEach { testCase ->
                repository.processIncomingNotification(
                    content = testCase.content,
                    sender = testCase.sender,
                    packageName = testCase.packageName
                )
            }
        }
    }

    val highRiskList = remember(allScans) { allScans.filter { it.riskScore >= 70 } }
    val suspiciousList = remember(allScans) { allScans.filter { it.riskScore in 35..69 } }
    val safeList = remember(allScans) { allScans.filter { it.riskScore < 35 } }

    val displayedList = remember(allScans, selectedFilter) {
        when (selectedFilter) {
            InboxFilter.ALL -> allScans
            InboxFilter.HIGH_RISK -> highRiskList
            InboxFilter.SUSPICIOUS -> suspiciousList
            InboxFilter.SAFE -> safeList
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AccentEmeraldDark.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SCAMSHIELD",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isFullyActive) AccentEmerald else CautionAmber, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFullyActive) stringResource(R.string.status_protected) else stringResource(R.string.status_action_required),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFullyActive) AccentEmerald else CautionAmber
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = TextWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Protection Status Action Banner (If permission disabled)
            if (!isFullyActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                    colors = CardDefaults.cardColors(containerColor = CautionAmberDark.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, CautionAmber),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CautionAmber,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.status_action_required),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CautionAmber
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.status_action_required_sub),
                                fontSize = 13.sp,
                                color = TextWhite
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = CautionAmber
                        )
                    }
                }
            }

            // Inbox Filter Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.tab_all),
                    count = allScans.size,
                    isSelected = selectedFilter == InboxFilter.ALL,
                    badgeColor = AccentEmerald,
                    onClick = { selectedFilter = InboxFilter.ALL }
                )
                FilterTabItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.tab_high_risk),
                    count = highRiskList.size,
                    isSelected = selectedFilter == InboxFilter.HIGH_RISK,
                    badgeColor = AlertCrimson,
                    onClick = { selectedFilter = InboxFilter.HIGH_RISK }
                )
                FilterTabItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.tab_suspicious),
                    count = suspiciousList.size,
                    isSelected = selectedFilter == InboxFilter.SUSPICIOUS,
                    badgeColor = CautionAmber,
                    onClick = { selectedFilter = InboxFilter.SUSPICIOUS }
                )
                FilterTabItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.tab_safe),
                    count = safeList.size,
                    isSelected = selectedFilter == InboxFilter.SAFE,
                    badgeColor = AccentEmerald,
                    onClick = { selectedFilter = InboxFilter.SAFE }
                )
            }

            // Live Simulator & Quick Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.inbox_heading),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                TextButton(
                    onClick = { showDemoDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_simulate_demo),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentEmerald
                    )
                }
            }

            // Message Cards List
            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CardNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (allScans.isEmpty()) Icons.Default.Notifications else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (allScans.isEmpty()) stringResource(R.string.empty_inbox_title) else stringResource(R.string.empty_threats_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (allScans.isEmpty()) stringResource(R.string.empty_inbox_desc) else stringResource(R.string.empty_threats_desc),
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(displayedList, key = { it.id }) { message ->
                        MessageItemCard(
                            message = message,
                            onClick = {
                                navController.navigate(Screen.ThreatDetail.createRoute(message.id))
                            }
                        )
                    }
                }
            }
        }

        // Demo Simulator Modal Dialog
        if (showDemoDialog) {
            AlertDialog(
                onDismissRequest = { showDemoDialog = false },
                containerColor = CardNavy,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text(
                        text = "Live Mentor Test Scenarios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tap a scenario to test automatic notification detection without copy-pasting:",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        DemoSimulator.PRELOADED_TEST_CASES.forEach { testCase ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(12.dp),
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = testCase.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        val badgeColor = when {
                                            testCase.expectedType.contains("HIGH", ignoreCase = true) -> AlertCrimson
                                            testCase.expectedType.contains("SUSPICIOUS", ignoreCase = true) -> CautionAmber
                                            else -> AccentEmerald
                                        }
                                        Text(
                                            text = testCase.expectedType,
                                            fontSize = 11.sp,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = testCase.content,
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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
fun FilterTabItem(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CardNavyBorder else CardNavy
        ),
        border = if (isSelected) BorderStroke(1.5.dp, badgeColor) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) badgeColor else TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isSelected) TextWhite else TextMuted,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MessageItemCard(
    message: ScanRecordEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isHighRisk = message.riskScore >= 70
    val isSuspicious = message.riskScore in 35..69
    val isSafe = message.riskScore < 35

    val themeColor = when {
        isHighRisk -> AlertCrimson
        isSuspicious -> CautionAmber
        else -> AccentEmerald
    }

    val containerBg = when {
        isHighRisk -> AlertCrimsonDark.copy(alpha = 0.5f)
        isSuspicious -> CautionAmberDark.copy(alpha = 0.35f)
        else -> CardNavy
    }

    val sourceApp = remember(message.sourcePackage) {
        AppFormatters.getSourceAppName(context, message.sourcePackage)
    }

    val relativeTime = remember(message.timestamp) {
        AppFormatters.formatRelativeTime(message.timestamp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.2.dp, themeColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Risk Tag & Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(themeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isHighRisk -> stringResource(R.string.risk_high)
                            isSuspicious -> stringResource(R.string.risk_suspicious)
                            else -> stringResource(R.string.risk_safe)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        letterSpacing = 0.5.sp
                    )
                }

                // Risk Score Tag
                Surface(
                    color = themeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.risk_score_label)}: ${message.riskScore}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scam Category Title
            Text(
                text = message.category,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Message Preview Snippet
            val previewText = message.messageSnippet.ifBlank {
                message.senderTitle
            }
            Text(
                text = previewText,
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.85f),
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Row: Source App & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (sourceApp == "WhatsApp") Icons.Default.Phone else Icons.Default.Email,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$sourceApp • $relativeTime",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VIEW ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
