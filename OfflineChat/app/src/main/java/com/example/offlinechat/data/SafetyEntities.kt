package com.example.offlinechat.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 1. Digital Tourist ID & Profile
 */
@Serializable
@Entity(tableName = "safety_tourist_profiles")
data class TouristProfile(
    @PrimaryKey val touristId: String, // e.g. "TID-IN-2026-8842"
    val fullName: String,
    val nationality: String,
    val passportOrIdHash: String,
    val phone: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val bloodGroup: String,
    val medicalNotes: String,
    val blockchainIdentityHash: String,
    val activeTripId: String = "",
    val qrCredentialPayload: String = "",
    val registeredAt: Long = System.currentTimeMillis()
)

/**
 * 2. Privacy & Selective Consent Settings
 */
@Serializable
data class ConsentSettings(
    val shareLocationWithAuthorities: Boolean = true,
    val shareMedicalDataInEmergency: Boolean = true,
    val shareItineraryWithRangers: Boolean = true,
    val anonymizedTelemetryAnalytics: Boolean = true,
    val biometricKeypointTracking: Boolean = true,
    val lastConsentUpdate: Long = System.currentTimeMillis()
)

/**
 * 3. Smart Geo-Fence Zone
 */
enum class GeoZoneType {
    SAFE,       // Tourist Hubs, Police Beats, Medical Camps (Green)
    CAUTION,    // Slippery Trails, High-Tide Beach, Low Connectivity (Yellow)
    RESTRICTED  // Landslide Slopes, Dense Forest Reserves, Danger Gorges (Red)
}

@Serializable
@Entity(tableName = "safety_geofence_zones")
data class GeoFenceZone(
    @PrimaryKey val zoneId: String,
    val name: String,
    val description: String,
    val zoneType: GeoZoneType,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Double,
    val riskWeight: Int, // 0 for SAFE, 30 for CAUTION, 85 for RESTRICTED
    val emergencyContactAgency: String = "LOCAL_POLICE"
)

/**
 * 4. Trip & Itinerary Management
 */
@Serializable
data class TripCheckpoint(
    val checkpointId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val isCheckedIn: Boolean = false,
    val checkInTimestamp: Long = 0L
)

@Serializable
@Entity(tableName = "safety_trip_itineraries")
data class TripItinerary(
    @PrimaryKey val tripId: String,
    val title: String,
    val destinationRegion: String,
    val safeRouteScore: Int, // 0 - 100
    val waypointsJson: String, // List<TripCheckpoint> serialized
    val totalDistanceKm: Double,
    val status: String = "ACTIVE", // PLANNED, ACTIVE, COMPLETED
    val currentCheckpointIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 5. Keypoint Pose & Motion State
 */
enum class PoseState {
    STANDING,
    WALKING,
    RUNNING,
    FALL_DETECTED,
    PROLONGED_INACTIVITY
}

/**
 * 6. AI Risk Score Level
 */
enum class ThreatLevel {
    NORMAL,
    LOW,
    ELEVATED,
    HIGH,
    CRITICAL
}

/**
 * 7. SOS & Incident Response
 */
enum class IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class IncidentStatus {
    REPORTED,
    ACKNOWLEDGED,
    DISPATCHED,
    RESOLVED
}

enum class ResponseAgency {
    POLICE_CONTROL,
    MEDICAL_AMBULANCE,
    FOREST_RANGERS,
    DISASTER_MANAGEMENT,
    TOURIST_HELPLINE
}

@Serializable
@Entity(tableName = "safety_incidents")
data class SafetyIncident(
    @PrimaryKey val incidentId: String, // e.g. "INC-2026-9041"
    val touristId: String,
    val touristName: String,
    val triggerSource: String, // ONE_TAP_SOS, AI_FALL_DETECTION, GEOFENCE_BREACH, INACTIVITY_TIMEOUT
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val assignedAgency: ResponseAgency,
    val latitude: Double,
    val longitude: Double,
    val zoneName: String,
    val riskScore: Int,
    val postureState: PoseState,
    val batteryLevel: Int,
    val telemetrySnapshot: String,
    val responderNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val blockchainProofHash: String = ""
)

/**
 * 8. Targeted CCTV Investigation Camera
 */
@Serializable
@Entity(tableName = "safety_cctv_cameras")
data class CctvCamera(
    @PrimaryKey val cameraId: String,
    val locationName: String,
    val zoneId: String,
    val latitude: Double,
    val longitude: Double,
    val activeDetections: String, // JSON list of detections (e.g. "FALL_DETECTED", "RUNNING")
    val matchedTouristId: String = "",
    val lastEventTimestamp: Long = System.currentTimeMillis(),
    val isLiveFeedActive: Boolean = true
)

/**
 * 9. Blockchain Trust Layer Block
 */
@Serializable
@Entity(tableName = "safety_blockchain_blocks")
data class BlockchainBlockEntity(
    @PrimaryKey val index: Long,
    val timestamp: Long,
    val transactionType: String, // TOURIST_REGISTRATION, CONSENT_UPDATE, INCIDENT_LOG, AUTHORITY_INVESTIGATION
    val payloadJson: String,
    val previousHash: String,
    val merkleRoot: String,
    val nonce: Long,
    val hash: String,
    val isValidated: Boolean = true
)
