# Whisp Tourist Safety: Functional & Algorithmic Specification

**Smart India Hackathon 2026**
- **Problem Statement ID**: `SIH25002`
- **Problem Statement Title**: Smart Tourist Safety Monitoring & Incident Response System using AI, Geo-Fencing, and Blockchain-based Digital ID
- **Theme**: Travel and Tourism | **Category**: Software
- **Platform**: Whisp (Decentralized Mesh & Tourist Safety Platform)
- **Team**: NETRUNNERS

---

## 1. Mathematical & Algorithmic Formulations

### 1.1 AI Risk Engine Scoring Model

The AI Risk Engine evaluates real-time environmental, behavioral, and biophysical parameters to compute a composite Risk Score $R \in [0, 100]$:

$$R = \min\left(100, \; \left(w_z \cdot Z + w_p \cdot P + w_d \cdot D + w_m \cdot M + w_b \cdot B\right)\right)$$

Where:
- $Z \in [0, 100]$: **Geo-Zone Hazard Weight**
  - Safe Zone: $Z = 5$
  - Open Uncharted Trail: $Z = 15$
  - Caution Zone (Slippery / High Tide / Remote): $Z = 35$
  - Restricted Hazard Zone (Landslides / Core Sanctuary): $Z = 85$
- $P \in [0, 100]$: **Keypoint Posture Anomaly Factor**
  - Standing / Resting: $P = 5$
  - Walking / Hiking: $P = 8$
  - Running / Fast Descent: $P = 20$
  - Prolonged Inactivity / Unresponsive: $P = 45$
  - Fall Detected: $P = 60$
- $D \in [0, 100]$: **Route Deviation Factor** (Cross-track error from planned itinerary polyline)
- $M \in [0, 100]$: **Mobility Dynamics Factor** (Sudden velocity drop or erratic speed spikes)
- $B \in [0, 100]$: **Device Health & Battery Factor** (Penalty for critical battery $< 15\%$)

#### Model Weights:
$$w_z = 0.40, \quad w_p = 0.40, \quad w_d = 0.10, \quad w_m = 0.05, \quad w_b = 0.05$$

#### Threat Level Categorization:
| Score Range ($R$) | Threat Level | UI Accent | Action Triggered |
|---|---|---|---|
| $0 - 24$ | `NORMAL` | Signal Emerald | Background GPS telemetry |
| $25 - 49$ | `LOW` | Sky Blue | Checkpoint progress tracking |
| $50 - 69$ | `ELEVATED` | Amber Yellow | Trail awareness audio warning |
| $70 - 84$ | `HIGH` | Vibrant Orange | Ranger proximity notification |
| $85 - 100$ | `CRITICAL` | Crimson Red | **2-Stage SOS Verification Countdown** |

---

## 2. MediaPipe 33-Point Pose Landmark Topology

Whisp tracks the full 33-keypoint MediaPipe body landmark topology to evaluate posture stability, kinematics, and sudden fall impacts:

```
                  0 [Nose]
               1 [L.Eye]   4 [R.Eye]
             2 [L.Ear]       5 [R.Ear]
                    \         /
             11 [L.Shoulder]--12 [R.Shoulder]
              |              |
             13 [L.Elbow]    14 [R.Elbow]
              |              |
             15 [L.Wrist]    16 [R.Wrist]
              |              |
             23 [L.Hip]-------24 [R.Hip]
              |              |
             25 [L.Knee]     26 [R.Knee]
              |              |
             27 [L.Ankle]    28 [R.Ankle]
              |              |
             31 [L.Toe]      32 [R.Toe]
```

### Fall Detection Kinematic Classifier:
A fall event is triggered when:
1. **Vertical Collapse Acceleration**: Downward velocity of shoulder midpoint $\frac{y_{11} + y_{12}}{2}$ exceeds $-3.2 \text{ m/s}^2$.
2. **Horizontal Body Orientation**: The angle $\theta$ between the spinal vector $\vec{V}_{\text{spine}} = (\text{Midpoint}_{\text{shoulder}} - \text{Midpoint}_{\text{hip}})$ and the horizontal ground plane drops below $25^\circ$:
   $$\theta = \left|\arctan\left(\frac{y_{\text{shoulder}} - y_{\text{hip}}}{x_{\text{shoulder}} - x_{\text{hip}}}\right)\right| < 25^\circ$$
3. **Subsequent Immobility**: Positional variance $\sigma^2(\text{keypoints}) < 0.005$ maintained for $\Delta t \ge 2.5\text{ seconds}$.

---

## 3. Smart Geo-Fencing & Proximity Calculations

Distance between the current GPS coordinates $(lat_1, lon_1)$ and geo-zone center $(lat_2, lon_2)$ is computed using the Great-Circle Haversine formula:

$$a = \sin^2\left(\frac{\Delta lat}{2}\right) + \cos(lat_1) \cdot \cos(lat_2) \cdot \sin^2\left(\frac{\Delta lon}{2}\right)$$
$$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \; \sqrt{1 - a}\right)$$
$$d = R_{\text{earth}} \cdot c \quad \text{where } R_{\text{earth}} = 6,371,000\text{ meters}$$

- **Inside Zone**: $d \le \text{radiusMeters}$
- **Approaching Buffer**: $\text{radiusMeters} < d \le 1.25 \times \text{radiusMeters}$

---

## 4. Blockchain Trust Layer & Ledger Specification

Each block in the immutable Whisp ledger is cryptographically bound:

```json
{
  "index": 1,
  "timestamp": 1725188400000,
  "transactionType": "INCIDENT_LOG",
  "payloadJson": "{\"incidentId\":\"INC-2026-4102\",\"touristId\":\"TID-IN-2026-1049\",\"severity\":\"HIGH\"}",
  "previousHash": "0000000000000000000000000000000000000000000000000000000000000000",
  "merkleRoot": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "nonce": 4821,
  "hash": "8f4a1c2b...",
  "isValidated": true
}
```

Block Hash is calculated via:
$$\text{Block Hash} = \text{SHA-256}\left(\text{index} \parallel \text{timestamp} \parallel \text{txType} \parallel \text{prevHash} \parallel \text{merkleRoot} \parallel \text{nonce}\right)$$
