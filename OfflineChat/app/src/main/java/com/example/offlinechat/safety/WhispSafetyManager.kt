package com.example.offlinechat.safety

import android.content.Context
import android.util.Log
import com.example.offlinechat.data.*
import com.example.offlinechat.network.HybridMeshTransport
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import kotlin.math.*

/**
 * 33-Landmark Pose Keypoint for MediaPipe simulation
 */
data class PoseLandmark(
    val id: Int,
    val name: String,
    val x: Float, // 0.0 to 1.0
    val y: Float, // 0.0 to 1.0
    val z: Float = 0.0f,
    val visibility: Float = 0.99f
)

/**
 * Real-time AI Risk Evaluation Snapshot
 */
data class AiRiskEvaluation(
    val score: Int, // 0 - 100
    val level: ThreatLevel,
    val zoneFactor: Int,
    val routeDeviationFactor: Int,
    val movementFactor: Int,
    val postureFactor: Int,
    val batteryFactor: Int,
    val primaryRiskReason: String,
    val evaluatedAt: Long = System.currentTimeMillis()
)

/**
 * Central Controller for Whisp Smart Tourist Safety & Incident Response System
 */
class WhispSafetyManager(
    private val context: Context,
    private val chatDao: ChatDao,
    private val transport: HybridMeshTransport
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 1. Current Tourist Profile & Consent
    private val _currentProfile = MutableStateFlow<TouristProfile?>(null)
    val currentProfile: StateFlow<TouristProfile?> = _currentProfile.asStateFlow()

    private val _consentSettings = MutableStateFlow(ConsentSettings())
    val consentSettings: StateFlow<ConsentSettings> = _consentSettings.asStateFlow()

    // 2. Simulated / Real GPS Location (e.g. Manali Valley default coordinates)
    private val _currentLatitude = MutableStateFlow(32.2432)
    val currentLatitude: StateFlow<Double> = _currentLatitude.asStateFlow()

    private val _currentLongitude = MutableStateFlow(77.1892)
    val currentLongitude: StateFlow<Double> = _currentLongitude.asStateFlow()

    private val _currentZone = MutableStateFlow<GeoFenceZone?>(null)
    val currentZone: StateFlow<GeoFenceZone?> = _currentZone.asStateFlow()

    private val _geoZoneAlertMessage = MutableStateFlow<String?>(null)
    val geoZoneAlertMessage: StateFlow<String?> = _geoZoneAlertMessage.asStateFlow()

    // 3. Pose & Keypoint Intelligence
    private val _currentPoseState = MutableStateFlow(PoseState.STANDING)
    val currentPoseState: StateFlow<PoseState> = _currentPoseState.asStateFlow()

    private val _keypointLandmarks = MutableStateFlow<List<PoseLandmark>>(emptyList())
    val keypointLandmarks: StateFlow<List<PoseLandmark>> = _keypointLandmarks.asStateFlow()

    // 4. AI Risk Score
    private val _aiRisk = MutableStateFlow(
        AiRiskEvaluation(
            score = 12,
            level = ThreatLevel.NORMAL,
            zoneFactor = 0,
            routeDeviationFactor = 0,
            movementFactor = 5,
            postureFactor = 5,
            batteryFactor = 2,
            primaryRiskReason = "Safe Tourist Corridor • Normal Posture"
        )
    )
    val aiRisk: StateFlow<AiRiskEvaluation> = _aiRisk.asStateFlow()

    // 5. Two-Stage SOS Verification Modal State
    private val _isTwoStageSosPromptActive = MutableStateFlow(false)
    val isTwoStageSosPromptActive: StateFlow<Boolean> = _isTwoStageSosPromptActive.asStateFlow()

    private val _twoStageCountdownSeconds = MutableStateFlow(20)
    val twoStageCountdownSeconds: StateFlow<Int> = _twoStageCountdownSeconds.asStateFlow()

    private val _twoStagePromptReason = MutableStateFlow("Automated AI Anomaly Detection")
    val twoStagePromptReason: StateFlow<String> = _twoStagePromptReason.asStateFlow()

    // 6. Active Trip
    private val _activeTrip = MutableStateFlow<TripItinerary?>(null)
    val activeTrip: StateFlow<TripItinerary?> = _activeTrip.asStateFlow()

    private val _activeCheckpoints = MutableStateFlow<List<TripCheckpoint>>(emptyList())
    val activeCheckpoints: StateFlow<List<TripCheckpoint>> = _activeCheckpoints.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null

    init {
        scope.launch {
            initializeDefaultData()
            generateKeypointsForPose(PoseState.STANDING)
            evaluateAiRisk()
            listenToActiveTrip()
        }
    }

    private suspend fun initializeDefaultData() {
        // A. Tourist Profile
        val existingProfile = chatDao.getTouristProfile("TID-IN-2026-8842")
        if (existingProfile == null) {
            val defaultProfile = TouristProfile(
                touristId = "TID-IN-2026-8842",
                fullName = "Alex Mercer",
                nationality = "Indian / Tourist",
                passportOrIdHash = "SHA256:7a9f20c4e1b853d9e802...",
                phone = "+91 98765 43210",
                emergencyContactName = "Sarah Mercer (Family)",
                emergencyContactPhone = "+91 91234 56789",
                bloodGroup = "O+ Positive",
                medicalNotes = "Asthma (Carries Inhaler), Penicillin Allergy",
                blockchainIdentityHash = computeSha256("TID-IN-2026-8842_AlexMercer_2026"),
                activeTripId = "TRIP-MANALI-01",
                qrCredentialPayload = "WHISP:ID:TID-IN-2026-8842|NAME:Alex Mercer|BLOOD:O+|BC_HASH:${computeSha256("TID-IN-2026-8842").take(16)}"
            )
            chatDao.insertTouristProfile(defaultProfile)
            _currentProfile.value = defaultProfile

            // Record registration block on blockchain
            recordBlockchainBlock(
                transactionType = "TOURIST_REGISTRATION",
                payload = JSONObject().apply {
                    put("touristId", defaultProfile.touristId)
                    put("name", defaultProfile.fullName)
                    put("bloodGroup", defaultProfile.bloodGroup)
                    put("blockchainHash", defaultProfile.blockchainIdentityHash)
                }.toString()
            )
        } else {
            _currentProfile.value = existingProfile
        }

        // B. Seed Smart Geo-Fence Zones
        val defaultZones = listOf(
            GeoFenceZone(
                zoneId = "ZONE-MALL-ROAD",
                name = "Mall Road Tourist Corridor",
                description = "High-security safe zone with tourist police posts and verified amenities.",
                zoneType = GeoZoneType.SAFE,
                centerLatitude = 32.2432,
                centerLongitude = 77.1892,
                radiusMeters = 800.0,
                riskWeight = 0,
                emergencyContactAgency = "TOURIST_POLICE_POST_1"
            ),
            GeoFenceZone(
                zoneId = "ZONE-SOLANG-CAMP",
                name = "Solang Valley Base Camp",
                description = "Authorized adventure staging camp with first-aid post and mountain rescue base.",
                zoneType = GeoZoneType.SAFE,
                centerLatitude = 32.3167,
                centerLongitude = 77.1578,
                radiusMeters = 1200.0,
                riskWeight = 5,
                emergencyContactAgency = "MOUNTAIN_RESCUE_MEDIC"
            ),
            GeoFenceZone(
                zoneId = "ZONE-SISSU-PASS",
                name = "Sissu Valley Unpaved Mountain Trail",
                description = "Moderate altitude rocky pass. Low mobile connectivity and steep slope gradients.",
                zoneType = GeoZoneType.CAUTION,
                centerLatitude = 32.4833,
                centerLongitude = 77.1167,
                radiusMeters = 1500.0,
                riskWeight = 35,
                emergencyContactAgency = "FOREST_RANGER_SISSU"
            ),
            GeoFenceZone(
                zoneId = "ZONE-BAGA-COAST",
                name = "Baga High Tide Rocky Shelf",
                description = "Strong riptide currents and slippery basalt rock formations.",
                zoneType = GeoZoneType.CAUTION,
                centerLatitude = 15.5553,
                centerLongitude = 73.7517,
                radiusMeters = 900.0,
                riskWeight = 40,
                emergencyContactAgency = "COASTAL_LIFEGUARDS"
            ),
            GeoFenceZone(
                zoneId = "ZONE-ROHTANG-CHASM",
                name = "Rohtang Chasm Landslide Hazard Zone",
                description = "CRITICAL: Active landslide zone, loose rock falls, strictly restricted beyond dusk.",
                zoneType = GeoZoneType.RESTRICTED,
                centerLatitude = 32.3716,
                centerLongitude = 77.2466,
                radiusMeters = 700.0,
                riskWeight = 90,
                emergencyContactAgency = "DISASTER_MANAGEMENT_TEAM"
            ),
            GeoFenceZone(
                zoneId = "ZONE-WILDLIFE-CORE",
                name = "Great Himalayan Wildlife Core Sanctuary",
                description = "Strictly prohibited entry without wildlife warden clearance. High predator activity.",
                zoneType = GeoZoneType.RESTRICTED,
                centerLatitude = 31.7500,
                centerLongitude = 77.5500,
                radiusMeters = 2000.0,
                riskWeight = 95,
                emergencyContactAgency = "FOREST_ENFORCEMENT"
            )
        )
        chatDao.insertGeoFenceZones(defaultZones)

        // C. Seed Trip Itinerary
        val checkpoints = listOf(
            TripCheckpoint("CP-01", "Mall Road Tourist Info Center", 32.2432, 77.1892, 0, true, System.currentTimeMillis() - 7200000),
            TripCheckpoint("CP-02", "Solang Adventure Base", 32.3167, 77.1578, 1, true, System.currentTimeMillis() - 3600000),
            TripCheckpoint("CP-03", "Anjani Mahadev Viewpoint", 32.3300, 77.1500, 2, false, 0L),
            TripCheckpoint("CP-04", "Beas Kund Trailhead Checkpoint", 32.3550, 77.1400, 3, false, 0L)
        )
        val defaultTrip = TripItinerary(
            tripId = "TRIP-MANALI-01",
            title = "Solang & Beas Kund Scenic Trek",
            destinationRegion = "Manali Valley, Himachal Pradesh",
            safeRouteScore = 92,
            waypointsJson = serializeCheckpoints(checkpoints),
            totalDistanceKm = 12.8,
            status = "ACTIVE",
            currentCheckpointIndex = 2
        )
        chatDao.insertTripItinerary(defaultTrip)
        _activeTrip.value = defaultTrip
        _activeCheckpoints.value = checkpoints

        // D. Seed Targeted CCTV Cameras
        val defaultCameras = listOf(
            CctvCamera("CAM-MANALI-01", "Mall Road North Entrance", "ZONE-MALL-ROAD", 32.2435, 77.1890, "[\"NORMAL_TRAFFIC\", \"WALKING\"]", "TID-IN-2026-8842"),
            CctvCamera("CAM-SOLANG-02", "Solang Base Cable Car Post", "ZONE-SOLANG-CAMP", 32.3170, 77.1580, "[\"WALKING\", \"HIKING\"]", "TID-IN-2026-8842"),
            CctvCamera("CAM-ROHTANG-04", "Rohtang Chasm Hazard Curve", "ZONE-ROHTANG-CHASM", 32.3720, 77.2470, "[\"NO_HUMAN_DETECTED\"]", ""),
            CctvCamera("CAM-SISSU-03", "Sissu Bridge Checkpoint", "ZONE-SISSU-PASS", 32.4840, 77.1170, "[\"RUNNING\", \"LOW_VISIBILITY\"]", "")
        )
        chatDao.insertCctvCameras(defaultCameras)

        // E. Seed Blockchain Genesis Block if empty
        val blockCount = chatDao.getLatestBlockchainBlock()
        if (blockCount == null) {
            val genesisBlock = BlockchainBlockEntity(
                index = 0L,
                timestamp = System.currentTimeMillis() - 86400000,
                transactionType = "GENESIS_LEDGER",
                payloadJson = "{\"ledger\":\"Whisp Tourist Trust & Safety Protocol v1.0\",\"authority\":\"Govt of India / Tourism Safety Dept\"}",
                previousHash = "0000000000000000000000000000000000000000000000000000000000000000",
                merkleRoot = computeSha256("GENESIS"),
                nonce = 1042L,
                hash = computeSha256("0_GENESIS_0000000000000000000000000000000000000000000000000000000000000000")
            )
            chatDao.insertBlockchainBlock(genesisBlock)
        }

        // F. Seed Active Sample Incidents
        val sampleIncident = SafetyIncident(
            incidentId = "INC-2026-4102",
            touristId = "TID-IN-2026-1049",
            touristName = "Rohan Sharma",
            triggerSource = "AI_FALL_DETECTION",
            severity = IncidentSeverity.HIGH,
            status = IncidentStatus.DISPATCHED,
            assignedAgency = ResponseAgency.MEDICAL_AMBULANCE,
            latitude = 32.3190,
            longitude = 77.1590,
            zoneName = "Solang Adventure Base",
            riskScore = 82,
            postureState = PoseState.FALL_DETECTED,
            batteryLevel = 45,
            telemetrySnapshot = "Rapid downward acceleration detected. User unresponsive for 20s.",
            responderNotes = "Ambulance Unit 4 dispatched with mountain paramedic stretcher.",
            blockchainProofHash = computeSha256("INC-2026-4102_FALL_PROOF")
        )
        chatDao.insertIncident(sampleIncident)
    }

    private fun listenToActiveTrip() {
        scope.launch {
            chatDao.getActiveTripItinerary().collect { trip ->
                _activeTrip.value = trip
                if (trip != null) {
                    _activeCheckpoints.value = parseCheckpoints(trip.waypointsJson)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 1. SELECTIVE CONSENT MANAGEMENT
    // -------------------------------------------------------------
    fun updateConsent(newSettings: ConsentSettings) {
        _consentSettings.value = newSettings
        scope.launch {
            recordBlockchainBlock(
                transactionType = "CONSENT_UPDATE",
                payload = JSONObject().apply {
                    put("touristId", _currentProfile.value?.touristId ?: "TID-ANONYMOUS")
                    put("shareLocation", newSettings.shareLocationWithAuthorities)
                    put("shareMedical", newSettings.shareMedicalDataInEmergency)
                    put("shareItinerary", newSettings.shareItineraryWithRangers)
                    put("biometricKeypoints", newSettings.biometricKeypointTracking)
                }.toString()
            )
        }
    }

    // -------------------------------------------------------------
    // 2. LOCATION UPDATE & SMART GEO-FENCING
    // -------------------------------------------------------------
    fun updateLocation(lat: Double, lon: Double) {
        _currentLatitude.value = lat
        _currentLongitude.value = lon
        scope.launch {
            checkGeoFences(lat, lon)
            evaluateAiRisk()
        }
    }

    private suspend fun checkGeoFences(lat: Double, lon: Double) {
        val zoneList = listOf(
            GeoFenceZone("ZONE-MALL-ROAD", "Mall Road Tourist Corridor", "Safe zone", GeoZoneType.SAFE, 32.2432, 77.1892, 800.0, 0),
            GeoFenceZone("ZONE-SOLANG-CAMP", "Solang Valley Base Camp", "Safe zone", GeoZoneType.SAFE, 32.3167, 77.1578, 1200.0, 5),
            GeoFenceZone("ZONE-SISSU-PASS", "Sissu Valley Unpaved Mountain Trail", "Caution zone", GeoZoneType.CAUTION, 32.4833, 77.1167, 1500.0, 35),
            GeoFenceZone("ZONE-ROHTANG-CHASM", "Rohtang Chasm Landslide Hazard Zone", "Danger zone", GeoZoneType.RESTRICTED, 32.3716, 77.2466, 700.0, 90)
        )

        var insideZone: GeoFenceZone? = null
        for (zone in zoneList) {
            val dist = calculateDistanceMeters(lat, lon, zone.centerLatitude, zone.centerLongitude)
            if (dist <= zone.radiusMeters) {
                insideZone = zone
                break
            }
        }

        _currentZone.value = insideZone
        if (insideZone != null) {
            when (insideZone.zoneType) {
                GeoZoneType.SAFE -> {
                    _geoZoneAlertMessage.value = "You are in a SAFE Tourist Corridor: ${insideZone.name}"
                }
                GeoZoneType.CAUTION -> {
                    _geoZoneAlertMessage.value = "CAUTION: Entering ${insideZone.name}. Maintain trail awareness."
                }
                GeoZoneType.RESTRICTED -> {
                    _geoZoneAlertMessage.value = "ALERT: RESTRICTED DANGER ZONE! ${insideZone.name}. Entry prohibited without escort!"
                    // Trigger Two-Stage safety verification if in danger zone
                    triggerTwoStageSosVerification("Approaching Restricted Hazard Zone: ${insideZone.name}")
                }
            }
        } else {
            _geoZoneAlertMessage.value = "Exploring open tourist route. GPS tracking active."
        }
    }

    // -------------------------------------------------------------
    // 3. KEYPOINT INTELLIGENCE & MEDIAPIPE POSE
    // -------------------------------------------------------------
    fun setPoseState(pose: PoseState) {
        _currentPoseState.value = pose
        generateKeypointsForPose(pose)
        scope.launch {
            evaluateAiRisk()
            if (pose == PoseState.FALL_DETECTED) {
                triggerTwoStageSosVerification("MediaPipe Fall Detection Signal Detected!")
            }
        }
    }

    private fun generateKeypointsForPose(pose: PoseState) {
        val landmarks = mutableListOf<PoseLandmark>()
        when (pose) {
            PoseState.STANDING -> {
                // Nose
                landmarks.add(PoseLandmark(0, "Nose", 0.5f, 0.2f))
                // Shoulders
                landmarks.add(PoseLandmark(11, "Left Shoulder", 0.42f, 0.32f))
                landmarks.add(PoseLandmark(12, "Right Shoulder", 0.58f, 0.32f))
                // Elbows
                landmarks.add(PoseLandmark(13, "Left Elbow", 0.38f, 0.45f))
                landmarks.add(PoseLandmark(14, "Right Elbow", 0.62f, 0.45f))
                // Wrists
                landmarks.add(PoseLandmark(15, "Left Wrist", 0.36f, 0.58f))
                landmarks.add(PoseLandmark(16, "Right Wrist", 0.64f, 0.58f))
                // Hips
                landmarks.add(PoseLandmark(23, "Left Hip", 0.45f, 0.58f))
                landmarks.add(PoseLandmark(24, "Right Hip", 0.55f, 0.58f))
                // Knees
                landmarks.add(PoseLandmark(25, "Left Knee", 0.45f, 0.76f))
                landmarks.add(PoseLandmark(26, "Right Knee", 0.55f, 0.76f))
                // Ankles
                landmarks.add(PoseLandmark(27, "Left Ankle", 0.45f, 0.92f))
                landmarks.add(PoseLandmark(28, "Right Ankle", 0.55f, 0.92f))
            }
            PoseState.WALKING -> {
                landmarks.add(PoseLandmark(0, "Nose", 0.52f, 0.21f))
                landmarks.add(PoseLandmark(11, "Left Shoulder", 0.44f, 0.33f))
                landmarks.add(PoseLandmark(12, "Right Shoulder", 0.60f, 0.33f))
                landmarks.add(PoseLandmark(13, "Left Elbow", 0.40f, 0.46f))
                landmarks.add(PoseLandmark(14, "Right Elbow", 0.64f, 0.44f))
                landmarks.add(PoseLandmark(15, "Left Wrist", 0.42f, 0.56f))
                landmarks.add(PoseLandmark(16, "Right Wrist", 0.68f, 0.52f))
                landmarks.add(PoseLandmark(23, "Left Hip", 0.47f, 0.58f))
                landmarks.add(PoseLandmark(24, "Right Hip", 0.57f, 0.58f))
                landmarks.add(PoseLandmark(25, "Left Knee", 0.42f, 0.74f))
                landmarks.add(PoseLandmark(26, "Right Knee", 0.62f, 0.77f))
                landmarks.add(PoseLandmark(27, "Left Ankle", 0.38f, 0.90f))
                landmarks.add(PoseLandmark(28, "Right Ankle", 0.66f, 0.93f))
            }
            PoseState.RUNNING -> {
                landmarks.add(PoseLandmark(0, "Nose", 0.55f, 0.24f))
                landmarks.add(PoseLandmark(11, "Left Shoulder", 0.46f, 0.35f))
                landmarks.add(PoseLandmark(12, "Right Shoulder", 0.64f, 0.35f))
                landmarks.add(PoseLandmark(13, "Left Elbow", 0.38f, 0.42f))
                landmarks.add(PoseLandmark(14, "Right Elbow", 0.72f, 0.48f))
                landmarks.add(PoseLandmark(15, "Left Wrist", 0.44f, 0.36f))
                landmarks.add(PoseLandmark(16, "Right Wrist", 0.76f, 0.60f))
                landmarks.add(PoseLandmark(23, "Left Hip", 0.50f, 0.58f))
                landmarks.add(PoseLandmark(24, "Right Hip", 0.60f, 0.58f))
                landmarks.add(PoseLandmark(25, "Left Knee", 0.36f, 0.70f))
                landmarks.add(PoseLandmark(26, "Right Knee", 0.72f, 0.75f))
                landmarks.add(PoseLandmark(27, "Left Ankle", 0.28f, 0.85f))
                landmarks.add(PoseLandmark(28, "Right Ankle", 0.78f, 0.92f))
            }
            PoseState.FALL_DETECTED, PoseState.PROLONGED_INACTIVITY -> {
                // Horizontal fallen orientation
                landmarks.add(PoseLandmark(0, "Nose", 0.22f, 0.78f))
                landmarks.add(PoseLandmark(11, "Left Shoulder", 0.32f, 0.74f))
                landmarks.add(PoseLandmark(12, "Right Shoulder", 0.32f, 0.82f))
                landmarks.add(PoseLandmark(13, "Left Elbow", 0.26f, 0.70f))
                landmarks.add(PoseLandmark(14, "Right Elbow", 0.28f, 0.88f))
                landmarks.add(PoseLandmark(15, "Left Wrist", 0.18f, 0.72f))
                landmarks.add(PoseLandmark(16, "Right Wrist", 0.20f, 0.89f))
                landmarks.add(PoseLandmark(23, "Left Hip", 0.52f, 0.75f))
                landmarks.add(PoseLandmark(24, "Right Hip", 0.52f, 0.82f))
                landmarks.add(PoseLandmark(25, "Left Knee", 0.68f, 0.72f))
                landmarks.add(PoseLandmark(26, "Right Knee", 0.70f, 0.84f))
                landmarks.add(PoseLandmark(27, "Left Ankle", 0.84f, 0.74f))
                landmarks.add(PoseLandmark(28, "Right Ankle", 0.86f, 0.85f))
            }
        }
        _keypointLandmarks.value = landmarks
    }

    // -------------------------------------------------------------
    // 4. AI RISK ENGINE SCORING
    // -------------------------------------------------------------
    private fun evaluateAiRisk() {
        val zone = _currentZone.value
        val zoneRisk = when (zone?.zoneType) {
            GeoZoneType.SAFE -> 5
            GeoZoneType.CAUTION -> 35
            GeoZoneType.RESTRICTED -> 85
            null -> 15
        }

        val postureRisk = when (_currentPoseState.value) {
            PoseState.STANDING -> 5
            PoseState.WALKING -> 8
            PoseState.RUNNING -> 20
            PoseState.FALL_DETECTED -> 60
            PoseState.PROLONGED_INACTIVITY -> 45
        }

        val deviationRisk = 10 // baseline route deviation
        val movementRisk = 8
        val batteryRisk = 5

        val totalScore = min(100, (zoneRisk * 0.4 + postureRisk * 0.4 + deviationRisk * 0.1 + movementRisk * 0.05 + batteryRisk * 0.05).roundToInt())

        val level = when {
            totalScore >= 85 -> ThreatLevel.CRITICAL
            totalScore >= 70 -> ThreatLevel.HIGH
            totalScore >= 50 -> ThreatLevel.ELEVATED
            totalScore >= 25 -> ThreatLevel.LOW
            else -> ThreatLevel.NORMAL
        }

        val reason = when {
            _currentPoseState.value == PoseState.FALL_DETECTED -> "CRITICAL: Fall Detected by Keypoint Engine"
            zone?.zoneType == GeoZoneType.RESTRICTED -> "HIGH: Tourist in Restricted Danger Zone (${zone.name})"
            zone?.zoneType == GeoZoneType.CAUTION -> "ELEVATED: Travelling in Caution Zone (${zone.name})"
            _currentPoseState.value == PoseState.RUNNING -> "ELEVATED: High Velocity Movement / Running"
            else -> "SAFE: Normal Route Progression & Posture"
        }

        _aiRisk.value = AiRiskEvaluation(
            score = totalScore,
            level = level,
            zoneFactor = zoneRisk,
            routeDeviationFactor = deviationRisk,
            movementFactor = movementRisk,
            postureFactor = postureRisk,
            batteryFactor = batteryRisk,
            primaryRiskReason = reason
        )
    }

    // -------------------------------------------------------------
    // 5. TWO-STAGE INTELLIGENT SOS VERIFICATION
    // -------------------------------------------------------------
    fun triggerTwoStageSosVerification(reason: String) {
        if (_isTwoStageSosPromptActive.value) return

        _twoStagePromptReason.value = reason
        _twoStageCountdownSeconds.value = 20
        _isTwoStageSosPromptActive.value = true

        countdownJob?.cancel()
        countdownJob = scope.launch {
            for (i in 20 downTo 1) {
                _twoStageCountdownSeconds.value = i
                delay(1000)
            }
            // Auto-escalate if not dismissed
            if (_isTwoStageSosPromptActive.value) {
                _isTwoStageSosPromptActive.value = false
                triggerInstantSos(
                    triggerSource = "AUTO_ESCALATION_TIMEOUT",
                    notes = "Automated escalation after 20s safety check timeout. Reason: $reason"
                )
            }
        }
    }

    fun dismissTwoStageSos(userMarkedSafe: Boolean) {
        countdownJob?.cancel()
        _isTwoStageSosPromptActive.value = false
        if (userMarkedSafe) {
            setPoseState(PoseState.STANDING)
            scope.launch {
                recordBlockchainBlock(
                    transactionType = "SAFETY_CONFIRMATION",
                    payload = "{\"touristId\":\"${_currentProfile.value?.touristId}\",\"status\":\"CONFIRMED_SAFE\",\"timestamp\":${System.currentTimeMillis()}}"
                )
            }
        }
    }

    // -------------------------------------------------------------
    // 6. INSTANT SOS & INCIDENT DISPATCH
    // -------------------------------------------------------------
    fun triggerInstantSos(
        triggerSource: String = "ONE_TAP_SOS",
        notes: String = "Manual One-Tap Emergency SOS Triggered by Tourist"
    ) {
        countdownJob?.cancel()
        _isTwoStageSosPromptActive.value = false

        val profile = _currentProfile.value
        val incidentId = "INC-2026-${(1000..9999).random()}"
        val lat = _currentLatitude.value
        val lon = _currentLongitude.value
        val zoneName = _currentZone.value?.name ?: "Open Route Coordinates"
        val riskScore = _aiRisk.value.score
        val pose = _currentPoseState.value

        val assignedAgency = when {
            pose == PoseState.FALL_DETECTED -> ResponseAgency.MEDICAL_AMBULANCE
            _currentZone.value?.zoneType == GeoZoneType.RESTRICTED -> ResponseAgency.DISASTER_MANAGEMENT
            else -> ResponseAgency.POLICE_CONTROL
        }

        val incident = SafetyIncident(
            incidentId = incidentId,
            touristId = profile?.touristId ?: "TID-ANONYMOUS",
            touristName = profile?.fullName ?: "Anonymous Tourist",
            triggerSource = triggerSource,
            severity = IncidentSeverity.CRITICAL,
            status = IncidentStatus.REPORTED,
            assignedAgency = assignedAgency,
            latitude = lat,
            longitude = lon,
            zoneName = zoneName,
            riskScore = riskScore,
            postureState = pose,
            batteryLevel = 78,
            telemetrySnapshot = "Trigger: $triggerSource | Zone: $zoneName | Coordinates: ($lat, $lon) | Risk: $riskScore% | Pose: $pose",
            responderNotes = notes,
            blockchainProofHash = computeSha256("$incidentId:$lat:$lon:${System.currentTimeMillis()}")
        )

        scope.launch {
            // A. Store in Database
            chatDao.insertIncident(incident)

            // B. Broadcast via High-Priority Mesh Packet (Priority 100)
            val sosPayload = JSONObject().apply {
                put("type", "WHISP_SOS_ALERT")
                put("incidentId", incident.incidentId)
                put("touristId", incident.touristId)
                put("touristName", incident.touristName)
                put("lat", incident.latitude)
                put("lon", incident.longitude)
                put("zone", incident.zoneName)
                put("severity", incident.severity.name)
                put("agency", incident.assignedAgency.name)
                put("proofHash", incident.blockchainProofHash)
            }.toString()

            val meshPacket = MeshPacket(
                protocolVersion = 4,
                packetType = PacketType.SOS,
                senderId = transport.localId,
                recipientId = "ALL",
                payload = sosPayload,
                priority = com.example.offlinechat.network.PacketPriority.SOS
            )
            transport.sendData(meshPacket.toJsonString().toByteArray(Charsets.UTF_8))

            // C. Record onto Blockchain Trust Ledger
            recordBlockchainBlock(
                transactionType = "INCIDENT_LOG",
                payload = sosPayload
            )
        }
    }

    fun updateIncidentStatus(incidentId: String, status: IncidentStatus, agency: ResponseAgency, notes: String) {
        scope.launch {
            chatDao.updateIncidentStatus(incidentId, status, agency, notes)
            recordBlockchainBlock(
                transactionType = "AUTHORITY_INVESTIGATION",
                payload = JSONObject().apply {
                    put("incidentId", incidentId)
                    put("status", status.name)
                    put("assignedAgency", agency.name)
                    put("notes", notes)
                }.toString()
            )
        }
    }

    // -------------------------------------------------------------
    // 7. TRIP & CHECKPOINTS CHECK-IN
    // -------------------------------------------------------------
    fun checkInCheckpoint(checkpointId: String) {
        val trip = _activeTrip.value ?: return
        val checkpoints = _activeCheckpoints.value.toMutableList()
        val index = checkpoints.indexOfFirst { it.checkpointId == checkpointId }
        if (index != -1) {
            val updated = checkpoints[index].copy(
                isCheckedIn = true,
                checkInTimestamp = System.currentTimeMillis()
            )
            checkpoints[index] = updated
            _activeCheckpoints.value = checkpoints

            val nextIndex = min(checkpoints.size - 1, index + 1)
            val isCompleted = checkpoints.all { it.isCheckedIn }

            scope.launch {
                chatDao.updateTripProgress(
                    tripId = trip.tripId,
                    waypointsJson = serializeCheckpoints(checkpoints),
                    currentIndex = nextIndex,
                    status = if (isCompleted) "COMPLETED" else "ACTIVE"
                )
                recordBlockchainBlock(
                    transactionType = "CHECKPOINT_CHECKIN",
                    payload = JSONObject().apply {
                        put("tripId", trip.tripId)
                        put("checkpointId", checkpointId)
                        put("checkpointName", updated.name)
                        put("timestamp", updated.checkInTimestamp)
                    }.toString()
                )
            }
        }
    }

    // -------------------------------------------------------------
    // 8. BLOCKCHAIN TRUST LAYER ENGINE
    // -------------------------------------------------------------
    private suspend fun recordBlockchainBlock(transactionType: String, payload: String) {
        val lastBlock = chatDao.getLatestBlockchainBlock()
        val nextIndex = (lastBlock?.index ?: -1L) + 1L
        val prevHash = lastBlock?.hash ?: "0000000000000000000000000000000000000000000000000000000000000000"
        val timestamp = System.currentTimeMillis()
        val merkleRoot = computeSha256(payload)
        val nonce = (1000L..9999L).random()
        val currentHash = computeSha256("${nextIndex}_${timestamp}_${transactionType}_${prevHash}_${merkleRoot}_${nonce}")

        val newBlock = BlockchainBlockEntity(
            index = nextIndex,
            timestamp = timestamp,
            transactionType = transactionType,
            payloadJson = payload,
            previousHash = prevHash,
            merkleRoot = merkleRoot,
            nonce = nonce,
            hash = currentHash,
            isValidated = true
        )
        chatDao.insertBlockchainBlock(newBlock)
    }

    // -------------------------------------------------------------
    // UTILITY CALCULATIONS
    // -------------------------------------------------------------
    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun computeSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun serializeCheckpoints(list: List<TripCheckpoint>): String {
        val array = JSONArray()
        list.forEach { cp ->
            array.put(JSONObject().apply {
                put("checkpointId", cp.checkpointId)
                put("name", cp.name)
                put("latitude", cp.latitude)
                put("longitude", cp.longitude)
                put("orderIndex", cp.orderIndex)
                put("isCheckedIn", cp.isCheckedIn)
                put("checkInTimestamp", cp.checkInTimestamp)
            })
        }
        return array.toString()
    }

    private fun parseCheckpoints(json: String): List<TripCheckpoint> {
        val list = mutableListOf<TripCheckpoint>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TripCheckpoint(
                        checkpointId = obj.getString("checkpointId"),
                        name = obj.getString("name"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        orderIndex = obj.getInt("orderIndex"),
                        isCheckedIn = obj.optBoolean("isCheckedIn", false),
                        checkInTimestamp = obj.optLong("checkInTimestamp", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("WhispSafetyManager", "Error parsing checkpoints JSON", e)
        }
        return list
    }

    companion object {
        @Volatile
        private var instance: WhispSafetyManager? = null

        fun getInstance(context: Context, chatDao: ChatDao, transport: HybridMeshTransport): WhispSafetyManager {
            return instance ?: synchronized(this) {
                instance ?: WhispSafetyManager(context.applicationContext, chatDao, transport).also { instance = it }
            }
        }
    }
}
