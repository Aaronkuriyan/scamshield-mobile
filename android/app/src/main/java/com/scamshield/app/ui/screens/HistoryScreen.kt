package com.scamshield.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.AppFormatters
import com.scamshield.app.util.LocalizationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val scope = rememberCoroutineScope()
    val allScans by repository.allScans.collectAsState(initial = emptyList())

    var showClearConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = AppTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )
                }

                if (allScans.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_clear), tint = AppTheme.colors.textSecondary)
                    }
                }
            }

            if (allScans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppTheme.colors.textSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.history_empty_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.history_empty_desc),
                            fontSize = 14.sp,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allScans) { scan ->
                        HistoryItemCard(
                            scan = scan,
                            onClick = {
                                navController.navigate(Screen.ThreatDetail.createRoute(scan.id))
                            }
                        )
                    }
                }
            }
        }

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                containerColor = AppTheme.colors.surface,
                title = { Text(stringResource(R.string.history_dialog_clear_title), color = AppTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(R.string.history_dialog_clear_desc), color = AppTheme.colors.textSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repository.clearHistory()
                            showClearConfirm = false
                        }
                    }) {
                        Text(stringResource(R.string.btn_clear), color = AlertCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text(stringResource(R.string.btn_cancel), color = AppTheme.colors.textPrimary)
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    scan: ScanRecordEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val (statusColor, badgeEmoji) = when {
        scan.riskScore >= 70 -> AlertCrimson to "🔴"
        scan.riskScore >= 35 -> CautionAmber to "🟠"
        else -> AccentEmerald to "🟢"
    }

    val classificationLabel = when {
        scan.riskScore >= 70 || scan.classification.equals("HIGH RISK", ignoreCase = true) -> stringResource(R.string.risk_high)
        scan.riskScore >= 35 || scan.classification.equals("SUSPICIOUS", ignoreCase = true) -> stringResource(R.string.risk_suspicious)
        else -> stringResource(R.string.risk_safe)
    }
    val localizedCategory = LocalizationHelper.getLocalizedCategory(context, scan.category)
    val timeFormatted = AppFormatters.formatRelativeTime(context, scan.timestamp)
    val appDisplayName = AppFormatters.getSourceAppName(context, scan.sourcePackage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = badgeEmoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${scan.riskScore}/100",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "— $classificationLabel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizedCategory,
                        fontSize = 15.sp,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$timeFormatted • ${if (scan.senderTitle.isNotBlank()) scan.senderTitle else appDisplayName}",
                        fontSize = 13.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            Text(
                text = stringResource(R.string.history_btn_view),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
        }
    }
}
