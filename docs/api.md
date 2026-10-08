# SCAMSHIELD — API Reference

## Base URL
- Local: `http://localhost:8000`
- Android Emulator: `http://10.0.2.2:8000`
- Interactive Swagger UI: `http://localhost:8000/docs`

---

### 1. `POST /api/analyze`
Analyzes a message snippet for scams, phishing, or financial fraud indicators.

#### Request Body
```json
{
  "content": "Dear Customer, your SBI bank account has been blocked today. Click http://bit.ly/sbi-unblock immediately and enter your OTP to verify KYC.",
  "sender": "+91 9876543210",
  "package_name": "com.google.android.apps.messaging",
  "metadata": {}
}
```

#### Response Body (`200 OK`)
```json
{
  "risk_score": 94,
  "classification": "SCAM",
  "category": "OTP & Credential Phishing",
  "confidence": 0.96,
  "indicators": [
    "Requests or demands sharing an OTP or verification code",
    "Contains URL shortener or suspicious domain",
    "Threatens immediate account blockage or suspension",
    "High psychological pressure to act immediately"
  ],
  "recommendation": "Do NOT share your OTP or password with anyone. Official banks never ask for your code.",
  "is_safe": false,
  "analyzed_at": "2026-10-08T18:15:20.123456Z",
  "engine": "groq_ai"
}
```

---

### 2. `POST /api/analyze/batch`
Processes up to 25 message snippets in a single request.

#### Request Body
```json
{
  "messages": [
    { "content": "Good morning Dad, did you take your pills?" },
    { "content": "Congratulations! You won Rs 25,00,000 in KBC Lucky Draw. Pay Rs 5,000 fee." }
  ]
}
```

#### Response Body (`200 OK`)
```json
{
  "results": [
    {
      "risk_score": 0,
      "classification": "SAFE",
      "category": "Safe / Normal",
      "confidence": 0.6,
      "indicators": [],
      "recommendation": "This message appears normal. No scam indicators detected.",
      "is_safe": true,
      "engine": "local_heuristic"
    },
    {
      "risk_score": 75,
      "classification": "SCAM",
      "category": "Lottery / Prize Scam",
      "confidence": 0.94,
      "indicators": [
        "Unrealistic lottery, reward, or cashback claim requiring upfront fee or action"
      ],
      "recommendation": "Do NOT send any advance processing fee. Legitimate lotteries never ask for upfront payment.",
      "is_safe": false,
      "engine": "local_heuristic"
    }
  ],
  "total_processed": 2
}
```

---

### 3. `GET /api/health`
Returns server status and AI inference engine availability.

#### Response Body (`200 OK`)
```json
{
  "status": "operational",
  "app": "SCAMSHIELD Backend",
  "version": "1.0.0",
  "ai_available": true,
  "ai_provider": "Groq (LLaMA 3.3 70B)",
  "ai_model": "llama-3.3-70b-versatile",
  "timestamp": "2026-10-08T18:15:20.123456Z"
}
```

---

### 4. `GET /api/categories`
Returns all supported threat categories, descriptions, and examples.

---

### 5. `GET /api/history`
Returns anonymized audit log metadata of recent scans (no raw message text stored).
