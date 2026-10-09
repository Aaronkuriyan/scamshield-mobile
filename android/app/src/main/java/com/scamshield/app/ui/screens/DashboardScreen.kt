package com.scamshield.app.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import com.scamshield.app.util.AppThemeMode
import com.scamshield.app.util.ThemeManager
import com.scamshield.app.util.LocalizationHelper
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

    val isDark = AppTheme.colors.isDark

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

    Scaffold(
        containerColor = AppTheme.colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = AppTheme.colors.surface,
                contentColor = AppTheme.colors.textPrimary,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on Home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                    label = { Text(stringResource(R.string.nav_home), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentEmerald,
                        selectedTextColor = AccentEmerald,
                        unselectedIconColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        unselectedTextColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        indicatorColor = if (isDark) AccentEmeraldDark.copy(alpha = 0.45f) else AccentEmeraldLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.FamilyProtection.route) },
                    icon = { Icon(Icons.Default.Shield, contentDescription = stringResource(R.string.nav_protection)) },
                    label = { Text(stringResource(R.string.nav_protection), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentEmerald,
                        selectedTextColor = AccentEmerald,
                        unselectedIconColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        unselectedTextColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        indicatorColor = if (isDark) AccentEmeraldDark.copy(alpha = 0.45f) else AccentEmeraldLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.SafetyGuide.route) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.nav_safety)) },
                    label = { Text(stringResource(R.string.nav_safety), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentEmerald,
                        selectedTextColor = AccentEmerald,
                        unselectedIconColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        unselectedTextColor = if (isDark) AppTheme.colors.textSecondary else Color(0xFF475569),
                        indicatorColor = if (isDark) AccentEmeraldDark.copy(alpha = 0.45f) else AccentEmeraldLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings), fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentEmerald,
                        selectedTextColor = AccentEmerald,
                        unselectedIconColor = AppTheme.colors.textSecondary,
                        unselectedTextColor = AppTheme.colors.textSecondary,
                        indicatorColor = if (isDark) AccentEmeraldDark.copy(alpha = 0.45f) else AccentEmeraldLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            .background(
                                if (isDark) AccentEmeraldDark.copy(alpha = 0.4f) else AccentEmeraldLight,
                                CircleShape
                            ),
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
                            color = AppTheme.colors.textPrimary,
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

                // Quick Theme Toggle (Top-right Settings icon removed; Settings is accessed exclusively via bottom navigation)
                IconButton(
                    onClick = {
                        val newMode = if (isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                        ThemeManager.setThemeMode(context, newMode)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = stringResource(
                            if (isDark) R.string.action_switch_to_light else R.string.action_switch_to_dark
                        ),
                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFF475569),
                        modifier = Modifier.size(24.dp)
                    )
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
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) CautionAmberDark.copy(alpha = 0.4f) else CautionAmberLight
                    ),
                    border = BorderStroke(1.dp, if (isDark) CautionAmber else CautionAmberBorder),
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
                                color = if (isDark) TextWhite else Color(0xFF78350F)
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
                    color = AppTheme.colors.textPrimary
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
                                .background(AppTheme.colors.surfaceElevated, CircleShape),
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
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (allScans.isEmpty()) stringResource(R.string.empty_inbox_desc) else stringResource(R.string.empty_threats_desc),
                            fontSize = 14.sp,
                            color = AppTheme.colors.textSecondary,
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
                containerColor = AppTheme.colors.surface,
                titleContentColor = AppTheme.colors.textPrimary,
                textContentColor = AppTheme.colors.textSecondary,
                title = {
                    Text(
                        text = stringResource(R.string.demo_dialog_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.demo_dialog_desc),
                            fontSize = 14.sp,
                            color = AppTheme.colors.textSecondary
                        )
                        DemoSimulator.PRELOADED_TEST_CASES.forEach { testCase ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
                                border = BorderStroke(1.dp, AppTheme.colors.border),
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
                                            color = AppTheme.colors.textPrimary
                                        )
                                        val badgeColor = when {
                                            testCase.expectedType.contains("HIGH", ignoreCase = true) -> AlertCrimson
                                            testCase.expectedType.contains("SUSPICIOUS", ignoreCase = true) -> CautionAmber
                                            else -> AccentEmerald
                                        }
                                        val expectedTypeLabel = when {
                                            testCase.expectedType.contains("HIGH", ignoreCase = true) -> stringResource(R.string.risk_high)
                                            testCase.expectedType.contains("SUSPICIOUS", ignoreCase = true) -> stringResource(R.string.risk_suspicious)
                                            else -> stringResource(R.string.risk_safe)
                                        }
                                        Text(
                                            text = expectedTypeLabel,
                                            fontSize = 11.sp,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = testCase.content,
                                        fontSize = 13.sp,
                                        color = AppTheme.colors.textSecondary,
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
                        Text(stringResource(R.string.btn_close), color = AccentEmerald, fontWeight = FontWeight.Bold)
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
    val isDark = AppTheme.colors.isDark
    val containerBg = if (isSelected) {
        if (isDark) CardNavyBorder else AccentEmeraldLight
    } else {
        AppTheme.colors.surfaceElevated
    }
    val borderColor = if (isSelected) badgeColor else AppTheme.colors.border

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        ),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
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
                color = if (isSelected) badgeColor else AppTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = if (isSelected) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
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
    val isDark = AppTheme.colors.isDark

    val themeColor = when {
        isHighRisk -> AlertCrimson
        isSuspicious -> CautionAmber
        else -> AccentEmerald
    }

    val containerBg = when {
        isHighRisk -> if (isDark) AlertCrimsonDark.copy(alpha = 0.22f) else AlertCrimsonLight
        isSuspicious -> if (isDark) CautionAmberDark.copy(alpha = 0.22f) else CautionAmberLight
        else -> if (isDark) AppTheme.colors.surface else AccentEmeraldLight.copy(alpha = 0.6f)
    }

    val cardBorder = when {
        isHighRisk -> BorderStroke(1.2.dp, if (isDark) AlertCrimson.copy(alpha = 0.5f) else AlertCrimsonBorder)
        isSuspicious -> BorderStroke(1.2.dp, if (isDark) CautionAmber.copy(alpha = 0.5f) else CautionAmberBorder)
        else -> BorderStroke(1.dp, if (isDark) AppTheme.colors.border else AccentEmeraldBorder)
    }

    val sourceApp = remember(message.sourcePackage) {
        AppFormatters.getSourceAppName(context, message.sourcePackage)
    }

    val relativeTime = remember(message.timestamp) {
        AppFormatters.formatRelativeTime(context, message.timestamp)
    }

    val localizedCategory = remember(message.category) {
        LocalizationHelper.getLocalizedCategory(context, message.category)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = cardBorder,
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
                    color = themeColor.copy(alpha = if (isDark) 0.2f else 0.12f),
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
                text = localizedCategory,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Message Preview Snippet
            val previewText = message.messageSnippet.ifBlank {
                message.senderTitle
            }
            Text(
                text = previewText,
                fontSize = 14.sp,
                color = if (isDark) TextWhite.copy(alpha = 0.88f) else Color(0xFF334155),
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
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$sourceApp • $relativeTime",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.btn_view_analysis),
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
