# Whisp REST API Reference

The embedded high-performance Ktor server runs locally on port `8080` (accessible via `http://localhost:8080` or the device's LAN/Hotspot IP `http://192.168.x.x:8080`).

---

## 1. Whisp Tourist Safety Endpoints

### 1.1 Overview & System Telemetry
- **Endpoint**: `GET /api/v1/safety/overview`
- **Description**: Returns live safety metrics, threat level, active incident count, and response time reduction KPI.
- **Sample Response**:
```json
{
  "status": "OPERATIONAL",
  "activeIncidentsCount": "1",
  "touristThreatLevel": "NORMAL",
  "aiRiskScore": "12",
  "currentZone": "Mall Road Tourist Corridor",
  "currentPose": "STANDING",
  "responseTimeReductionPct": "66",
  "baselineResponseTimeMin": "18.2",
  "whispResponseTimeMin": "6.1",
  "projectedIncidentsPrevented2026": "350",
  "blockchainTrustActive": "true"
}
```

---

### 1.2 Get Registered Tourists
- **Endpoint**: `GET /api/v1/safety/tourists`
- **Description**: Returns list of all registered tourists in the corridor with consent-filtered profiles.
- **Sample Response**:
```json
[
  {
    "touristId": "TID-IN-2026-8842",
    "fullName": "Alex Mercer",
    "nationality": "Indian / Tourist",
    "passportOrIdHash": "SHA256:7a9f20c4e1b853d9e802...",
    "phone": "+91 98765 43210",
    "emergencyContactName": "Sarah Mercer (Family)",
    "emergencyContactPhone": "+91 91234 56789",
    "bloodGroup": "O+ Positive",
    "medicalNotes": "Asthma (Carries Inhaler), Penicillin Allergy",
    "blockchainIdentityHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "activeTripId": "TRIP-MANALI-01",
    "registeredAt": 1725188400000
  }
]
```

---

### 1.3 Get All Incidents & SOS Alerts
- **Endpoint**: `GET /api/v1/safety/incidents`
- **Description**: Returns all reported, acknowledged, dispatched, and resolved incidents.
- **Sample Response**:
```json
[
  {
    "incidentId": "INC-2026-4102",
    "touristId": "TID-IN-2026-1049",
    "touristName": "Rohan Sharma",
    "triggerSource": "AI_FALL_DETECTION",
    "severity": "HIGH",
    "status": "DISPATCHED",
    "assignedAgency": "MEDICAL_AMBULANCE",
    "latitude": 32.3190,
    "longitude": 77.1590,
    "zoneName": "Solang Adventure Base",
    "riskScore": 82,
    "postureState": "FALL_DETECTED",
    "batteryLevel": 45,
    "telemetrySnapshot": "Rapid downward acceleration detected. User unresponsive for 20s.",
    "responderNotes": "Ambulance Unit 4 dispatched with mountain paramedic stretcher.",
    "timestamp": 1725188400000,
    "blockchainProofHash": "8f4a1c2b..."
  }
]
```

---

### 1.4 Assign Response Agency & Update Incident
- **Endpoint**: `POST /api/v1/safety/incidents/{id}/assign`
- **Request Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "status": "DISPATCHED",
  "assignedAgency": "FOREST_RANGERS",
  "notes": "Ranger Patrol Unit 2 deployed to Trailhead Checkpoint 3."
}
```
- **Response**:
```json
{
  "success": true,
  "message": "Incident INC-2026-4102 assigned to FOREST_RANGERS"
}
```

---

### 1.5 Get Geo-Fence Zones
- **Endpoint**: `GET /api/v1/safety/geofences`
- **Description**: Returns safe, caution, and restricted hazard zones.

---

### 1.6 Get Targeted CCTV Cameras
- **Endpoint**: `GET /api/v1/safety/cctv`
- **Description**: Returns smart CCTV camera feeds and detections.

---

### 1.7 Get Blockchain Trust Blocks
- **Endpoint**: `GET /api/v1/safety/blockchain`
- **Description**: Returns the complete chain of validated tamper-proof blocks.

---

## 2. Whisp Mesh Admin & Control Plane Endpoints

- `GET /api/v1/admin/overview` — Network health score, peer count, DTN storage metrics.
- `GET /api/v1/admin/nodes` — Connected and discovered radio nodes.
- `GET /api/v1/admin/routes` — Routing table entries with PRoPHET score and next hops.
- `GET /api/v1/admin/dtn/bundles` — Ingested store-and-forward custody bundles.
- `GET /api/v1/admin/partitions` — Partition status, epoch number, reconciliation state.
- `GET /api/v1/admin/crdt/docs` — CRDT synchronized document registry.
- `GET /api/v1/admin/security/events` — Cryptographic signature & rate-limit security log.
- `POST /api/v1/admin/simulation/run` — Execute discrete mesh network benchmarks.
