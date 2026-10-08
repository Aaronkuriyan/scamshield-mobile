import re
from typing import Dict, Any, List
from .patterns import SCAM_RULES, PROTECTIVE_PATTERNS
from ..schemas.analysis import ClassificationEnum, ScamCategoryEnum

class HeuristicEngine:
    def __init__(self, safe_max: int = 34, suspicious_threshold: int = 35, scam_threshold: int = 70):
        self.safe_max = safe_max
        self.suspicious_threshold = suspicious_threshold
        self.scam_threshold = scam_threshold

    def evaluate(self, text: str, sender: str = None) -> Dict[str, Any]:
        """
        Evaluates message text against weighted heuristics and protective rules.
        Returns a structured dictionary matching AnalyzeResponse fields.
        """
        clean_text = text.lower().strip()
        matched_indicators: List[str] = []
        categories_matched: Dict[str, int] = {}
        total_score = 0
        has_critical_rule = False

        # 1. Check for protective / educational warning context
        has_protective_warning = False
        for pat in PROTECTIVE_PATTERNS:
            if re.search(pat, clean_text, re.IGNORECASE):
                has_protective_warning = True
                break

        # 2. Check each scam rule
        for rule in SCAM_RULES:
            rule_matched = False
            for pat in rule["patterns"]:
                if re.search(pat, clean_text, re.IGNORECASE):
                    rule_matched = True
                    break

            if rule_matched:
                total_score += rule["weight"]
                matched_indicators.append(rule["indicator"])
                cat = rule["category"]
                categories_matched[cat] = categories_matched.get(cat, 0) + rule["weight"]
                if rule.get("is_critical", False):
                    has_critical_rule = True

        # 3. Compound Threat Boosters
        # If a message has a critical pattern (like lottery claim, electricity cut, or remote access)
        # and also asks for fee/action or creates urgency, boost score into clear SCAM territory
        if has_critical_rule and len(matched_indicators) >= 2:
            total_score = max(total_score, 75)
        elif has_critical_rule and total_score >= 50:
            total_score = max(total_score, 72)

        # 4. Contextual Adjustment for False Positive prevention
        # If the message is a bank advisory WARNING the user not to share OTPs,
        # but triggered an OTP pattern purely from the words:
        if has_protective_warning:
            # Check if there is an actual phishing link or demand
            has_link = bool(re.search(r"https?://\S+", clean_text))
            has_collect_request = "enter upi pin" in clean_text or "send 1" in clean_text or "processing fee" in clean_text
            
            if not has_link and not has_collect_request:
                # Legitimate security advisory!
                total_score = max(0, total_score - 60)
                matched_indicators = [ind for ind in matched_indicators if "sharing an OTP" not in ind]

        # Clamp score between 0 and 100
        score = max(0, min(100, total_score))

        # Determine Classification
        if score >= self.scam_threshold:
            classification = ClassificationEnum.SCAM
        elif score >= self.suspicious_threshold:
            classification = ClassificationEnum.SUSPICIOUS
        else:
            classification = ClassificationEnum.SAFE

        # Determine Primary Category
        if classification == ClassificationEnum.SAFE:
            primary_category = ScamCategoryEnum.SAFE_NORMAL.value
        elif categories_matched:
            # Pick category with highest matched weight
            primary_category = max(categories_matched.items(), key=lambda x: x[1])[0]
        else:
            primary_category = ScamCategoryEnum.OTHER.value

        # Calculate Confidence (0.60 to 0.98 based on score and indicator count)
        confidence = min(0.98, max(0.60, 0.5 + (len(matched_indicators) * 0.12) + (score / 250.0)))

        # Elderly-friendly plain-language recommendations
        recommendation = self._generate_recommendation(classification, primary_category)

        return {
            "risk_score": score,
            "classification": classification,
            "category": primary_category,
            "confidence": round(confidence, 2),
            "indicators": matched_indicators,
            "recommendation": recommendation,
            "is_safe": score <= self.safe_max,
            "engine": "local_heuristic"
        }

    def _generate_recommendation(self, classification: ClassificationEnum, category: str) -> str:
        if classification == ClassificationEnum.SAFE:
            return "This message appears normal. No scam indicators detected."

        if category == "OTP & Credential Phishing":
            return "Do NOT share your OTP or password with anyone. Official banks never ask for your code."
        elif category == "UPI Scam":
            return "Do NOT enter your UPI PIN. Remember: You NEVER need to enter a PIN to receive money."
        elif category == "Account Suspension Scam" or category == "Fake KYC":
            return "Do NOT click any link in this message. Call your bank branch directly using their official number."
        elif category == "Government Impersonation":
            return "Do NOT pay or call numbers listed in the message. Official utility boards do not cut power via SMS."
        elif category == "Lottery / Prize Scam":
            return "Do NOT send any advance processing fee. Legitimate lotteries never ask for upfront payment."
        elif category == "Remote Access Scam":
            return "Do NOT install AnyDesk or screen-sharing apps. The sender may take control of your phone and bank."
        elif category == "Delivery Scam":
            return "Do NOT click the tracking link or pay redelivery fees. Check your order directly in the official shopping app."
        elif classification == ClassificationEnum.SUSPICIOUS:
            return "Be cautious. Verify the sender's identity through official channels before sharing any information or clicking links."
        else:
            return "Do not click any links, do not share OTPs, and do not send money."

heuristic_engine = HeuristicEngine()
