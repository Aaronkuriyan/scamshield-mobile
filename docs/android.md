# SCAMSHIELD — Android Native Architecture

## Overview
SCAMSHIELD is built natively using **Kotlin**, **Jetpack Compose**, **Material 3**, and Android SDK APIs (Min SDK: 26, Target SDK: 34).

## Core Modules & Components

### 1. `ScamNotificationListenerService`
- Extends Android `NotificationListenerService`.
- Registered with permission `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`.
- Automatically invoked when new notifications are posted.
- **Safety checks**:
  - Drops self notifications (`com.scamshield.app`) to avoid infinite loops.
  - Drops system UI, Google Play download manager, and battery alerts.
  - Implements a 5-minute memory cache to prevent duplicate alerts for the same notification.

### 2. `LocalScamFilter`
- Lightweight on-device pattern matching and weighted heuristics.
- Fast, battery-efficient, and operates completely offline.
- Suppresses false positives from legitimate bank security advisories.

### 3. `ScanRepository`
- Coordinates between `LocalScamFilter`, `NetworkClient` (Retrofit), and `ScamDatabase` (Room).
- Handles offline fallback: if the backend network call fails or times out, the local heuristic result is used to protect the user without interruption.

### 4. `NotificationHelper` & `TextToSpeechHelper`
- High-priority Heads-up Notification with sound, vibration, and alert red color.
- Optional spoken voice warning using Android `TextToSpeech` tuned for elderly clarity.

### 5. Jetpack Compose UI
- `SplashScreen`: Brand animation and automated permission verification.
- `OnboardingScreen`: Clear explanation of automated protection.
- `PermissionScreen`: Guided setup with direct intent link to Notification Access.
- `DashboardScreen`: Protection status, metrics, recent threats, and one-tap live demo simulator.
- `ThreatDetailScreen`: Bold warning card, indicators list, and actionable safety steps.
- `HistoryScreen`: Chronological list of past threat scans.
- `SafetyGuideScreen`: Educational cards covering OTP, UPI, Electricity, KYC, and Job scams.
- `SettingsScreen`: Toggles for protection, voice warnings, custom backend host, and privacy controls.
- `FamilyProtectionScreen`: Opt-in guardian contact alert setup.
