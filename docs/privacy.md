# SCAMSHIELD — Privacy & Data Protection Architecture

Privacy is a non-negotiable core requirement for SCAMSHIELD. When protecting users from SMS, WhatsApp, and messaging scams, the application handles deeply sensitive personal communications.

## 1. Zero-Surveillance Architecture
- **No Third-Party Access**: SCAMSHIELD does NOT access the private databases or message histories of WhatsApp, Telegram, or banking apps.
- **Notification-Only Surface**: SCAMSHIELD inspects only the temporary system notification stream provided explicitly by Android's `NotificationListenerService`.

## 2. On-Device Edge Filtering (Privacy Barrier)
- Before any data touches the network, it must pass through the on-device `LocalScamFilter`.
- If a message is a normal daily conversation ("Good morning", "Dinner is ready", "Are we meeting at 5?"), the local risk score is 0.
- **Harmless messages are immediately dropped on the phone.** They are **never** transmitted to the backend or AI provider.

## 3. Ephemeral AI Inference
- For messages that trigger suspicious heuristic patterns (e.g. OTP requests, fake bank suspensions), only the snippet is sent over encrypted HTTPS to the analysis engine.
- The AI inference engine does not retain or store training data from user inputs.

## 4. Anonymized Local Storage
- Local scan history stored in Room database preserves only safety metadata:
  - Timestamp
  - Risk score
  - Classification (SAFE / SUSPICIOUS / SCAM)
  - Threat category
  - Matched indicators
  - Source app package name
- Raw personal message text is **never permanently stored** in the database.
- Users can wipe all local history with a single tap in the Settings screen.

## 5. Non-Invasive Family Protection
- In the opt-in Family Protection mode, family members are **never shown message contents**.
- They only receive an emergency notification: *"Your parent received a potentially fraudulent message."*
