# Whisp - Off-Grid Peer to Peer Mesh Network

> **Decentralized Peer-to-Peer Mesh Networking, Delay-Tolerant Routing & Encrypted Communication**  
> *Developed by Team **NETRUNNERS** for **Smart India Hackathon 2026** (Problem Statement ID: `SIH25002`)*

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2034-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Tink Cryptography](https://img.shields.io/badge/Security-Google%20Tink%20AEAD-orange.svg)](https://github.com/google/tink)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

---

## What is Whisp?

**Whisp** is a state-of-the-art, privacy-first, zero-cloud communication and decentralized networking platform. Designed to operate in environments with **zero cellular connectivity, no mobile data, and no internet access**, Whisp connects devices directly through physical local radios (Wi-Fi Direct, Bluetooth Low Energy, and Multi-Hop Mesh Relays).

Whisp provides a resilient, delay-tolerant transport engine that enables encrypted peer-to-peer messaging, group mesh communication, collaborative data synchronization, and decentralized node discovery entirely off the grid.

---

## Core Platform Features & Innovations

### 1. Resilient Decentralized Mesh & Delay-Tolerant Networking (DTN)
- **Zero-Internet Local Radios**: Discover and link peers automatically via high-speed Wi-Fi Direct and energy-efficient BLE beacons.
- **Store-and-Forward DTN Engine**: When destination nodes are out of immediate radio range, custody bundles are cached with TTLs and opportunistic PRoPHET routing until reaching an available relay or gateway node.
- **Battery-Aware Relay Policy**: Prevents low-battery nodes (< 15%) from exhausting reserves while optimizing packet routing paths.
- **Deduplication & Loop Prevention**: High-throughput LRU cache and hop limit verification stop broadcast packet storms.

### 2. End-to-End Cryptography & Verifiable Identities
- **Hardware-Backed AEAD**: AES-256-GCM at rest with Android Keystore integration and XChaCha20-Poly1305 for in-transit communication.
- **Verifiable Cryptographic Credentials**: Peer identities backed by Ed25519 signatures and cryptographic key pairs.
- **Tamper-Proof Chained Ledger**: Maintains an immutable SHA-256 audit ledger for node activity and topology integrity.

### 3. Encrypted P2P & Group Mesh Chat
- **1-on-1 Direct Messaging**: Zero-cloud private messaging over multi-hop mesh relays with delivery receipts.
- **Broadcast & Group Channels**: Multi-peer group messaging propagating across ad-hoc local mesh clusters.
- **Offline Packet Bundling**: Messages queued and delivered automatically as peers move within radio proximity.

### 4. CRDT Collaborative Shared Notes
- **Conflict-Free Replicated Data Types (CRDT)**: LWWMap-based distributed collaborative notes across mesh peers without merge conflicts.
- **Decentralized Synchronization**: Notes sync seamlessly over peer-to-peer radio exchanges without requiring a central database.

### 5. Network Control Plane & Mesh Diagnostics
- **Operator Console**: Real-time topology monitoring, packet delivery metrics, routing tables, and DTN custody buffer inspection.
- **Embedded Web Control Plane**: High-performance embedded Ktor REST web server on port `8080` for local node management and diagnostic telemetry.

---

## System Architecture

```
+-----------------------------------------------------------------------------------+
|                        JETPACK COMPOSE USER INTERFACES                            |
|    +-----------------------------+       +------------------------------------+   |
|    |   Whisp Encrypted P2P Chat  |       |   Network Operator Console         |   |
|    |   - 1-on-1 Direct Messaging |       |   - Topology & Active Peer Map     |   |
|    |   - Multi-Hop Group Mesh    |       |   - Routing & DTN Custody Stats    |   |
|    |   - Delivery Receipts       |       |   - Packet Telemetry Logs          |   |
|    +-----------------------------+       +------------------------------------+   |
|    +-----------------------------+       +------------------------------------+   |
|    |   CRDT Collaborative Notes  |       |   Node Identity & Key Hub          |   |
|    |   - Real-time Conflict-Free |       |   - Key Pair Management            |   |
|    |   - Distributed Field Notes |       |   - Cryptographic Credentials      |   |
|    +-----------------------------+       +------------------------------------+   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                     WHISP CORE ROUTING & NETWORKING ENGINES                       |
|  - HybridMeshTransport Coordinator       - Multi-Hop Routing & PRoPHET Engine     |
|  - Delay-Tolerant (DTN) Custody Manager  - Battery-Aware Relay Policy Controller  |
|  - Packet Deduplication & LRU Cache      - CRDT Document Synchronization Engine   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                    SECURITY, CRYPTOGRAPHY & TRUST LAYER                           |
|  - Google Tink AEAD (Hardware Keystore)  - Ed25519 Packet Envelope Signatures     |
|  - Tamper-Evident SHA-256 Block Ledger   - Decentralized Key Exchange Protocol    |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                         TRANSPORT & COMMUNICATION LAYER                           |
|  - Wi-Fi Direct / Nearby Connections     - Bluetooth Low Energy (BLE) Mesh        |
|  - Delay-Tolerant (DTN) Store & Forward  - Embedded Ktor REST Web Server (:8080)  |
+-----------------------------------------------------------------------------------+
```

---

## Codebase Directory Tour

```
OfflineChat/
├── app/src/main/java/com/example/offlinechat/
│   ├── data/
│   │   ├── Entities.kt           # Messages, Conversations, DTN Bundles, Epochs
│   │   ├── ChatDao.kt            # Room DAO queries for mesh nodes and message records
│   │   ├── ChatDatabase.kt       # Room Database configuration (v6)
│   │   └── UserManager.kt        # User accounts & role-based access control
│   ├── ui/
│   │   ├── HomeScreen.kt         # Main Dashboard with Mesh Status & Navigation Tabs
│   │   ├── AdminScreen.kt        # Network Grid Operator Control Plane
│   │   ├── ChatScreen.kt         # Encrypted Peer-to-Peer & Group Mesh Chat
│   │   ├── CrdtNotesScreen.kt    # Conflict-Free Collaborative Field Notes
│   │   └── AuthScreen.kt         # Authentication & Secure Login Gate
│   ├── network/
│   │   ├── HybridMeshTransport.kt# Wi-Fi Direct + BLE + Relay Transport Coordinator
│   │   ├── WebServerManager.kt   # Embedded Ktor REST API Server
│   │   └── dtn/DtnEngine.kt      # Delay-Tolerant Store-and-Forward Custody Engine
│   ├── routing/
│   │   ├── RoutingEngine.kt      # Multi-hop opportunistic & PRoPHET routing
│   │   └── BatteryRelayPolicy.kt # Energy-aware packet relay control
│   └── security/
│       └── CryptoManager.kt      # Google Tink AEAD at rest & in-transit cryptography
docs/
├── ARCHITECTURE.md               # Technical architecture & subsystem deep-dive
├── API_REFERENCE.md              # Complete REST API reference for Ktor server
└── SETUP_AND_TESTING.md          # Step-by-step developer setup & judge demo script
```

---

## Quick Start: Build & Run

### 1. Prerequisites
- **JDK 17+** and **Android Studio** (Hedgehog or newer)
- **Android SDK Level 34**

### 2. Build via Terminal
```bash
# Clone the repository
git clone https://github.com/vsr-yashwanth/Whisp.git
cd Whisp/OfflineChat

# Compile the app
./gradlew compileDebugSources

# Run unit tests
./gradlew testDebugUnitTest

# Generate APK
./gradlew assembleDebug
```

### 3. Pre-Configured Test Accounts
- **Peer User**: Username `yashwanth` | Password `password123`
- **Super Admin**: Username `admin` | Password `whispadmin123`
- **Network Operator**: Username `operator` | Password `operator123`
- **Mesh Peer**: Username `alice` | Password `alice123`

---

## Embedded Web Control Plane
When the app is running on a device or emulator, open your browser at:
- `http://localhost:8080/api/v1/network/overview` - Live network health & peer metrics
- `http://localhost:8080/api/v1/network/topology` - Mesh topology and routing table entries
- `http://localhost:8080/api/v1/network/bundles` - Delay-Tolerant custody bundles

---

## Team NETRUNNERS (Smart India Hackathon 2026)

| Role | Name | Registration No. | Department |
|---|---|---|---|
| **Team Leader** | **Vangala Sreeram Yaswanth** | `RA2511056010025` | DSBS |
| **Team Member** | **Souvik Chattopadhyay** | `RA2511056010061` | DSBS |
| **Team Member** | **Anamika Gupta** | `RA2511056010082` | DSBS |
| **Team Member** | **Alisha** | `RA2511026011294` | CINTEL |
| **Team Member** | **Anuj Kumar Singh** | `RA2511003010803` | CTECH |
| **Team Member** | **Vansh Tyagi** | `RA2511056010073` | DSBS |
| **Faculty Mentor** | **Jagadish Kumar N** | - | DSBS |
| **Industry Mentor**| **V Sree Harsha** | - | - |

---

## License
This project is licensed under the Apache 2.0 License.
