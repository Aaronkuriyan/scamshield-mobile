# SCAMSHIELD — System Architecture

## 1. High-Level Concept
SCAMSHIELD is a real-time, privacy-first cybersecurity guardian for Android designed specifically for elderly and non-technical users. It operates quietly in the background without requiring users to copy and paste suspicious messages into an analyzer.

```
+-------------------------------------------------------------+
|                     INCOMING MESSAGE                        |
|        (SMS, WhatsApp, Telegram, Banking Alerts)            |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|          ANDROID NotificationListenerService                |
|  - Deduplication cache (5-minute window)                    |
|  - Package validation (ignores system apps / self)         |
|  - Extracts notification title and body safely              |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|              ON-DEVICE LOCAL SCAM FILTER                    |
|  - Protective pattern checks ("Never share OTP" -> SAFE)    |
|  - Weighted heuristic scoring                               |
+-------------------------------------------------------------+
               /                             \
        Score < 35                     Score >= 35
              /                               \
             v                                 v
   +------------------+             +----------------------+
   |  DROP / IGNORE   |             |   BACKEND AI ENGINE  |
   | (Privacy First - |             |  (Groq LLaMA 3.3 70B |
   |  stays on phone) |             |   or Offline Fallback)
   +------------------+             +----------------------+
                                               |
                                               v
                                    +----------------------+
                                    |     RISK SCORE       |
                                    |  (0 - 100 Scale)     |
                                    +----------------------+
                                               |
                                               v
                          +------------------------------------------+
                          |             ALERT PIPELINE               |
                          |  >= 70: Heads-Up Notification            |
                          |  >= 70: Spoken Voice Warning (TTS)       |
                          |  Persist to Room DB (Anonymized metadata)|
                          +------------------------------------------+
```

## 2. Key Architecture Principles

1. **Zero-Copy UX**: The user never needs to manually copy or paste text. Protection is proactive and automatic upon notification arrival.
2. **Privacy-by-Default Edge Filtering**: Harmless messages ("Good morning", "Where are you?") never leave the smartphone. Only messages containing non-zero suspicious heuristic triggers are sent to the AI backend.
3. **Dual-Engine Redundancy**: If internet is down or the backend is unreachable, the local heuristic engine on the Android device takes over seamlessly.
4. **Non-Technical Elderly Accessibility**: High-contrast dark themes, large touch targets, bold warning banners, and plain-language guidance ("Do NOT click the link. Do NOT share your OTP.").
