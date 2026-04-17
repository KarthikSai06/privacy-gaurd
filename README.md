<div align="center">

<img src="app/src/main/res/drawable/ic_launcher.xml" width="120" alt="PrivacyGuard Logo"/>

# 🛡️ PrivacyGuard

### *Your Personal Privacy Sentinel for Android*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![API](https://img.shields.io/badge/Min%20API-26%20(Oreo)-orange?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=for-the-badge)]()

**PrivacyGuard is a real-time, on-device Android privacy monitoring application that detects microphone abuse, keyloggers, suspicious background activity, and hidden app co-activation patterns — without ever sending your data to the cloud.**

[Features](#-features) • [Problem Statement](#-problem-statement) • [Architecture](#-architecture) • [Getting Started](#-getting-started) • [Roadmap](#-roadmap)

</div>

---

## 🚨 Problem Statement

In today's mobile ecosystem, **your privacy is constantly under attack** — and most of the time, you don't even know it.

Consider these alarming realities:

- 📱 **Thousands of apps request microphone access** but use it silently in the background, far beyond the scope the user consented to.
- ⌨️ **Keyloggers disguised as keyboards or assistants** secretly capture every password, message, and search query you type.
- 🌙 **Apps secretly communicate with remote servers at 3 AM**, exfiltrating your personal data while you sleep.
- 🔗 **Hidden inter-app communication chains** allow one malicious app to trigger data collection in another, evading simple permission checks.

Android's built-in permission model is a **passive gatekeeper** — it asks once during installation and then largely steps aside. There is no native system that continuously monitors if granted permissions are being abused, detects behavioral anomalies, or warns users about coordinated multi-app tracking strategies.

> **PrivacyGuard was built to fill this gap.** It acts as a continuous, intelligent watchdog that monitors these threat vectors in real-time on your device, stores all findings locally, and presents them in an easy-to-understand dashboard — putting privacy control back in your hands.

---

## ✨ Features

### 🎤 1. Microphone Usage Tracking
Leverages Android's `AppOpsManager` to continuously detect and log every microphone access event on the device.
- Logs **which app** accessed the mic, **when**, and for **how long**
- Identifies apps accessing the mic in the background when the screen is off
- Visualized in a dedicated chart-based screen for quick analysis

### ⌨️ 2. Keylogger Detection via Accessibility Service
Uses a custom `PrivacyAccessibilityService` to analyze app interaction patterns.
- Detects unusually high frequencies of text-field interactions — a hallmark of keylogging behavior
- Assigns a **suspicion score** to apps based on their input-capture patterns
- Flags apps that intercept text changes across other app windows

### 🌙 3. Suspicious Night-Time Activity Monitoring
Connects to Android's `UsageStatsManager` to track app behavior during off-hours.
- Scans for apps that **wake up between 12 AM – 6 AM** without user interaction
- Detects background processes that launch silently during sleep hours
- Highlights which apps consistently run overnight and how much resource they consume

### 🔗 4. App Co-Activation Pattern Detection (Trigger Map)
Investigates hidden causal relationships between apps on your device.
- Identifies when launching **App A** mysteriously triggers **App B** to start
- Detects apps that act as silent proxies or data-sharing partners for other apps
- Presented as an interactive **Trigger Map** screen to visually expose hidden links

### 📊 5. Real-Time Privacy Dashboard
A rich, dark-themed dashboard giving an instant overview of all threat categories.
- Built with **MPAndroidChart** for smooth, interactive data visualizations
- Aggregated threat summaries across all four monitoring domains
- Color-coded risk indicators for instant threat assessment

### ⚙️ 6. Always-On Background Engine
A robust background scanning engine that works efficiently without draining the battery.
- Powered by **Android WorkManager** for reliable, battery-optimized background scanning
- `BootReceiver` ensures protection restarts automatically after phone reboot
- All data processed and stored 100% locally via **Room Database** — nothing leaves your device

---

## 🏗️ Architecture

PrivacyGuard is built on **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns.

```
PrivacyGuard/
│
├── 📦 data/
│   ├── db/
│   │   ├── entities/          # Room DB Entities (MicUsage, NightActivity, etc.)
│   │   ├── dao/               # Data Access Objects for DB operations
│   │   └── AppDatabase.kt     # Room Database instance
│   └── repository/            # Repository layer (abstracts data sources)
│
├── 🧠 di/
│   ├── AppModule.kt           # Hilt DI module for app-level deps
│   └── DatabaseModule.kt      # Hilt DI module for Room DB
│
├── 🔔 receiver/
│   └── BootReceiver.kt        # Restarts services on device boot
│
├── 🔧 service/
│   └── PrivacyAccessibilityService.kt  # Keylogger detection service
│
├── 🖥️ ui/
│   ├── screens/
│   │   ├── dashboard/         # Main summary screen
│   │   ├── micusage/          # Microphone usage logs & charts
│   │   ├── keylogger/         # Keylogger detection alerts
│   │   ├── nightactivity/     # Overnight background app activity
│   │   ├── triggermap/        # App co-activation trigger map
│   │   ├── onboarding/        # First-run permissions setup
│   │   └── settings/          # App settings & preferences
│   ├── components/            # Reusable Composable components
│   └── theme/                 # Color, Typography, Theme definitions
│
├── 🗺️ navigation/
│   └── AppNavigation.kt       # Compose Navigation graph
│
└── ⚙️ worker/
    └── PrivacyScanWorker.kt   # WorkManager background scan job
```

### Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Architecture** | MVVM + Repository Pattern |
| **DI** | Hilt |
| **Local DB** | Room (SQLite) |
| **Background Work** | WorkManager |
| **Charts** | MPAndroidChart |
| **Privacy APIs** | AppOpsManager, UsageStatsManager, AccessibilityService |
| **Min Android** | API 26 (Android 8.0 Oreo) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK API 26+
- A physical Android device (recommended) or API 26+ emulator
- Java 11+ / Kotlin 1.9+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/KarthikSai06/privacy-gaurd.git
   cd privacy-gaurd
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select `File > Open` and navigate to the cloned folder.

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or simply press **Run ▶️** in Android Studio.

4. **Grant Required Permissions**
   On first launch, the onboarding screen will guide you to grant:
   - ✅ Usage Stats Access
   - ✅ Accessibility Service
   - ✅ Notification Permission

---

## 🔒 Privacy Commitment

> **PrivacyGuard practices what it preaches.**

- ✅ **Zero cloud dependency** — All analysis, logs, and data live exclusively on your device.
- ✅ **No analytics or telemetry** — We don't collect any usage data, crash reports, or identifiers.
- ✅ **No internet permission** — PrivacyGuard does not hold the `INTERNET` permission in its manifest.
- ✅ **Open source** — The entire codebase is available for audit.

---

## 🗺️ Roadmap

- [ ] 🤖 **On-Device ML Model** — Replace heuristic rules with a TensorFlow Lite anomaly detection model trained on your personal usage patterns.
- [ ] 📡 **Network Traffic Monitoring** — Use `VpnService` to detect apps silently exfiltrating data to tracking servers.
- [ ] 📸 **Camera & Location Abuse Tracking** — Extend `AppOpsManager` monitoring to camera and GPS access events.
- [ ] 🔔 **Instant Push Alerts** — Real-time high-priority notifications the moment a threat is detected.
- [ ] 📄 **Weekly Privacy Report** — Automated PDF/markdown summary of your device's weekly privacy health.
- [ ] 📤 **Export Data (CSV/JSON)** — Export all locally stored logs for external analysis or ML training.
- [ ] 🏆 **Privacy Health Score** — A simple score (0-100) telling you how private your device is at a glance.

---

## 🤝 Contributing

Contributions, issues and feature requests are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ to give Android users back their privacy**

*If you find PrivacyGuard useful, please ⭐ star the repository — it helps others discover it!*

</div>
