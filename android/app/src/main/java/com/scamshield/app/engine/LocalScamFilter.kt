package com.scamshield.app.engine

import java.util.regex.Pattern

data class LocalEvaluationResult(
    val riskScore: Int,
    val classification: String,
    val category: String,
    val indicators: List<String>,
    val recommendation: String,
    val shouldQueryBackend: Boolean,
    val isSafe: Boolean
)

object LocalScamFilter {

    private val PROTECTIVE_PATTERNS = listOf(
        Pattern.compile("do\\s+not\\s+share\\s+(?:your\\s+)?(?:otp|pin|password|cvv)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("never\\s+share\\s+(?:your\\s+)?(?:otp|pin|password|cvv)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("bank\\s+never\\s+asks\\s+for\\s+(?:otp|pin|password|details)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("beware\\s+of\\s+(?:fraud|scams|fake)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("do\\s+not\\s+disclose", Pattern.CASE_INSENSITIVE),
        Pattern.compile("keep\\s+(?:it\\s+)?confidential", Pattern.CASE_INSENSITIVE)
    )

    private data class Rule(
        val category: String,
        val weight: Int,
        val isCritical: Boolean,
        val indicator: String,
        val patterns: List<Pattern>
    )

    private val RULES = listOf(
        Rule(
            category = "OTP & Credential Phishing",
            weight = 55,
            isCritical = true,
            indicator = "Demands sharing OTP or verification code",
            patterns = listOf(
                Pattern.compile("(?:share|send|enter|give|provide)\\s+(?:your\\s+)?(?:otp|one\\s+time\\s+password|verification\\s+code)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("otp\\s+is\\s+required\\s+to\\s+(?:claim|receive|verify|unblock)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("forward\\s+this\\s+(?:sms|otp|code)", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "UPI Scam",
            weight = 70,
            isCritical = true,
            indicator = "Tricks into entering UPI PIN or approving collect request to receive money",
            patterns = listOf(
                Pattern.compile("enter\\s+(?:upi\\s+)?pin\\s+to\\s+receive", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:approve|accept)\\s+(?:collect\\s+)?request.*(?:to\\s+receive|reward|cashback|refund)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("scan\\s+(?:this\\s+)?qr\\s+code\\s+to\\s+receive\\s+money", Pattern.CASE_INSENSITIVE),
                Pattern.compile("send\\s+(?:₹|rs\\.?|inr)\\s*1\\s+to\\s+verify", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Account Suspension Scam",
            weight = 40,
            isCritical = false,
            indicator = "Threatens immediate bank account or card blockage",
            patterns = listOf(
                Pattern.compile("account\\s+(?:will\\s+be\\s+)?(?:blocked|suspended|deactivated|closed)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:sbi|hdfc|icici|axis|pnb|bank|card|netbanking|debit\\s+card)\\s+(?:account\\s+)?(?:has\\s+been|is)\\s*(?:blocked|suspended)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("sim\\s+card\\s+will\\s+be\\s+deactivated", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Fake KYC",
            weight = 45,
            isCritical = false,
            indicator = "Demands urgent KYC update or Aadhaar/PAN linking",
            patterns = listOf(
                Pattern.compile("(?:update|complete|verify)\\s+your\\s+kyc", Pattern.CASE_INSENSITIVE),
                Pattern.compile("kyc\\s+(?:has\\s+)?expired", Pattern.CASE_INSENSITIVE),
                Pattern.compile("link\\s+aadhaar\\s+(?:with|to)\\s+(?:bank|pan)", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Government Impersonation",
            weight = 60,
            isCritical = true,
            indicator = "Threatens electricity disconnection for fake unpaid bill",
            patterns = listOf(
                Pattern.compile("electricity\\s+(?:power\\s+)?will\\s+be\\s+(?:disconnected|cut\\s*off)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("power\\s+cut\\s+(?:tonight|today|at)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("contact\\s+electricity\\s+officer", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Lottery / Prize Scam",
            weight = 55,
            isCritical = true,
            indicator = "Claims fake lottery win requiring upfront processing fee",
            patterns = listOf(
                Pattern.compile("(?:congratulations|hurry)\\W.*won\\s+(?:₹|rs\\.?|inr|\\$)\\s*[\\d,]+", Pattern.CASE_INSENSITIVE),
                Pattern.compile("won\\s+(?:a\\s+)?(?:car|iphone|cash|lottery|prize)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("kbc\\s+(?:lottery|lucky\\s+draw)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?:processing|registration|release)\\s+fee\\s+of\\s+(?:₹|rs\\.?)", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Remote Access Scam",
            weight = 70,
            isCritical = true,
            indicator = "Demands installing screen-sharing app like AnyDesk or TeamViewer",
            patterns = listOf(
                Pattern.compile("(?:install|download)\\s+(?:anydesk|teamviewer|quicksupport|rustdesk)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("share\\s+(?:9\\s*digit|your)\\s+code\\s+for\\s+support", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Delivery Scam",
            weight = 35,
            isCritical = false,
            indicator = "Fake package delivery error asking for payment or address link",
            patterns = listOf(
                Pattern.compile("(?:parcel|package|delivery)\\s+(?:could\\s+not|failed|pending)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("pay\\s+(?:₹|rs\\.?)\\s*[\\d,]+\\s*(?:delivery|tracking|reschedule)\\s+fee", Pattern.CASE_INSENSITIVE),
                Pattern.compile("indiapost.*undelivered", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Malicious Link",
            weight = 30,
            isCritical = false,
            indicator = "Contains shortened or suspicious link",
            patterns = listOf(
                Pattern.compile("https?://(?:bit\\.ly|tinyurl\\.com|t\\.co|is\\.gd|cutt\\.ly|rb\\.gy)/\\S+", Pattern.CASE_INSENSITIVE),
                Pattern.compile("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?::\\d+)?/\\S*", Pattern.CASE_INSENSITIVE),
                Pattern.compile("https?://\\S+\\.(?:xyz|top|work|click|club|buzz)/\\S*", Pattern.CASE_INSENSITIVE),
                Pattern.compile("https?://\\S*(?:apk|download|verify-kyc|claim-reward|sbi-update|unblock)\\S*", Pattern.CASE_INSENSITIVE)
            )
        ),
        Rule(
            category = "Social Engineering",
            weight = 20,
            isCritical = false,
            indicator = "Creates artificial urgency or pressure",
            patterns = listOf(
                Pattern.compile("\\b(?:immediately|urgent|within\\s+24\\s+hours|action\\s+required|last\\s+chance)\\b", Pattern.CASE_INSENSITIVE)
            )
        )
    )

    fun evaluateLocally(text: String): LocalEvaluationResult {
        val clean = text.trim()
        if (clean.isBlank()) {
            return LocalEvaluationResult(
                riskScore = 0,
                classification = "SAFE",
                category = "Safe / Normal",
                indicators = emptyList(),
                recommendation = "No threat detected.",
                shouldQueryBackend = false,
                isSafe = true
            )
        }

        var totalScore = 0
        val matchedIndicators = mutableListOf<String>()
        val categoryScores = mutableMapOf<String, Int>()
        var hasCritical = false

        // Check protective context
        var isProtectiveWarning = false
        for (pattern in PROTECTIVE_PATTERNS) {
            if (pattern.matcher(clean).find()) {
                isProtectiveWarning = true
                break
            }
        }

        // Evaluate Rules
        for (rule in RULES) {
            var ruleHit = false
            for (p in rule.patterns) {
                if (p.matcher(clean).find()) {
                    ruleHit = true
                    break
                }
            }
            if (ruleHit) {
                totalScore += rule.weight
                matchedIndicators.add(rule.indicator)
                categoryScores[rule.category] = (categoryScores[rule.category] ?: 0) + rule.weight
                if (rule.isCritical) hasCritical = true
            }
        }

        // Compound rule boost
        if (hasCritical && matchedIndicators.size >= 2) {
            totalScore = maxOf(totalScore, 75)
        } else if (hasCritical && totalScore >= 50) {
            totalScore = maxOf(totalScore, 72)
        }

        // False positive deduction for protective bank messages
        if (isProtectiveWarning) {
            val hasLink = clean.contains("http://") || clean.contains("https://")
            val hasCollect = clean.contains("enter upi pin", ignoreCase = true)
            if (!hasLink && !hasCollect) {
                totalScore = maxOf(0, totalScore - 60)
                matchedIndicators.removeAll { it.contains("OTP", ignoreCase = true) }
            }
        }

        val finalScore = totalScore.coerceIn(0, 100)

        val classification = when {
            finalScore >= 70 -> "SCAM"
            finalScore >= 35 -> "SUSPICIOUS"
            else -> "SAFE"
        }

        val primaryCategory = if (classification == "SAFE") {
            "Safe / Normal"
        } else {
            categoryScores.maxByOrNull { it.value }?.key ?: "Other"
        }

        val recommendation = when (primaryCategory) {
            "OTP & Credential Phishing" -> "Do NOT share your OTP or password with anyone. Official banks never ask for your code."
            "UPI Scam" -> "Do NOT enter your UPI PIN. You NEVER need to enter a PIN to receive money."
            "Account Suspension Scam", "Fake KYC" -> "Do NOT click any link in this message. Call your bank branch directly."
            "Government Impersonation" -> "Do NOT pay or call numbers in this message. Utility boards do not disconnect power via SMS."
            "Lottery / Prize Scam" -> "Do NOT pay any processing fee. Legitimate lotteries never ask for upfront payment."
            "Remote Access Scam" -> "Do NOT install AnyDesk or screen-sharing apps. The sender may steal money from your bank."
            "Delivery Scam" -> "Do NOT click the link or pay redelivery fees. Check your order in the official shopping app."
            else -> if (finalScore >= 35) "Be cautious. Verify the sender through official channels before acting." else "This message appears normal."
        }

        // Privacy principle: only messages with non-zero risk or suspicious indicators are sent for AI analysis
        val shouldQueryBackend = finalScore >= 35

        return LocalEvaluationResult(
            riskScore = finalScore,
            classification = classification,
            category = primaryCategory,
            indicators = matchedIndicators,
            recommendation = recommendation,
            shouldQueryBackend = shouldQueryBackend,
            isSafe = finalScore < 35
        )
    }
}
