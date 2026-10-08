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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.scamshield.app.ui.theme.*

data class SafetyTopic(
    val title: String,
    val rule: String,
    val fakeExample: String,
    val whyDangerous: String
)

val SAFETY_TOPICS = listOf(
    SafetyTopic(
        title = "OTP & Password Phishing",
        rule = "NEVER share your OTP with anyone calling or texting you.",
        fakeExample = "\"Dear customer, share the 6-digit OTP code to avoid card blocking.\"",
        whyDangerous = "Bank staff will NEVER ask for your OTP. If someone asks for it, they are trying to steal money from your account."
    ),
    SafetyTopic(
        title = "UPI PIN / Collect Requests",
        rule = "You NEVER enter a PIN to receive money.",
        fakeExample = "\"Click approve on Google Pay or enter UPI PIN to receive Rs 5000 cashback.\"",
        whyDangerous = "Entering your UPI PIN ALWAYS transfers money OUT of your bank account, never into it."
    ),
    SafetyTopic(
        title = "Electricity / Power Cutoff Threats",
        rule = "Utility companies never disconnect power within hours via SMS.",
        fakeExample = "\"Your electricity power will be cut off at 9:30 PM tonight due to unpaid bill. Call officer now.\"",
        whyDangerous = "Fraudsters create fake panic to make you pay money or install dangerous apps on your phone."
    ),
    SafetyTopic(
        title = "Fake KYC & Bank Blockage",
        rule = "Never update KYC through SMS links.",
        fakeExample = "\"Your SBI account is blocked. Click http://sbi-kyc.xyz to update Aadhaar & PAN.\"",
        whyDangerous = "The link opens a fake lookalike website designed to steal your net-banking password and PAN card."
    ),
    SafetyTopic(
        title = "Lottery & KBC Lucky Draw",
        rule = "If you didn't buy a ticket, you didn't win.",
        fakeExample = "\"Congratulations! You won Rs 25 Lakhs. Pay Rs 5,000 registration fee to claim.\"",
        whyDangerous = "Scammers ask for an advance 'tax' or 'processing' fee and then disappear."
    ),
    SafetyTopic(
        title = "Remote Control Apps (AnyDesk / TeamViewer)",
        rule = "NEVER install screen-sharing apps on instructions from a stranger.",
        fakeExample = "\"Install AnyDesk app from Play Store so bank customer care can help you.\"",
        whyDangerous = "These apps allow the scammer to see your phone screen and take over your banking apps."
    ),
    SafetyTopic(
        title = "Fake Courier / Delivery Scams",
        rule = "Check package updates only inside official apps (Amazon, Flipkart, Post).",
        fakeExample = "\"IndiaPost: Package undelivered. Pay Rs 48 redelivery fee using this link.\"",
        whyDangerous = "Small payment links steal credit card numbers and net-banking credentials."
    ),
    SafetyTopic(
        title = "Work-from-Home & Job Scams",
        rule = "Real employers never make you pay to work.",
        fakeExample = "\"Earn Rs 5,000 daily by liking YouTube videos. Pay Rs 1,000 joining fee.\"",
        whyDangerous = "Victims are lured into deposit schemes where money cannot be withdrawn."
    )
)

@Composable
fun SafetyGuideScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
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
                    text = "Safety Guide",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Text(
                text = "Simple rules to keep you and your family safe from message scams:",
                fontSize = 16.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(SAFETY_TOPICS) { topic ->
                    SafetyTopicCard(topic)
                }
            }
        }
    }
}

@Composable
fun SafetyTopicCard(topic: SafetyTopic) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = topic.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AccentEmerald
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Rule: ${topic.rule}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentEmerald
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Example Scam Message:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlertCrimson
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = topic.fakeExample,
                        fontSize = 14.sp,
                        color = TextWhite,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Why this is dangerous:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.whyDangerous,
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
