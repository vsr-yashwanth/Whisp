# OfflineChat - Whisp Android Application

This folder contains the complete Android application implementation for **Whisp: Decentralized Offline Mesh Communication & Smart Tourist Safety System**.

## Tech Stack & Dependencies
- **Language**: Kotlin 1.9.0
- **UI Framework**: Jetpack Compose with Material 3 (Obsidian Dark High-Tech Aesthetic)
- **Local Persistence**: SQLite with Room Database (Version 6, Auto-migration enabled)
- **Embedded Web Server**: Ktor CIO Engine with Content Negotiation & KotlinX Serialization (Port 8080)
- **Cryptography**: Google Tink AEAD (Hardware Keystore Backed AES-256-GCM at rest, XChaCha20-Poly1305 in transit, Ed25519 signatures, SHA-256 Blockchain)
- **Local Radios**: Wi-Fi Direct / Google Nearby Connections API + Bluetooth Low Energy (BLE) Advertisers & Scanners

## Key Source Code Paths
- `app/src/main/java/com/example/offlinechat/safety/WhispSafetyManager.kt` - Central safety domain engine (AI Risk, Geo-Fencing, 33-pt Pose, 2-stage SOS, Blockchain).
- `app/src/main/java/com/example/offlinechat/ui/safety/TouristSafetyScreen.kt` - Tourist Safety Hub (Digital ID, QR card, Radar visualizer, AI Pose canvas).
- `app/src/main/java/com/example/offlinechat/ui/safety/AuthorityDispatchScreen.kt` - Authority Dispatch Desk (Incident triage, CCTV search, Zone monitoring, 66% impact analytics).
- `app/src/main/java/com/example/offlinechat/data/SafetyEntities.kt` - Tourist profiles, GeoZones, Trips, Incidents, and Blockchain data models.
- `app/src/main/java/com/example/offlinechat/network/WebServerManager.kt` - Ktor REST API endpoints (`/api/v1/safety/*`).
- `app/src/main/java/com/example/offlinechat/network/HybridMeshTransport.kt` - Physical radio abstraction, multi-hop routing coordinator.
- `app/src/main/java/com/example/offlinechat/network/dtn/DtnEngine.kt` - Delay-Tolerant Networking store-and-forward custody engine.

## Build Instructions
```bash
./gradlew compileDebugSources
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
