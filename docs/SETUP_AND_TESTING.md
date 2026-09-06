# Developer Setup, Testing & Demonstration Guide

This guide explains how to build, run, test, and demonstrate the **Whisp** platform.

---

## 1. Prerequisites

- **JDK**: Java Development Kit (JDK 17 or higher)
- **Android Studio**: Hedgehog / Iguana / Koala or newer
- **Android SDK**: API Level 34 (Compile & Target SDK), Min SDK 24 (Android 7.0+)
- **Device Requirements**: Physical Android device or Emulator with Wi-Fi / Bluetooth enabled.

---

## 2. Building & Running the App

### Option A: Via Command Line (Gradle)
```bash
# Navigate to the Android project root
cd OfflineChat

# Compile all Kotlin & Java source files
./gradlew compileDebugSources

# Run unit test suite
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug
```
The compiled APK will be located at:
`OfflineChat/app/build/outputs/apk/debug/app-debug.apk`

### Option B: Via Android Studio
1. Open Android Studio -> Select `Open an Existing Project`.
2. Select the `OfflineChat` folder.
3. Allow Gradle to sync dependencies.
4. Click the green **Run** button to deploy to your connected device or emulator.

---

## 3. Pre-Configured Test Accounts

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `whispadmin123` | `SUPER_ADMIN` | Full control plane, incident dispatch, node isolation, simulation |
| `operator` | `operator123` | `NETWORK_ADMIN` | Incident dispatch, CCTV search, route monitoring |
| `yashwanth` | `password123` | `USER` | Tourist safety hub, digital ID card, peer chat, SOS triggers |
| `alice` | `alice123` | `USER` | Tourist safety hub, direct P2P mesh chat |

---

## 4. SIH Evaluator / Live Demo Walkthrough Script

### Step 1: Launch & Digital Tourist ID
1. Open the app and log in as `yashwanth` (password: `password123`).
2. Tap the **WHISP TOURIST SAFETY HUB** card on the home screen.
3. In the **DIGITAL ID** tab:
   - View your cryptographic Tourist ID badge, emergency blood group, and emergency contact details.
   - Tap the **Share / QR icon** to open the high-tech **Verifiable QR Tourist Card**.
   - Toggle **Selective Privacy Consents** (e.g. disable/enable Ranger itinerary sharing).

### Step 2: Smart Geo-Fence Radar & Live Location Simulation
1. Switch to the **SMART TRIP** tab.
2. View your active trek route (*Solang & Beas Kund Trek*) with 92% Safe Route Score.
3. Tap **CHECK-IN** on *Anjani Mahadev Viewpoint* to record an authenticated checkpoint on the blockchain.
4. Observe the animated **360-Degree Geo-Fencing Radar**:
   - Tap **Caution Trail** -> Observe radar boundary warning (*Entering Sissu Valley Pass*).
   - Tap **Danger Hazard** -> Observe instant danger alert (*Rohtang Landslide Hazard Zone*).

### Step 3: 33-Point MediaPipe Pose Estimation & Fall Detection
1. Switch to the **AI POSE** tab.
2. Observe the real-time **33-Landmark Skeletal Canvas** rendering joint nodes and bones.
3. Tap **Walking** or **Running** to see the skeletal posture dynamically shift.
4. Tap **Sim Fall**:
   - Skeleton turns crimson red and snaps to fallen horizontal ground orientation.
   - The **Two-Stage Safety Verification Modal** immediately activates with a **20-Second Buzzer Countdown**.
   - Tap **"I AM SAFE"** to safely dismiss, or let the timer reach 0 to observe automated high-priority emergency SOS escalation.

### Step 4: Multi-Agency Authority Dispatch Desk
1. Tap the **Authority View** icon in the top bar (or open from Admin panel).
2. In the **INCIDENT TRIAGE** tab:
   - Inspect active emergency incidents.
   - Tap on an incident to open the dispatch modal.
   - Assign response agency (**Police**, **Medical Ambulance**, or **Forest Rangers**) and tap **SAVE & DISPATCH**.
3. In the **CCTV SEARCH** tab:
   - Filter camera feeds by `FALL_DETECTED` or `WALKING` to view targeted camera stream matches.
4. In the **IMPACT 66%** tab:
   - View the response time comparison (**18.2 min -> 6.1 min, 66% reduction**) and the annual incidents prevented projection chart.

### Step 5: Web Control Plane Browser Interface
1. While the app is running on device/emulator, open your computer browser at:
   `http://localhost:8080/api/v1/safety/overview`
2. View real-time JSON telemetry from the embedded Ktor engine.
