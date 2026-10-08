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

        # Elderly-friendly plain-language recommendations and actionable guidance
        recommendation = self._generate_recommendation(classification, primary_category)
        what_to_do = self._generate_what_to_do(classification, primary_category)
        what_not_to_do = self._generate_what_not_to_do(classification, primary_category)

        return {
            "risk_score": score,
            "classification": classification,
            "category": primary_category,
            "confidence": round(confidence, 2),
            "indicators": matched_indicators,
            "what_to_do": what_to_do,
            "what_not_to_do": what_not_to_do,
            "recommendation": recommendation,
            "is_safe": score <= self.safe_max,
            "engine": "local_heuristic"
        }

    def _generate_what_to_do(self, classification: ClassificationEnum, category: str) -> list[str]:
        if classification == ClassificationEnum.SAFE:
            return [
                "You can continue normally without concern.",
                "No special action is required."
            ]
        
        guidance = {
            "OTP & Credential Phishing": [
                "Keep your OTP confidential; it is meant strictly for you.",
                "If you mistakenly shared a code, call your bank customer service immediately.",
                "Block and report the sender on your messaging app."
            ],
            "UPI Scam": [
                "Decline or ignore the collect request immediately.",
                "Check your account balance independently using your bank app.",
                "Report the incident on the National Cyber Crime Portal (1930 / cybercrime.gov.in)."
            ],
            "Account Suspension Scam": [
                "Contact your bank branch using the official phone number printed on your debit card.",
                "Check your account status directly via official netbanking.",
                "Ask a trusted family member or branch staff for assistance if unsure."
            ],
            "Fake KYC": [
                "Complete KYC updates only in person at your official bank branch or official app.",
                "Report fake KYC text messages to 1930 helpline.",
                "Delete the message after reporting."
            ],
            "Government Impersonation": [
                "Call the official electricity board or government office customer care number.",
                "Pay utility bills only through authorized municipal offices or official apps.",
                "Report impersonation SMS to local authorities."
            ],
            "Lottery / Prize Scam": [
                "Immediately delete and ignore this message.",
                "Block the sender's phone number.",
                "Remember: If you did not buy a ticket, you cannot win a prize."
            ],
            "Delivery Scam": [
                "Check your active package tracking directly in your shopping app (Amazon, Flipkart).",
                "Contact the official courier's verified customer helpline.",
                "Ignore tracking links sent via unsolicited SMS."
            ],
            "Remote Access Scam": [
                "Hang up the call immediately.",
                "Disconnect your phone from Wi-Fi/mobile data if any app was installed.",
                "Ask a knowledgeable family member or technician to check your device."
            ]
        }
        return guidance.get(category, [
            "Verify the message sender independently through verified official channels.",
            "Ask a trusted family member or call 1930 before taking any action.",
            "Report the suspicious message."
        ])

    def _generate_what_not_to_do(self, classification: ClassificationEnum, category: str) -> list[str]:
        if classification == ClassificationEnum.SAFE:
            return [
                "Never share passwords or OTPs with strangers even in normal conversations."
            ]

        guidance = {
            "OTP & Credential Phishing": [
                "Do NOT share your OTP, PIN, or CVV with anyone under any circumstances.",
                "Do NOT read verification codes aloud over phone calls.",
                "Do NOT forward this message to anyone."
            ],
            "UPI Scam": [
                "Do NOT enter your 4 or 6 digit UPI PIN to receive money.",
                "Do NOT scan QR codes sent by unknown buyers or callers.",
                "Do NOT approve any collect request on PhonePe, GPay, or Paytm."
            ],
            "Account Suspension Scam": [
                "Do NOT click any web link enclosed in this SMS.",
                "Do NOT enter your netbanking password or card details on linked websites.",
                "Do NOT call the unverified phone numbers listed in the message."
            ],
            "Fake KYC": [
                "Do NOT enter your Aadhaar or PAN number on external web links.",
                "Do NOT download unverified APK files or apps sent via SMS.",
                "Do NOT share OTPs for KYC verification over the phone."
            ],
            "Government Impersonation": [
                "Do NOT pay electricity or utility bills using private UPI handles.",
                "Do NOT panic or believe urgent disconnection threats over SMS.",
                "Do NOT call the personal mobile numbers mentioned in the message."
            ],
            "Lottery / Prize Scam": [
                "Do NOT pay any upfront registration, customs, or processing fees.",
                "Do NOT provide bank account details to claim non-existent prizes.",
                "Do NOT transfer funds to unknown individuals."
            ],
            "Delivery Scam": [
                "Do NOT click links to reschedule your delivery.",
                "Do NOT pay ₹5 or ₹10 re-delivery fees through unknown web portals.",
                "Do NOT provide card details on delivery tracking forms."
            ],
            "Remote Access Scam": [
                "Do NOT install AnyDesk, TeamViewer, RustDesk, or QuickSupport.",
                "Do NOT share the 9-digit code shown on screen-sharing apps.",
                "Do NOT open your banking app while someone is on a call with you."
            ]
        }
        return guidance.get(category, [
            "Do NOT click links in this message.",
            "Do NOT share your OTP, password, or banking credentials.",
            "Do NOT send money or approve payment requests."
        ])

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
