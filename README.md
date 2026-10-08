# SCAMSHIELD 🛡️

> **AI-Powered Real-Time Android Scam Message Protection for Elderly & Non-Technical Users**

[![Android](https://img.shields.io/badge/Android-Native%20Kotlin-brightgreen?logo=android)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-blue?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-FastAPI%20%7C%20Python%203.14-009688?logo=fastapi)](https://fastapi.tiangolo.com/)
[![AI](https://img.shields.io/badge/AI-Groq%20Cloud%20LLaMA%203.3%2070B-orange)](https://groq.com/)
[![Privacy](https://img.shields.io/badge/Privacy-Zero--Surveillance%20Edge%20Filter-blueviolet)]()

---

## 📌 The Problem
Smartphone scams—including fake KYC deactivation threats, UPI collect request fraud, electricity disconnection extortion, and OTP phishing—disproportionately target elderly and less technically confident users. Existing security tools fail them because:
1. **They require manual copy-pasting**: Elderly users often panic and click dangerous links or share OTPs before opening an analyzer app.
2. **They flood users with complex jargon**: Technical vulnerability scores confuse non-technical users rather than protecting them.
3. **They compromise privacy**: Many tools record all personal messages or monitor private communications invasively.

---

## 💡 The Solution: SCAMSHIELD
**SCAMSHIELD** is a production-grade, native Android cybersecurity companion that operates automatically in the background.

```
USER RECEIVES A MESSAGE (SMS, WhatsApp, Banking, Telegram)
                           ↓
    ANDROID NOTIFICATION LISTENER INTERCEPTS NOTIFICATION
                           ↓
             ON-DEVICE LOCAL SCAM FILTER
                           ↓
     Is it suspicious? ─── NO ───→ Silently Drop (Stays on Device)
            │
           YES
            ↓
  DEEPER AI BACKEND ANALYSIS (Groq LLaMA 3.3 70B)
            ↓
  RISK SCORE GENERATION (0–100 Scale)
            ↓
  HEADS-UP ALERT NOTIFICATION + OPTIONAL VOICE WARNING (TTS)
```

**The user never needs to copy, paste, or open the app manually.** Protection happens automatically before harmful actions are taken.

---

## ✨ Key Features

- **Automatic Real-Time Interception**: Uses Android's native `NotificationListenerService` to detect incoming message threats instantly.
- **Privacy-First Edge Filtering**: Safe, normal conversations ("Good morning", "Dinner ready") never leave the user's phone. Only suspicious triggers undergo deeper verification.
- **Sub-Second AI Detection**: Cloud inference powered by **Groq LLaMA 3.3 70B** for rapid contextual analysis.
- **100% Offline Redundancy**: If internet is down or the backend is unreachable, the on-device weighted heuristic engine automatically protects the user without crashing.
- **False-Positive Prevention**: Context-aware protective rules distinguish between scammers asking for OTPs vs. bank advisories warning users never to share OTPs.
- **Elderly-First Accessibility**:
  - High-contrast typography and large touch targets.
  - Plain-language advice: *"Do NOT click the link. Do NOT share your OTP. Do NOT send money."*
  - Spoken Voice Warnings via Android **Text-to-Speech (TTS)**.
- **Opt-in Family Protection**: Enables designated family guardians to be alerted when a parent receives a high-risk scam, with zero invasive message monitoring.
- **One-Tap Hackathon Demo Simulator**: Test simulated Indian and global scam vectors with one click from the dashboard.

---

## 🏗️ Architecture & Technology Stack

### Android Client
- **Language**: Kotlin (1.9.24)
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: Clean Architecture / MVVM with Kotlin Coroutines & Flow
- **Background Interception**: Android `NotificationListenerService`
- **Networking**: Retrofit 2 + OkHttp 3 with dynamic host configuration
- **Local Persistence**: Room Database (stores only anonymized metadata)
- **Audio Accessibility**: Android `TextToSpeech` API

### Backend & AI Engine
- **Framework**: FastAPI (Python 3.14) + Uvicorn + Pydantic v2
- **AI Inference**: Groq Cloud SDK (`llama-3.3-70b-versatile`)
- **Heuristic Engine**: Rule-based weighted scoring engine with regex patterns tailored for Indian banking, UPI, KYC, and courier fraud
- **Testing**: PyTest with full coverage for endpoints, heuristics, and batch analysis

---

## 📁 Project Structure

```
SCAMSHIELD-MOBILE/
├── android/                             # Native Android Kotlin Application
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml      # Permissions & NotificationListenerService
│   │   │   ├── java/com/scamshield/app/
│   │   │   │   ├── ScamShieldApplication.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/          # Room DB (Entities, DAOs)
│   │   │   │   │   ├── model/          # API Request/Response Data Classes
│   │   │   │   │   ├── network/        # Retrofit Client & Endpoints
│   │   │   │   │   └── repository/     # Dual-engine ScanRepository
│   │   │   │   ├── engine/
│   │   │   │   │   └── LocalScamFilter.kt # On-device Edge Filter
│   │   │   │   ├── service/
│   │   │   │   │   ├── ScamNotificationListenerService.kt
│   │   │   │   │   ├── NotificationHelper.kt
│   │   │   │   │   └── TextToSpeechHelper.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/     # NavGraph & Route definitions
│   │   │   │   │   ├── screens/        # Compose Screens (Dashboard, ThreatDetail, etc.)
│   │   │   │   │   └── theme/          # Accessible Typography & High-Contrast Colors
│   │   │   │   └── util/
│   │   │   │       └── DemoSimulator.kt# Live hackathon test case runner
│   │   │   └── res/                    # Icons, values, strings, XML rules
│   │   ├── src/test/                   # Android JVM Unit Tests
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── backend/                             # Python FastAPI AI Detection Server
│   ├── app/
│   │   ├── main.py                     # App entry point & middleware
│   │   ├── api/routes.py               # REST Endpoints (/analyze, /health, /history)
│   │   ├── detection/
│   │   │   ├── heuristics.py           # Weighted rule scoring engine
│   │   │   └── patterns.py             # Regex threat indicators & protective patterns
│   │   ├── services/
│   │   │   └── ai_service.py           # Groq LLaMA 3.3 70B integration
│   │   ├── schemas/analysis.py         # Pydantic v2 contract models
│   │   └── config/settings.py          # Environment settings
│   ├── tests/                          # 15 Comprehensive PyTest unit tests
│   ├── requirements.txt
│   └── .env.example
│
├── docs/                                # Technical Documentation
│   ├── architecture.md
│   ├── android.md
│   ├── backend.md
│   ├── api.md
│   ├── privacy.md
│   └── testing.md
│
├── .gitignore                           # Comprehensive gitignore for secrets & builds
└── README.md
```

---

## 🚀 Quickstart & Local Development

### 1. Start Backend API
```bash
cd backend
python -m venv .venv

# On Windows:
.venv\Scripts\activate
# On Linux/macOS:
source .venv/bin/activate

pip install -r requirements.txt
cp .env.example .env
# Edit .env and optionally add your GROQ_API_KEY

uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
Interactive API docs are available at `http://localhost:8000/docs`.

### 2. Run Backend Tests
```bash
pytest -v
```
All 15 automated test cases verify risk scoring, false-positive prevention, and batch operations.

### 3. Open & Run Android Project
1. Open the [`android/`](file:///c:/CODING/SCAMSHIELD-MOBILE/android) directory in Android Studio (Jellyfish or newer with JDK 17/21).
2. Sync Gradle dependencies.
3. Run on an Android Emulator (API 26+) or a connected physical Android device.
4. Launch the app, tap **ENABLE PROTECTION**, and allow **Notification Access** in Android Settings.
5. Tap **SIMULATE LIVE DEMO TEST** on the dashboard to test the real-time detection pipeline end-to-end!

---

## 🔒 Security & Privacy Guarantees
- **No Hardcoded Keys**: API keys and secrets are loaded strictly from environment variables.
- **Privacy Audit**: Full message contents are **never permanently stored** on the server or in the client database.
- **Zero Surveillance**: Family Protection notifies guardians of risk status only—never private messages.

---

## 👥 Contributors & Collaboration
- **Lead Developer**: [@Aaronkuriyan](https://github.com/Aaronkuriyan)
- **Collaborator**: [@zer0neo](https://github.com/zer0neo)

---

## 📄 License
This project is licensed under the Apache 2.0 License.
