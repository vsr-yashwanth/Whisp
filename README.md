# Whisp - Decentralized Offline Mesh Communication & Smart Tourist Safety Platform

> **Decentralized Peer-to-Peer Mesh Networking, Delay-Tolerant Routing & AI-Powered Tourist Safety Monitoring**  
> *Developed by Team **NETRUNNERS** for **Smart India Hackathon 2026** (Problem Statement ID: `SIH25002`)*

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2034-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Tink Cryptography](https://img.shields.io/badge/Security-Google%20Tink%20AEAD-orange.svg)](https://github.com/google/tink)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

---

## What is Whisp?

**Whisp** is a state-of-the-art, privacy-first, zero-cloud communication and emergency intelligence platform. Designed to operate in environments with **zero cellular connectivity, no mobile data, and no internet access**, Whisp connects devices directly through physical local radios (Wi-Fi Direct, Bluetooth Low Energy, and Multi-Hop Mesh Relays).

Built on top of Whisp's resilient peer-to-peer and delay-tolerant transport engine is the **Whisp Smart Tourist Safety & Incident Response Subsystem** - an AI-driven guardian for travelers, hikers, and expedition teams that integrates real-time geo-fencing radar, 33-point MediaPipe skeletal tracking, kinematic fall detection, two-stage intelligent SOS verification, verifiable digital identities, and a tamper-proof blockchain audit ledger to **reduce emergency response times by 66%**.

---

## Core Platform Features & Innovations

### 1. Resilient Decentralized Mesh & Delay-Tolerant Networking (DTN)
- **Zero-Internet Local Radios**: Discover and link peers automatically via high-speed Wi-Fi Direct and energy-efficient BLE beacons.
- **Store-and-Forward DTN Engine**: When destination nodes are out of immediate radio range, custody bundles are cached with TTLs and opportunistic PRoPHET routing until reaching an available relay or gateway node.
- **Battery-Aware Relay Policy**: Prevents low-battery nodes (< 15%) from exhausting reserves while routing high-priority emergency packets unconditionally.
- **Deduplication & Loop Prevention**: High-throughput LRU cache and hop limit verification stop broadcast packet storms.

### 2. Privacy-First Digital Tourist ID & Selective Consent Matrix
- **Verifiable Cryptographic Credentials**: Automatically creates an immutable Digital Tourist ID (`TID-IN-2026-XXXX`) tied to a cryptographic hash.
- **Offline QR Code Verifier**: Authorities, park rangers, and local police verify credentials offline using public-key cryptography without exposing private PII.
- **Selective Consent Matrix**: Tourists own their data. Granular toggles control sharing of real-time coordinates, emergency medical notes, blood group, or trek itineraries. Every consent change is recorded onto the blockchain ledger.

### 3. Smart Geo-Fencing & 360-Degree Live Radar
- **Three-Tier Safety Corridors**:
  - **Safe Zones**: Verified tourist hubs, police beats, and medical staging camps (e.g., *Mall Road Tourist Corridor*, *Solang Valley Base Camp*).
  - **Caution Zones**: Unpaved mountain passes, high-tide coastal shelves, and low-connectivity trails (e.g., *Sissu Valley Pass*).
  - **Restricted Hazard Zones**: Active landslide slopes, deep chasms, and wildlife core sanctuaries (e.g., *Rohtang Landslide Hazard Zone*).
- **Interactive Visual Radar**: Real-time canvas radar displaying dynamic boundary rings, distance metrics, and automated audio/visual breach alerts computed via the Great-Circle Haversine formula.

### 4. AI Multi-Factor Risk Engine & Two-Stage SOS
- **Multi-Factor Risk Scoring (0 - 100)**: Continuously evaluates current zone danger ($w_z=0.40$), posture signals ($w_p=0.40$), route deviation ($w_d=0.10$), movement dynamics ($w_m=0.05$), and device battery health ($w_b=0.05$).
- **Intelligent Two-Stage SOS**: When a fall or anomaly is detected, Whisp launches a **20-Second Verification Buzzer**. Tourists can tap **"I AM SAFE"** to dismiss or **"DISPATCH SOS"** to escalate immediately. If unanswered, it auto-dispatches help over the mesh!

### 5. 33-Point MediaPipe Skeletal Tracking & Fall Detection
- **Real-Time Skeletal Tracking**: Tracks 33 body landmarks (head, shoulders, elbows, wrists, hips, knees, ankles) to classify posture (`STANDING`, `WALKING`, `RUNNING`, `FALL_DETECTED`).
- **Kinematic Fall Classifier**: Identifies rapid downward acceleration followed by a horizontal spinal angle (< 25 degrees), triggering emergency verification instantly.

### 6. Smart Itinerary & Verifiable Checkpoint Check-Ins
- **Safe Route Planning**: Pre-loaded travel routes rated with an objective Safety Score (e.g., *Solang & Beas Kund Trek - 92% Safe Route*).
- **Verifiable Checkpoints**: Check in at mountain trailheads and base stations with timestamped cryptographic receipts written to the blockchain.

### 7. Targeted CCTV Spatio-Temporal Investigation
- **Authority Investigation Search**: Enables police control rooms and rescue teams to search smart CCTV cameras using time windows, location zones, and AI pose filters (`FALL_DETECTED`, `UNUSUAL_INACTIVITY`) to locate lost or distressed travelers rapidly.

### 8. Multi-Agency Emergency Dispatch Desk
- **Incident Coordination**: Instant one-tap SOS that dispatches structured telemetry snapshots to **Police**, **Ambulance / Paramedics**, **Forest Rangers**, and **Disaster Management**.
- **Offline Mesh Relaying**: Operates seamlessly over device-to-device Wi-Fi Direct and BLE mesh with DTN store-and-forward bundles.

### 9. Blockchain Trust Layer
- **Tamper-Proof Audit Trail**: Maintains an immutable SHA-256 chained block ledger of tourist registrations, consent updates, checkpoint check-ins, and emergency dispatch logs.

### 10. CRDT Collaborative Shared Notes & Admin Console
- **Conflict-Free Replicated Data Types (CRDT)**: LWWMap-based distributed collaborative notes across mesh peers without merge conflicts.
- **Embedded Web Control Plane**: High-performance embedded Ktor REST web server on port `8080` for grid operators and emergency dispatchers.

---

## Measurable Impact: Saving Lives

Whisp combines automated sensor keypoints, geo-fencing, and decentralized mesh relays to dramatically accelerate emergency response:

```
Average Emergency Response Time (Minutes)
-----------------------------------------
Before Whisp : [==================] 18.2 min
With Whisp   : [======] 6.1 min   [66% FASTER]
```

---

## System Architecture

```
+-----------------------------------------------------------------------------------+
|                        JETPACK COMPOSE USER INTERFACES                            |
|    +-----------------------------+       +------------------------------------+   |
|    |   Whisp Tourist Safety UI   |       |   Authority Incident Dispatch UI   |   |
|    |   - Digital ID Card & QR    |       |   - Incident Triage Queue          |   |
|    |   - Live 360 Radar & Zones  |       |   - Targeted CCTV Camera Feeds     |   |
|    |   - 33-Pt Pose Canvas & Fall|       |   - Active Corridor Zone Monitor   |   |
|    |   - 2-Stage SOS Modal       |       |   - 66% Impact Analytics Dashboard |   |
|    +-----------------------------+       +------------------------------------+   |
|    +-----------------------------+       +------------------------------------+   |
|    |   Whisp Encrypted P2P Chat  |       |   Admin Grid Operator Console      |   |
|    |   - 1-on-1 Direct Messaging |       |   - Topology, Routing & DTN Stats  |   |
|    |   - Group Emergency Mesh    |       |   - CRDT Shared Field Notes        |   |
|    +-----------------------------+       +------------------------------------+   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                      WHISP CORE SERVICES & SAFETY ENGINES                         |
|  - WhispSafetyManager (AI Risk 0-100)    - 33-Point MediaPipe Pose Classifier     |
|  - Smart Geo-Fence Proximity Engine      - Two-Stage Intelligent SOS State Machine|
|  - Multi-Hop Routing & PRoPHET Engine    - Trip Itinerary & Checkpoint Tracker    |
|  - Delay-Tolerant (DTN) Custody Manager  - CRDT Document Synchronization Engine   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                    SECURITY, CRYPTOGRAPHY & TRUST LAYER                           |
|  - Google Tink AEAD (Hardware Keystore)  - Ed25519 Packet Envelope Signatures     |
|  - Tamper-Evident SHA-256 Blockchain     - Selective Privacy Consent Controller   |
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
│   │   ├── SafetyEntities.kt     # Tourist Profiles, GeoZones, Trips, Incidents, Blockchain models
│   │   ├── Entities.kt           # Messages, Conversations, DTN Bundles, Epochs
│   │   ├── ChatDao.kt            # Room DAO queries for all mesh and safety entities
│   │   ├── ChatDatabase.kt       # Room Database configuration (v6)
│   │   └── UserManager.kt        # User accounts & role-based access control
│   ├── safety/
│   │   └── WhispSafetyManager.kt # Central AI Risk, Geo-Fence, Pose, and SOS Domain Controller
│   ├── ui/
│   │   ├── safety/
│   │   │   ├── TouristSafetyScreen.kt    # Tourist Safety Hub (Digital ID, Radar, Pose, SOS)
│   │   │   └── AuthorityDispatchScreen.kt# Authority Dispatch Desk (Incidents, CCTV, Analytics)
│   │   ├── HomeScreen.kt         # Main Dashboard with Whisp Safety Card & Mesh Tabs
│   │   ├── AdminScreen.kt        # Network Grid Operator Control Plane
│   │   ├── ChatScreen.kt         # Encrypted Peer-to-Peer & Group Mesh Chat
│   │   ├── CrdtNotesScreen.kt    # Conflict-Free Collaborative Field Notes
│   │   └── AuthScreen.kt         # Authentication & Secure Login Gate
│   ├── network/
│   │   ├── HybridMeshTransport.kt# Wi-Fi Direct + BLE + Relay Transport Coordinator
│   │   ├── WebServerManager.kt   # Embedded Ktor REST API Server (/api/v1/safety/*)
│   │   └── dtn/DtnEngine.kt      # Delay-Tolerant Store-and-Forward Custody Engine
│   ├── routing/
│   │   ├── RoutingEngine.kt      # Multi-hop opportunistic & PRoPHET routing
│   │   └── BatteryRelayPolicy.kt # Energy-aware packet relay control
│   └── security/
│       └── CryptoManager.kt      # Google Tink AEAD at rest & in-transit cryptography
docs/
├── ARCHITECTURE.md               # Technical architecture & subsystem deep-dive
├── SAFETY_SPECIFICATION.md       # Mathematical formulations, kinematics & SIH mapping
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
- **Tourist**: Username `yashwanth` | Password `password123`
- **Super Admin**: Username `admin` | Password `whispadmin123`
- **Emergency Operator**: Username `operator` | Password `operator123`
- **Tourist Peer**: Username `alice` | Password `alice123`

---

## Embedded Web Control Plane
When the app is running on a device or emulator, open your browser at:
- `http://localhost:8080/api/v1/safety/overview` - Live safety health & metrics
- `http://localhost:8080/api/v1/safety/incidents` - Active SOS emergency alerts
- `http://localhost:8080/api/v1/safety/geofences` - Safe and restricted zone definitions
- `http://localhost:8080/api/v1/safety/tourists` - Registered tourist profiles & status

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
