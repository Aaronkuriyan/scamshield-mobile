import re

# Protective / Legitimate Warning Patterns (Reduces false positives!)
PROTECTIVE_PATTERNS = [
    r"do\s+not\s+share\s+(?:your\s+)?(?:otp|pin|password|cvv)",
    r"never\s+share\s+(?:your\s+)?(?:otp|pin|password|cvv)",
    r"bank\s+never\s+asks\s+for\s+(?:otp|pin|password|details)",
    r"beware\s+of\s+(?:fraud|scams|fake)",
    r"do\s+not\s+disclose",
    r"keep\s+(?:it\s+)?confidential",
    r"if\s+not\s+requested\s+by\s+you",
    r"for\s+fraud\s+reporting\s+call",
    r"do\s+not\s+share\b",
]

# High-Risk Threat Indicators with Refined Weights & Severity
SCAM_RULES = [
    {
        "id": "otp_request",
        "category": "OTP & Credential Phishing",
        "weight": 55,
        "is_critical": True,
        "indicator": "Requests or demands sharing an OTP or verification code",
        "patterns": [
            r"(?:share|send|enter|give|provide)\s+(?:your\s+)?(?:otp|one\s+time\s+password|verification\s+code)",
            r"otp\s+is\s+required\s+to\s+(?:claim|receive|verify|unblock)",
            r"forward\s+this\s+(?:sms|otp|code)",
            r"verify\s+(?:your\s+)?otp",
        ]
    },
    {
        "id": "upi_pin_fraud",
        "category": "UPI Scam",
        "weight": 70,
        "is_critical": True,
        "indicator": "Tricks user into entering UPI PIN or approving collect request to receive money",
        "patterns": [
            r"enter\s+(?:upi\s+)?pin\s+to\s+receive",
            r"(?:approve|accept)\s+(?:collect\s+)?request.*(?:to\s+receive|reward|cashback|refund)",
            r"scan\s+(?:this\s+)?qr\s+code\s+to\s+receive\s+money",
            r"send\s+(?:₹|rs\.?|inr)\s*1\s+to\s+verify",
            r"collect\s+request\s+of\s+(?:₹|rs\.?)",
        ]
    },
    {
        "id": "account_suspension",
        "category": "Account Suspension Scam",
        "weight": 40,
        "is_critical": False,
        "indicator": "Threatens immediate account blockage or suspension",
        "patterns": [
            r"account\s+(?:will\s+be\s+)?(?:blocked|suspended|deactivated|closed)\s*(?:today|immediately|within)?",
            r"(?:sbi|hdfc|icici|axis|pnb|bank|card|netbanking|debit\s+card)\s+(?:account\s+)?(?:has\s+been|is)\s*(?:blocked|suspended)",
            r"sim\s+card\s+will\s+be\s+deactivated",
            r"pan\s+(?:card\s+)?not\s+linked",
        ]
    },
    {
        "id": "fake_kyc",
        "category": "Fake KYC",
        "weight": 45,
        "is_critical": False,
        "indicator": "Urgent demand to complete or update KYC via external link",
        "patterns": [
            r"(?:update|complete|verify)\s+your\s+kyc",
            r"kyc\s+(?:has\s+)?expired",
            r"mandatory\s+kyc\s+verification",
            r"link\s+aadhaar\s+(?:with|to)\s+(?:bank|pan)",
        ]
    },
    {
        "id": "utility_cutoff",
        "category": "Government Impersonation",
        "weight": 60,
        "is_critical": True,
        "indicator": "Electricity or utility disconnection threat due to fake unpaid bill",
        "patterns": [
            r"electricity\s+(?:power\s+)?will\s+be\s+(?:disconnected|cut\s*off)",
            r"power\s+cut\s+(?:tonight|today|at)",
            r"bill\s+was\s+not\s+updated",
            r"contact\s+electricity\s+officer",
        ]
    },
    {
        "id": "lottery_prize",
        "category": "Lottery / Prize Scam",
        "weight": 55,
        "is_critical": True,
        "indicator": "Unrealistic lottery, reward, or cashback claim requiring upfront fee or action",
        "patterns": [
            r"(?:congratulations|hurry)\W.*won\s+(?:₹|rs\.?|inr|\$)\s*[\d,]+",
            r"won\s+(?:a\s+)?(?:car|iphone|cash|lottery|prize)",
            r"kbc\s+(?:lottery|lucky\s+draw)",
            r"claim\s+(?:your\s+)?(?:cashback|prize|reward)\s+(?:now|here)",
            r"(?:processing|registration|release)\s+fee\s+of\s+(?:₹|rs\.?)",
            r"pay\s+(?:₹|rs\.?)\s*[\d,]+\s+(?:processing|release|claim|transfer)\s+fee",
        ]
    },
    {
        "id": "job_scam",
        "category": "Job Scam",
        "weight": 40,
        "is_critical": False,
        "indicator": "Work from home or task scam demanding deposits or promising unrealistic daily earnings",
        "patterns": [
            r"earn\s+(?:₹|rs\.?)\s*[\d,]+\s*(?:daily|per\s+day)\s*(?:from\s+home)?",
            r"part\s+time\s+job\s+offer.*like\s+(?:youtube|google)",
            r"simple\s+online\s+task\s+to\s+earn",
            r"work\s+from\s+home\s+daily\s+payment",
        ]
    },
    {
        "id": "delivery_fraud",
        "category": "Delivery Scam",
        "weight": 35,
        "is_critical": False,
        "indicator": "Fake courier package delivery failure requesting payment or address update",
        "patterns": [
            r"(?:parcel|package|delivery)\s+(?:could\s+not|failed|pending)",
            r"pay\s+(?:₹|rs\.?)\s*[\d,]+\s*(?:delivery|tracking|reschedule)\s+fee",
            r"update\s+(?:delivery\s+)?address\s+link",
            r"indiapost.*undelivered",
        ]
    },
    {
        "id": "suspicious_links",
        "category": "Malicious Link",
        "weight": 30,
        "is_critical": False,
        "indicator": "Contains URL shortener, raw IP address, or suspicious domain",
        "patterns": [
            r"https?://(?:bit\.ly|tinyurl\.com|t\.co|is\.gd|cutt\.ly|rb\.gy|v\.gd)/\S+",
            r"https?://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}(?::\d+)?/\S*",
            r"https?://\S+\.(?:xyz|top|work|click|club|loan|kim|buzz|gq|cf|tk)/\S*",
            r"https?://\S*(?:apk|download|verify-kyc|claim-reward|sbi-update|bank-login|unblock)\S*",
        ]
    },
    {
        "id": "remote_access",
        "category": "Remote Access Scam",
        "weight": 70,
        "is_critical": True,
        "indicator": "Instructs installing remote screen-sharing tools to troubleshoot",
        "patterns": [
            r"(?:install|download)\s+(?:anydesk|teamviewer|quicksupport|rustdesk)",
            r"share\s+(?:9\s*digit|your)\s+code\s+for\s+support",
            r"remote\s+access\s+app",
        ]
    },
    {
        "id": "urgency_pressure",
        "category": "Social Engineering",
        "weight": 20,
        "is_critical": False,
        "indicator": "High psychological pressure to act immediately",
        "patterns": [
            r"\b(?:immediately|urgent|within\s+24\s+hours|action\s+required|last\s+chance|final\s+warning|tonight|today)\b",
            r"otherwise\s+your\s+service\s+will\s+be\s+terminated",
        ]
    }
]
