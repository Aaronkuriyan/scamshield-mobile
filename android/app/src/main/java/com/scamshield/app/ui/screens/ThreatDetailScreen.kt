package com.scamshield.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.R
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.engine.LocalScamFilter
import com.scamshield.app.ui.theme.*
import com.scamshield.app.util.AppFormatters

@Composable
fun ThreatDetailScreen(
    navController: NavController,
    threatId: Long
) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    var threat by remember { mutableStateOf<ScanRecordEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(threatId) {
        isLoading = true
        if (threatId > 0) {
            repository.getScanById(threatId).collect { record ->
                if (record != null) {
                    threat = record
                    isLoading = false
                } else {
                    repository.allScans.collect { list ->
                        threat = list.firstOrNull()
                        isLoading = false
                    }
                }
            }
        } else {
            repository.allScans.collect { list ->
                threat = list.firstOrNull()
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        threat?.let { threat ->
            val isHighRisk = threat.riskScore >= 70
            val isSuspicious = threat.riskScore in 35..69
            val isSafe = threat.riskScore < 35

            val themeColor = when {
                isHighRisk -> AlertCrimson
                isSuspicious -> CautionAmber
                else -> AccentEmerald
            }

            val sourceApp = remember(threat.sourcePackage) {
                AppFormatters.getSourceAppName(context, threat.sourcePackage)
            }
            val formattedTime = remember(threat.timestamp) {
                AppFormatters.formatRelativeTime(threat.timestamp)
            }

            val indicatorsList = remember(threat.indicatorsCsv) {
                threat.indicatorsCsv.split(" • ").filter { it.isNotBlank() }
            }

            val whatToDoList = remember(threat.whatToDoCsv, threat.category, threat.classification) {
                val parsed = threat.whatToDoCsv.split(" | ").filter { it.isNotBlank() }
                if (parsed.isNotEmpty()) parsed else LocalScamFilter.generateWhatToDo(threat.category, threat.classification)
            }

            val whatNotToDoList = remember(threat.whatNotToDoCsv, threat.category, threat.classification) {
                val parsed = threat.whatNotToDoCsv.split(" | ").filter { it.isNotBlank() }
                if (parsed.isNotEmpty()) parsed else LocalScamFilter.generateWhatNotToDo(threat.category, threat.classification)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
                            text = stringResource(R.string.details_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Hero Risk Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isHighRisk -> AlertCrimsonDark.copy(alpha = 0.55f)
                                isSuspicious -> CautionAmberDark.copy(alpha = 0.4f)
                                else -> AccentEmeraldDark.copy(alpha = 0.4f)
                            }
                        ),
                        border = BorderStroke(1.5.dp, themeColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = when {
                                            isHighRisk -> stringResource(R.string.risk_high)
                                            isSuspicious -> stringResource(R.string.risk_suspicious)
                                            else -> stringResource(R.string.risk_safe)
                                        },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColor,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Surface(
                                    color = themeColor.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${threat.riskScore}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColor,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = threat.category,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = threat.recommendation,
                                fontSize = 15.sp,
                                color = TextWhite.copy(alpha = 0.9f),
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Message Content Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.section_message),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$sourceApp • $formattedTime",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val messageText = threat.messageSnippet.ifBlank { threat.senderTitle }
                            Text(
                                text = "\"$messageText\"",
                                fontSize = 16.sp,
                                color = TextWhite,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // "Why We Flagged This" Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = stringResource(R.string.section_why_flagged),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) AccentEmerald else themeColor,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isSafe || indicatorsList.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.safe_summary),
                                        fontSize = 15.sp,
                                        color = TextWhite,
                                        lineHeight = 22.sp
                                    )
                                }
                            } else {
                                indicatorsList.forEach { indicator ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "🔴",
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = indicator,
                                            fontSize = 15.sp,
                                            color = TextWhite,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // WHAT TO DO (✓) Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        border = BorderStroke(1.2.dp, AccentEmerald.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(AccentEmeraldDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.section_what_to_do),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentEmerald,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            whatToDoList.forEach { action ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "✓",
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = action,
                                        fontSize = 15.sp,
                                        color = TextWhite,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // WHAT NOT TO DO (✕) Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        border = BorderStroke(1.2.dp, AlertCrimson.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(AlertCrimsonDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✕",
                                        color = AlertCrimson,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.section_what_not_to_do),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            whatNotToDoList.forEach { forbidden ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "✕",
                                        color = AlertCrimson,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = forbidden,
                                        fontSize = 15.sp,
                                        color = TextWhite,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    // Emergency 1930 Cyber Helpline Button (for high risk)
                    if (isHighRisk) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:1930")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = TextWhite)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_call_helpline),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }

                // Bottom Back Action Button
                Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = BorderStroke(1.5.dp, CardNavyBorder)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.btn_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = AccentEmerald)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Threat Record Not Found",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This scan record is no longer available or was cleared.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                        ) {
                            Text("Back to Inbox", color = TextWhite)
                        }
                    }
                }
            }
        }
    }
}
