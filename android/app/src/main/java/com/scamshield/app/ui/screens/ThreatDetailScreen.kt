package com.scamshield.app.ui.screens

import androidx.compose.foundation.background
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
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThreatDetailScreen(
    navController: NavController,
    threatId: Long
) {
    val context = LocalContext.current
    val repository = remember { ScanRepository(context) }
    val threatState by repository.getScanById(threatId).collectAsState(initial = null)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        threatState?.let { threat ->
            val isHighRisk = threat.riskScore >= 70
            val bannerColor = if (isHighRisk) AlertCrimson else CautionAmber
            val bannerBg = if (isHighRisk) AlertCrimsonDark else CautionAmberDark

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Threat Analysis",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Urgent Warning Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bannerBg),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = bannerColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHighRisk) "POSSIBLE SCAM" else "SUSPICIOUS MESSAGE",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }

                                Text(
                                    text = "${threat.riskScore}/100",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Category: ${threat.category}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Source: ${threat.senderTitle} (${threat.sourcePackage})",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Why we are concerned Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Why we are concerned:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val indicatorsList = threat.indicatorsCsv.split(" • ").filter { it.isNotBlank() }
                            if (indicatorsList.isNotEmpty()) {
                                indicatorsList.forEach { indicator ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "• ", fontSize = 18.sp, color = AlertCrimson, fontWeight = FontWeight.Bold)
                                        Text(text = indicator, fontSize = 16.sp, color = TextWhite, lineHeight = 22.sp)
                                    }
                                }
                            } else {
                                Text(
                                    text = "This message contains patterns commonly used by scammers to deceive users.",
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // What you should do (Clear elderly-friendly instructions)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavy),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "What you should do:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            SafetyRuleItem("Do NOT click any link in the message.")
                            SafetyRuleItem("Do NOT share your OTP, PIN, or password.")
                            SafetyRuleItem("Do NOT transfer or send money.")
                            SafetyRuleItem("Verify directly through official branch or helpline.")

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Advice: ${threat.recommendation}",
                                fontSize = 15.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Anonymized Time & Engine details
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(threat.timestamp))

                    Text(
                        text = "Scanned on: $dateFormatted\nEngine: ${threat.engine.replace("_", " ").uppercase()}",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }

                // Action Button
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                    ) {
                        Text(
                            text = "DISMISS WARNING",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceDark
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentEmerald)
            }
        }
    }
}

@Composable
fun SafetyRuleItem(ruleText: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AccentEmerald,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = ruleText,
            fontSize = 16.sp,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold
        )
    }
}
