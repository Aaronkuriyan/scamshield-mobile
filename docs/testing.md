# SCAMSHIELD — Testing & Hackathon Demo Guide

## 1. Automated Backend Test Suite
The backend contains 15 unit and integration tests covering heuristic rules, false-positive prevention, API endpoints, and batch processing.

### Running Backend Tests:
```bash
cd backend
.venv\Scripts\pytest -v
```

### Verified Test Cases:
- `test_safe_normal_conversation`: Daily family chats score 0 and are marked SAFE.
- `test_protective_bank_warning_false_positive_prevention`: Official bank security warnings ("Never share your OTP with anyone") are properly detected as SAFE without false alarms.
- `test_otp_phishing_scam`: Account suspension with OTP request scores >= 70 (SCAM).
- `test_upi_collect_fraud`: Google Pay / PhonePe collect fraud scores >= 70 (SCAM).
- `test_electricity_cutoff_scam`: Disconnection threats score >= 70 (SCAM).
- `test_lottery_kbc_scam`: Fake prize with upfront processing fee scores >= 70 (SCAM).
- `test_remote_access_anydesk_scam`: Requests to install screen-sharing apps score >= 70 (SCAM).
- `test_api_root`, `test_health_check`, `test_categories_endpoint`, `test_analyze_scam_endpoint`, `test_analyze_safe_endpoint`, `test_batch_analyze_endpoint`, `test_history_audit_endpoint`.

---

## 2. Android On-Device Unit Tests
Unit tests in `android/app/src/test/java/com/scamshield/app/LocalScamFilterTest.kt` evaluate the Kotlin `LocalScamFilter` directly on the JVM without needing an emulator:
- Verifies exact matching of Indian scam vectors (UPI, KBC, electricity, OTP).
- Verifies protective pattern filtering.

---

## 3. How to Demonstrate at a Live Hackathon

### Step 1: Launch Backend
```bash
cd backend
.venv\Scripts\python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### Step 2: Open App & Grant Permission
1. Launch SCAMSHIELD on your Android device or emulator.
2. Tap **ENABLE PROTECTION** to grant Notification Access.
3. Observe the green shield status: **🛡️ PROTECTION ACTIVE**.

### Step 3: Trigger Live Demo Alert (One-Tap In-App Simulator)
1. Tap **SIMULATE LIVE DEMO TEST** on the Dashboard.
2. Choose **"Bank Account Block & OTP Phishing"**.
3. **Observe the result immediately**:
   - A prominent heads-up alert notification arrives: `⚠️ SCAM ALERT (94/100)`.
   - Android Text-to-Speech speaks aloud: *"Warning. This message may be a scam. Do NOT share your OTP or password..."*
   - Dashboard increments **Messages Checked** and **Threats Blocked**.
   - Tap **VIEW THREAT HISTORY** to inspect the recorded threat without exposing private message content.
4. Choose **"Normal Family Conversation"**:
   - Observe that no warning is triggered and the message is safely handled.
