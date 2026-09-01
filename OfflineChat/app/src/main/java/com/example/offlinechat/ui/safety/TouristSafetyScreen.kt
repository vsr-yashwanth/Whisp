package com.example.offlinechat.ui.safety

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.data.*
import com.example.offlinechat.safety.PoseLandmark
import com.example.offlinechat.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouristSafetyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuthorityView: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as OfflineChatApp
    val safetyManager = app.safetyManager
    val chatDao = app.database.chatDao()

    val currentProfile by safetyManager.currentProfile.collectAsState()
    val consentSettings by safetyManager.consentSettings.collectAsState()
    val currentLat by safetyManager.currentLatitude.collectAsState()
    val currentLon by safetyManager.currentLongitude.collectAsState()
    val currentZone by safetyManager.currentZone.collectAsState()
    val geoZoneAlertMsg by safetyManager.geoZoneAlertMessage.collectAsState()

    val currentPose by safetyManager.currentPoseState.collectAsState()
    val landmarks by safetyManager.keypointLandmarks.collectAsState()
    val aiRisk by safetyManager.aiRisk.collectAsState()

    val isSosPromptActive by safetyManager.isTwoStageSosPromptActive.collectAsState()
    val sosCountdown by safetyManager.twoStageCountdownSeconds.collectAsState()
    val sosReason by safetyManager.twoStagePromptReason.collectAsState()

    val activeTrip by safetyManager.activeTrip.collectAsState()
    val checkpoints by safetyManager.activeCheckpoints.collectAsState()

    val blockchainBlocks by chatDao.getAllBlockchainBlocks().collectAsState(initial = emptyList())

    // 0: DIGITAL ID & QR, 1: SMART TRIP & RADAR, 2: AI RISK & POSE, 3: BLOCKCHAIN TRUST
    var selectedTab by remember { mutableStateOf(0) }
    var showQrVerificationModal by remember { mutableStateOf(false) }

    // Two-Stage SOS Countdown Modal Dialog
    if (isSosPromptActive) {
        Dialog(onDismissRequest = { /* Prevent dismiss without safety action */ }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E0A0E))
                    .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$sosCountdown",
                            color = Color(0xFFEF4444),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "EMERGENCY SAFETY VERIFICATION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCA5A5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sosReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Auto-dispatching SOS to emergency authorities & rangers in $sosCountdown seconds...",
                        fontSize = 11.sp,
                        color = Color(0xFFF87171),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { safetyManager.dismissTwoStageSos(userMarkedSafe = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I AM SAFE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { safetyManager.triggerInstantSos("IMMEDIATE_USER_CONFIRMATION", "User confirmed emergency SOS immediately during safety check.") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DISPATCH SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // QR Verification Fullscreen Dialog
    if (showQrVerificationModal && currentProfile != null) {
        Dialog(onDismissRequest = { showQrVerificationModal = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, SignalEmerald, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VERIFIED DIGITAL TOURIST ID", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Stylized High-Tech QR Pattern Canvas
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellSize = size.width / 10f
                            val hash = currentProfile!!.blockchainIdentityHash
                            for (r in 0 until 10) {
                                for (c in 0 until 10) {
                                    val isCorner = (r < 3 && c < 3) || (r < 3 && c >= 7) || (r >= 7 && c < 3)
                                    val isFilled = isCorner || (hash.getOrElse((r * 10 + c) % hash.length) { '0' }.code % 2 == 0)
                                    if (isFilled) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(currentProfile!!.fullName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("TID: ${currentProfile!!.touristId}", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cryptographically verifiable by Police & Park Rangers offline without internet", color = TextMuted, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showQrVerificationModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SignalEmerald.copy(alpha = 0.2f))
                                .border(1.dp, SignalEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "WHISP TOURIST SAFETY",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite
                            )
                            Text(
                                "AI Sentinel • Geo-Fencing Radar • Digital ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAuthorityView) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Authority View", tint = SignalEmerald)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBlack,
                    titleContentColor = PureWhite
                )
            )
        },
        containerColor = ObsidianBlack
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Top Risk & Threat Level HUD
                val threatColor = when (aiRisk.level) {
                    ThreatLevel.NORMAL -> SignalEmerald
                    ThreatLevel.LOW -> Color(0xFF38BDF8)
                    ThreatLevel.ELEVATED -> Color(0xFFFBBF24)
                    ThreatLevel.HIGH -> Color(0xFFF97316)
                    ThreatLevel.CRITICAL -> Color(0xFFEF4444)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF141824), Color(0xFF10131C))
                            )
                        )
                        .border(1.dp, threatColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(threatColor.copy(alpha = 0.15f))
                                .border(1.5.dp, threatColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${aiRisk.score}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = threatColor
                                )
                                Text("RISK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = threatColor)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "THREAT LEVEL: ${aiRisk.level.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = threatColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = aiRisk.primaryRiskReason,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        // One-Tap High Priority SOS Trigger
                        Button(
                            onClick = {
                                safetyManager.triggerInstantSos("ONE_TAP_SOS_BUTTON", "One-Tap SOS Button pressed on Tourist HUD")
                                Toast.makeText(context, "High-Priority SOS Dispatched over Mesh!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SOS", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs: DIGITAL ID | SMART TRIP | AI & POSE | BLOCKCHAIN
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(4.dp)
                ) {
                    listOf("DIGITAL ID", "SMART TRIP", "AI POSE", "BLOCKCHAIN").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == index) SurfaceElevated else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) SignalEmerald else TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =========================================================================
            // TAB 0: DIGITAL TOURIST ID & SELECTIVE CONSENT
            // =========================================================================
            if (selectedTab == 0 && currentProfile != null) {
                item {
                    // 1. Digital Tourist ID Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF16202E), Color(0xFF0F141E))
                                )
                            )
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VERIFIED DIGITAL TOURIST ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), letterSpacing = 1.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SignalEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ACTIVE & SECURE", color = SignalEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                        .border(1.dp, Color(0xFF38BDF8), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentProfile!!.fullName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(currentProfile!!.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PureWhite)
                                    Text("ID: ${currentProfile!!.touristId} • ${currentProfile!!.nationality}", fontSize = 11.sp, color = TextSecondary)
                                }
                                IconButton(
                                    onClick = { showQrVerificationModal = true },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = "View QR", tint = SignalEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = SurfaceBorderSubtle, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Medical & Emergency Strip
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("BLOOD GROUP", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(currentProfile!!.bloodGroup, fontSize = 12.sp, color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("EMERGENCY CONTACT", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(currentProfile!!.emergencyContactName, fontSize = 12.sp, color = PureWhite, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("CONTACT PHONE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(currentProfile!!.emergencyContactPhone, fontSize = 12.sp, color = SignalEmerald, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Selective Privacy Consent Management
                    Text(
                        "PRIVACY-FIRST SELECTIVE CONSENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            ConsentToggleRow(
                                title = "Share Location with Emergency Authorities",
                                subtitle = "Provides real-time GPS only during active SOS or danger zone breach",
                                isChecked = consentSettings.shareLocationWithAuthorities,
                                onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(shareLocationWithAuthorities = it)) }
                            )
                            Divider(color = SurfaceBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                            ConsentToggleRow(
                                title = "Share Medical & Blood Data in Emergency",
                                subtitle = "Decrypts medical notes for first responders upon SOS trigger",
                                isChecked = consentSettings.shareMedicalDataInEmergency,
                                onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(shareMedicalDataInEmergency = it)) }
                            )
                            Divider(color = SurfaceBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                            ConsentToggleRow(
                                title = "Share Itinerary with Forest Rangers",
                                subtitle = "Allows check-in progress verification along mountain trails",
                                isChecked = consentSettings.shareItineraryWithRangers,
                                onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(shareItineraryWithRangers = it)) }
                            )
                            Divider(color = SurfaceBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))
                            ConsentToggleRow(
                                title = "Biometric MediaPipe Keypoint Tracking",
                                subtitle = "Enables fall & distress posture anomaly detection locally on device",
                                isChecked = consentSettings.biometricKeypointTracking,
                                onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(biometricKeypointTracking = it)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // =========================================================================
            // TAB 1: SMART TRIP & GEO-FENCE RADAR
            // =========================================================================
            if (selectedTab == 1) {
                item {
                    // Active Trip Header
                    if (activeTrip != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(activeTrip!!.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PureWhite)
                                        Text(activeTrip!!.destinationRegion, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SignalEmerald.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("SAFETY ${activeTrip!!.safeRouteScore}%", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                // Checkpoints progress
                                checkpoints.forEachIndexed { idx, cp ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (cp.isCheckedIn) SignalEmerald else SurfaceElevated)
                                                .border(1.dp, if (cp.isCheckedIn) SignalEmerald else TitaniumDim, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (cp.isCheckedIn) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(14.dp))
                                            } else {
                                                Text("${idx + 1}", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(cp.name, fontSize = 12.sp, color = if (cp.isCheckedIn) PureWhite else TextMuted, fontWeight = FontWeight.SemiBold)
                                            if (cp.isCheckedIn) {
                                                Text("Checked in", fontSize = 9.sp, color = SignalEmerald)
                                            }
                                        }
                                        if (!cp.isCheckedIn) {
                                            OutlinedButton(
                                                onClick = {
                                                    safetyManager.checkInCheckpoint(cp.checkpointId)
                                                    Toast.makeText(context, "Checked into ${cp.name}!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalEmerald),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, SignalEmerald),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("CHECK-IN", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Smart Geo-Fence Radar
                    Text("LIVE GEO-FENCING RADAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B0F16))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Radar Sweep Animation
                        val infiniteTransition = rememberInfiniteTransition(label = "radar")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(4000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "radarAngle"
                        )

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = size.width.coerceAtMost(size.height) / 2

                            // Draw radar concentric circles
                            drawCircle(color = Color(0xFF1E293B), radius = maxRadius * 0.33f, center = center, style = Stroke(1f))
                            drawCircle(color = Color(0xFF1E293B), radius = maxRadius * 0.66f, center = center, style = Stroke(1f))
                            drawCircle(color = Color(0xFF1E293B), radius = maxRadius, center = center, style = Stroke(1f))

                            // Draw Crosshairs
                            drawLine(color = Color(0xFF1E293B), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1f)
                            drawLine(color = Color(0xFF1E293B), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1f)

                            // Sweep Line
                            val sweepX = center.x + maxRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                            val sweepY = center.y + maxRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                            drawLine(
                                color = SignalEmerald.copy(alpha = 0.5f),
                                start = center,
                                end = Offset(sweepX, sweepY),
                                strokeWidth = 2f
                            )

                            // Safe Zone Blip (Green)
                            drawCircle(color = SignalEmerald, radius = 6f, center = Offset(center.x - 40f, center.y - 30f))
                            // Caution Zone Blip (Yellow)
                            drawCircle(color = Color(0xFFFBBF24), radius = 6f, center = Offset(center.x + 55f, center.y + 40f))
                            // Restricted Danger Zone Blip (Red)
                            drawCircle(color = Color(0xFFEF4444), radius = 7f, center = Offset(center.x - 65f, center.y + 60f))

                            // Tourist Position Center Blip
                            drawCircle(color = Color(0xFF38BDF8), radius = 8f, center = center)
                            drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.3f), radius = 16f, center = center)
                        }

                        // Bottom Overlay Info
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GPS: ${"%.4f".format(currentLat)}, ${"%.4f".format(currentLon)} • ${currentZone?.name ?: "Open Trail"}",
                                fontSize = 10.sp,
                                color = PureWhite,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (geoZoneAlertMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (currentZone?.zoneType) {
                                        GeoZoneType.RESTRICTED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        GeoZoneType.CAUTION -> Color(0xFFFBBF24).copy(alpha = 0.15f)
                                        else -> SignalEmerald.copy(alpha = 0.15f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (currentZone?.zoneType) {
                                        GeoZoneType.RESTRICTED -> Color(0xFFEF4444)
                                        GeoZoneType.CAUTION -> Color(0xFFFBBF24)
                                        else -> SignalEmerald
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = geoZoneAlertMsg!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (currentZone?.zoneType) {
                                    GeoZoneType.RESTRICTED -> Color(0xFFFCA5A5)
                                    GeoZoneType.CAUTION -> Color(0xFFFDE68A)
                                    else -> Color(0xFFA7F3D0)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location Simulation Controls (For Live SIH Demo Testing)
                    Text("LOCATION & ZONE SIMULATOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { safetyManager.updateLocation(32.2432, 77.1892) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = SignalEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Safe Hub", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { safetyManager.updateLocation(32.4833, 77.1167) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Caution Trail", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { safetyManager.updateLocation(32.3716, 77.2466) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Danger Hazard", fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // =========================================================================
            // TAB 2: AI RISK ENGINE & MEDIAPIPE KEYPOINT POSE
            // =========================================================================
            if (selectedTab == 2) {
                item {
                    // Posture State HUD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentPose == PoseState.FALL_DETECTED) Icons.Rounded.Warning else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = if (currentPose == PoseState.FALL_DETECTED) Color(0xFFEF4444) else SignalEmerald
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CURRENT POSTURE: ${currentPose.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PureWhite)
                                Text("33-Landmark MediaPipe Pose Estimation Engine", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 33-Landmark Skeletal Visualizer Canvas
                    Text("LIVE 33-POINT SKELETAL OVERLAY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0A0D14))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val skeletonColor = if (currentPose == PoseState.FALL_DETECTED) Color(0xFFEF4444) else SignalEmerald

                        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            val w = size.width
                            val h = size.height

                            // Draw bones connecting landmarks
                            val map = landmarks.associateBy { it.id }

                            fun drawBone(id1: Int, id2: Int) {
                                val p1 = map[id1]
                                val p2 = map[id2]
                                if (p1 != null && p2 != null) {
                                    drawLine(
                                        color = skeletonColor.copy(alpha = 0.7f),
                                        start = Offset(p1.x * w, p1.y * h),
                                        end = Offset(p2.x * w, p2.y * h),
                                        strokeWidth = 3f
                                    )
                                }
                            }

                            // Torso & Shoulders
                            drawBone(11, 12)
                            drawBone(11, 23)
                            drawBone(12, 24)
                            drawBone(23, 24)
                            // Left Arm
                            drawBone(11, 13)
                            drawBone(13, 15)
                            // Right Arm
                            drawBone(12, 14)
                            drawBone(14, 16)
                            // Left Leg
                            drawBone(23, 25)
                            drawBone(25, 27)
                            // Right Leg
                            drawBone(24, 26)
                            drawBone(26, 28)

                            // Draw Landmark Nodes
                            landmarks.forEach { lm ->
                                drawCircle(
                                    color = skeletonColor,
                                    radius = 5f,
                                    center = Offset(lm.x * w, lm.y * h)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5f,
                                    center = Offset(lm.x * w, lm.y * h)
                                )
                            }
                        }

                        // Overlay Tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MEDIAPIPE POSE 33", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = skeletonColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Posture Simulation Buttons
                    Text("SIMULATE MOVEMENT / FALL ANOMALY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { safetyManager.setPoseState(PoseState.STANDING) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = PureWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Standing", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { safetyManager.setPoseState(PoseState.WALKING) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = PureWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Walking", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { safetyManager.setPoseState(PoseState.RUNNING) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Running", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { safetyManager.setPoseState(PoseState.FALL_DETECTED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Sim Fall", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // =========================================================================
            // TAB 3: BLOCKCHAIN TRUST LEDGER
            // =========================================================================
            if (selectedTab == 3) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("BLOCKCHAIN TRUST LAYER", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PureWhite)
                                    Text("Immutable SHA-256 Ledger of Consents, IDs & SOS Incidents", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                items(blockchainBlocks) { block ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF10141E))
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("BLOCK #${block.index}", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SurfaceElevated)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(block.transactionType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(block.timestamp)),
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("HASH: ${block.hash.take(16)}...${block.hash.takeLast(8)}", fontSize = 10.sp, color = PureWhite, fontFamily = FontFamily.Monospace)
                            Text("PREV: ${block.previousHash.take(16)}...", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PAYLOAD: ${block.payloadJson}", fontSize = 10.sp, color = TextSecondary, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsentToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PureWhite)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = SignalEmerald,
                uncheckedThumbColor = TitaniumLight,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}
