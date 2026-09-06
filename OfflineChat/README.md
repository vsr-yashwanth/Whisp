# OfflineChat - Whisp Android Application

This folder contains the complete Android application implementation for **Whisp: Off-Grid Peer to Peer Mesh Network**.

## Tech Stack & Dependencies
- **Language**: Kotlin 1.9.0
- **UI Framework**: Jetpack Compose with Material 3 (Obsidian Dark High-Tech Aesthetic)
- **Local Persistence**: SQLite with Room Database (Version 6, Auto-migration enabled)
- **Embedded Web Server**: Ktor CIO Engine with Content Negotiation & KotlinX Serialization (Port 8080)
- **Cryptography**: Google Tink AEAD (Hardware Keystore Backed AES-256-GCM at rest, XChaCha20-Poly1305 in transit, Ed25519 signatures, SHA-256 Blockchain)
- **Local Radios**: Wi-Fi Direct / Google Nearby Connections API + Bluetooth Low Energy (BLE) Advertisers & Scanners

## Key Source Code Paths
- `app/src/main/java/com/example/offlinechat/network/HybridMeshTransport.kt` - Physical radio abstraction, multi-hop routing coordinator.
- `app/src/main/java/com/example/offlinechat/network/dtn/DtnEngine.kt` - Delay-Tolerant Networking store-and-forward custody engine.
- `app/src/main/java/com/example/offlinechat/routing/RoutingEngine.kt` - Multi-hop opportunistic & PRoPHET routing algorithms.
- `app/src/main/java/com/example/offlinechat/routing/BatteryRelayPolicy.kt` - Energy-aware packet relay control.
- `app/src/main/java/com/example/offlinechat/security/CryptoManager.kt` - Google Tink AEAD at-rest & in-transit cryptography.
- `app/src/main/java/com/example/offlinechat/ui/ChatScreen.kt` - Encrypted peer-to-peer and group mesh chat.
- `app/src/main/java/com/example/offlinechat/ui/CrdtNotesScreen.kt` - Conflict-Free Replicated Data Type (CRDT) collaborative field notes.
- `app/src/main/java/com/example/offlinechat/network/WebServerManager.kt` - Embedded Ktor REST API Server.

## Build Instructions
```bash
./gradlew compileDebugSources
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
