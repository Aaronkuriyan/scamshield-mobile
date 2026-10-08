package com.scamshield.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.ui.navigation.Screen
import com.scamshield.app.ui.theme.*
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
        color = SurfaceDark
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                if (allScans.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = TextMuted)
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No scan logs yet.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Incoming message notifications will appear here automatically.",
                            fontSize = 14.sp,
                            color = TextMuted
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
                containerColor = CardNavy,
                title = { Text("Clear Scan History?", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = { Text("All local scan records will be cleared. Protection will remain active.", color = TextMuted) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repository.clearHistory()
                            showClearConfirm = false
                        }
                    }) {
                        Text("CLEAR", color = AlertCrimson, fontWeight = FontWeight.Bold)
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

@Composable
fun HistoryItemCard(
    scan: ScanRecordEntity,
    onClick: () -> Unit
) {
    val (statusColor, badgeEmoji) = when {
        scan.riskScore >= 70 -> AlertCrimson to "🔴"
        scan.riskScore >= 35 -> CautionAmber to "🟠"
        else -> AccentEmerald to "🟢"
    }

    val timeFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        .format(Date(scan.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardNavy),
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
                            text = "— ${scan.classification}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scan.category,
                        fontSize = 15.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$timeFormatted • ${scan.senderTitle}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "VIEW →",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
        }
    }
}
