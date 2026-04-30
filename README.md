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

**PrivacyGuard is a real-time, on-device Android privacy monitoring application that detects microphone abuse, camera & location tracking, keyloggers, suspicious background activity, hidden app co-activation patterns, and network tracker domains — without ever sending your data to the cloud.**

[Features](#-features) • [Problem Statement](#-problem-statement) • [Architecture](#-architecture) • [Getting Started](#-getting-started) • [Roadmap](#-roadmap)

</div>

---

## 🚨 Problem Statement

In today's mobile ecosystem, **your privacy is constantly under attack** — and most of the time, you don't even know it.

Consider these alarming realities:

- 📱 **Thousands of apps request microphone access** but use it silently in the background, far beyond the scope the user consented to.
- 📷 **Camera and location permissions are abused** by apps that track your every move and capture imagery without your knowledge.
- ⌨️ **Keyloggers disguised as keyboards or assistants** secretly capture every password, message, and search query you type.
- 🌙 **Apps secretly communicate with remote servers at 3 AM**, exfiltrating your personal data while you sleep.
- 🔗 **Hidden inter-app communication chains** allow one malicious app to trigger data collection in another, evading simple permission checks.
- 🌐 **Ad trackers and analytics SDKs** silently phone home to hundreds of tracking domains, building profiles of your behavior.

Android's built-in permission model is a **passive gatekeeper** — it asks once during installation and then largely steps aside. There is no native system that continuously monitors if granted permissions are being abused, detects behavioral anomalies, or warns users about coordinated multi-app tracking strategies.

> **PrivacyGuard was built to fill this gap.** It acts as a continuous, intelligent watchdog that monitors these threat vectors in real-time on your device, stores all findings locally, and presents them in an easy-to-understand dashboard — putting privacy control back in your hands.

---

## ✨ Features

### 🎤 1. Microphone Usage Tracking
Leverages Android's `AppOpsManager` to continuously detect and log every microphone access event on the device.
- Logs **which app** accessed the mic, **when**, and for **how long**
- Identifies apps accessing the mic in the background when the screen is off
- Visualized in a dedicated chart-based screen for quick analysis

### 📷 2. Camera & Location Abuse Monitoring
Extends `AppOpsManager` monitoring to camera and GPS access events.
- Detects which apps accessed your camera and for how long
- Tracks fine & coarse location queries per app
- Alerts when apps access sensors for unusually long durations

### ⌨️ 3. Keylogger Detection via Accessibility Service
Uses a custom `PrivacyAccessibilityService` with **debounced event tracking** to analyze app interaction patterns.
- Detects unusually high frequencies of text-field interactions — a hallmark of keylogging behavior
- Assigns a **suspicion score** to apps based on their input-capture patterns
- Flags apps that intercept >10 text changes within 60 seconds across app windows
- Battery-efficient debouncing prevents drain from high-frequency event monitoring

### 🌙 4. Suspicious Night-Time Activity Monitoring
Connects to Android's `UsageStatsManager` to track app behavior during off-hours.
- Scans for apps that **wake up between 12 AM – 6 AM** without user interaction
- Detects background processes that launch silently during sleep hours
- Configurable night-time window via Settings

### 🔗 5. App Co-Activation Pattern Detection (Trigger Map)
Investigates hidden causal relationships between apps on your device.
- Identifies when launching **App A** mysteriously triggers **App B** to start within 5 seconds
- Detects apps that act as silent proxies or data-sharing partners for other apps
- Presented as an interactive **Trigger Map** screen to visually expose hidden links

### 🌐 6. Network Traffic Monitoring (DNS Inspector)
Uses a **local-only VPN service** to monitor DNS queries and detect tracker domains.
- Intercepts DNS queries via a local TUN interface — **no data leaves the device**
- Matches queried domains against a bundled list of **250+ known tracking/advertising domains**
- Real-time tracker count on the dashboard
- Toggle VPN monitoring on/off directly from the Network Monitor screen
- Filter between "All DNS Queries" and "Trackers Only"

> **⚠️ Privacy Note:** The `INTERNET` permission is used **solely** for the local VPN packet inspection engine. PrivacyGuard does not make any external network calls, does not communicate with cloud servers, and does not upload any user data.

### 🔐 7. Permission Audit
Provides a bird's-eye view of dangerous permissions granted across all installed apps.
- Lists all apps with their granted dangerous permissions (Camera, Mic, Location, Contacts, SMS, etc.)
- Searchable and sortable by permission count
- Tap any app to jump directly to Android's app settings
- Color-coded risk indicators based on permission count

### 📋 8. Clipboard Monitor
Detects suspicious clipboard access patterns — a common data-theft vector.
- Monitors clipboard change frequency
- Alerts when clipboard is accessed >5 times in 10 seconds
- Protects against clipboard-harvesting malware

### 📊 9. Real-Time Privacy Dashboard
A rich, dark-themed dashboard giving an instant overview of all threat categories.
- Built with **MPAndroidChart** for smooth, interactive data visualizations
- Aggregated threat summaries across all monitoring domains
- Color-coded risk indicators for instant threat assessment
- **Privacy Score** (0-100) calculated using weighted penalties from `PrivacyScoreCalculator`

### 📅 10. Privacy Timeline
A unified chronological view of ALL privacy events across all categories.
- Merges mic, camera, location, night, keylogger, and trigger events
- Filter by event type and date range (Today, 3 Days, 7 Days, 30 Days)
- Color-coded timeline entries by event type

### 📤 11. Data Export (CSV/ZIP)
Export all locally stored privacy logs for external analysis.
- Exports all 8 database tables as individual CSV files
- Packages everything into a shareable ZIP archive
- Uses Android's share sheet for easy transfer

### 📄 12. Weekly PDF Privacy Report
Automated PDF summary of your device's weekly privacy health.
- Privacy Score with color-coded breakdown
- Per-category statistics (mic, camera, location, keylogger, night, triggers)
- Share via email, messaging, or file manager

### 🤖 13. AI Anomaly Insights
On-device anomaly detection using feature extraction and rule-based scoring.
- Per-app anomaly scores (0-100%) based on privacy event aggregation
- Feature breakdown chips showing mic, camera, location, night, keylogger, and trigger counts
- Ready for TFLite model integration

### 🔍 14. App Detail Deep-Dive
A unified detail screen for any individual app showing all its privacy events in one place.
- Consolidated view of all event types per app
- Per-app anomaly score gauge
- Direct link to Android's system app info settings

### ⚙️ 15. Always-On Background Engine
A robust background scanning engine that works efficiently without draining the battery.
- Powered by **Android WorkManager** with `requiresBatteryNotLow` constraint
- `BootReceiver` ensures protection restarts automatically after phone reboot
- Camera, location, night, and keylogger notification triggers on each scan cycle
- All data processed and stored 100% locally via **Room Database**

---

## 🏗️ Architecture

PrivacyGuard is built on **MVVM (Model-View-ViewModel)** architecture with clean separation of concerns.

```
PrivacyGuard/
│
├── 📦 data/
│   ├── db/
│   │   ├── entities/          # Room Entities (MicUsage, NightActivity, NetworkEvent, etc.)
│   │   ├── dao/               # Data Access Objects (8 DAOs)
│   │   └── AppDatabase.kt     # Room Database (v3, 8 entities)
│   └── repository/            # Repository layer (7 repositories)
│
├── 🧠 di/
│   ├── AppModule.kt           # Hilt DI module for app-level deps
│   └── DatabaseModule.kt      # Hilt DI module for Room DB (8 DAO providers)
│
├── 🧮 domain/
│   └── PrivacyScoreCalculator.kt  # Weighted privacy score engine
│
├── 🤖 ml/
│   ├── AnomalyDetector.kt    # TFLite-ready anomaly detection
│   └── FeatureExtractor.kt   # Privacy feature aggregation per app
│
├── 🔔 receiver/
│   └── BootReceiver.kt        # Restarts services on device boot
│
├── 🔧 service/
│   ├── PrivacyAccessibilityService.kt  # Enhanced keylogger detector (debounced)
│   ├── NetworkMonitorVpnService.kt     # Local-only VPN DNS inspector
│   ├── DnsPacketParser.kt              # Raw DNS packet parser
│   ├── TrackerDomainMatcher.kt         # Tracker domain blocklist matcher
│   └── ClipboardMonitor.kt             # Clipboard access monitor
│
├── 🛠️ utils/
│   ├── NotificationHelper.kt   # Push notification manager
│   ├── PdfReportGenerator.kt   # Weekly PDF report engine
│   └── DataExportManager.kt    # CSV/ZIP data export
│
├── 🖥️ ui/
│   ├── screens/
│   │   ├── dashboard/         # Main summary screen
│   │   ├── micusage/          # Microphone usage logs & charts
│   │   ├── camera/            # Camera usage tracking
│   │   ├── location/          # Location access monitoring
│   │   ├── keylogger/         # Keylogger detection alerts
│   │   ├── nightactivity/     # Overnight background app activity
│   │   ├── triggermap/        # App co-activation trigger map
│   │   ├── network/           # Network DNS monitor & tracker detection
│   │   ├── permissions/       # Permission audit screen
│   │   ├── timeline/          # Unified privacy event timeline
│   │   ├── appdetail/         # Per-app privacy deep-dive
│   │   ├── aiinsights/        # AI anomaly analysis
│   │   ├── report/            # Weekly PDF report
│   │   ├── onboarding/        # First-run permissions setup
│   │   └── settings/          # App settings, export, & whitelist
│   ├── components/            # Reusable Composable components
│   └── theme/                 # Color, Typography, Theme definitions
│
├── 🗺️ navigation/
│   └── AppNavigation.kt       # Compose Navigation graph (15 routes)
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
| **Local DB** | Room (SQLite, 8 entities) |
| **Background Work** | WorkManager |
| **Network Monitor** | VpnService (local-only) |
| **Charts** | MPAndroidChart |
| **Privacy APIs** | AppOpsManager, UsageStatsManager, AccessibilityService, VpnService |
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
   - ✅ VPN Permission (for network monitoring — optional)

---

## 🔒 Privacy Commitment

> **PrivacyGuard practices what it preaches.**

- ✅ **Zero cloud dependency** — All analysis, logs, and data live exclusively on your device.
- ✅ **No analytics or telemetry** — We don't collect any usage data, crash reports, or identifiers.
- ✅ **Local-only networking** — The `INTERNET` permission is used **solely** for the local VPN DNS inspection engine. No data is sent to any server.
- ✅ **No external API calls** — PrivacyGuard never communicates with external servers.
- ✅ **Open source** — The entire codebase is available for audit.

---

## 🗺️ Roadmap

- [x] 🎤 **Microphone Usage Tracking** — Real-time mic access monitoring via `AppOpsManager`
- [x] 📷 **Camera & Location Abuse Tracking** — Extended `AppOpsManager` monitoring
- [x] ⌨️ **Keylogger Detection** — Enhanced AccessibilityService with debouncing
- [x] 🌙 **Night-Time Activity Monitoring** — Configurable overnight app scanning
- [x] 🔗 **App Co-Activation Detection** — Trigger Map for hidden app relationships
- [x] 📡 **Network Traffic Monitoring** — Local VPN DNS inspection with tracker detection
- [x] 🔐 **Permission Audit** — System-wide dangerous permission scanner
- [x] 📋 **Clipboard Monitor** — Suspicious clipboard access detection
- [x] 📊 **Privacy Score Dashboard** — Weighted 0-100 score with animated gauge
- [x] 📅 **Privacy Timeline** — Unified chronological event view
- [x] 📤 **Data Export (CSV/ZIP)** — Full data export with share sheet
- [x] 📄 **Weekly PDF Report** — Automated privacy health summary
- [x] 🤖 **AI Anomaly Insights** — Rule-based anomaly scoring (TFLite-ready)
- [x] 🔍 **App Detail Deep-Dive** — Per-app privacy event consolidation
- [x] 🔔 **Push Alert System** — Real-time notifications for all threat categories
- [ ] 🧠 **On-Device ML Model** — Replace heuristic rules with TFLite autoencoder
- [ ] 📦 **Gradle Modularization** — Split into `:core`, `:feature` modules for scalability
- [ ] 🧪 **Unit Test Suite** — JUnit5 + MockK tests for domain and data layers

---

## 👥 Core Team / Creators

- **Karthik Sai** - [@KarthikSai06](https://github.com/KarthikSai06)
- **Nikhil Gowda** - [@Nikhil-gowda2005](https://github.com/Nikhil-gowda2005)

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
