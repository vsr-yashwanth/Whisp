package com.example.offlinechat.ui.safety

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import com.example.offlinechat.ui.theme.*
import java.util.Locale

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

    val currentPose by safetyManager.currentPoseState.collectAsState()
    val landmarks by safetyManager.keypointLandmarks.collectAsState()
    val aiRisk by safetyManager.aiRisk.collectAsState()

    val isSosPromptActive by safetyManager.isTwoStageSosPromptActive.collectAsState()
    val sosCountdown by safetyManager.twoStageCountdownSeconds.collectAsState()
    val sosReason by safetyManager.twoStagePromptReason.collectAsState()

    val blockchainBlocks by chatDao.getAllBlockchainBlocks().collectAsState(initial = emptyList())

    // 0: DIGITAL ID & QR, 1: 360 RADAR, 2: AI RISK & POSE, 3: BLOCKCHAIN TRUST
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
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sosCountdown}s",
                            color = Color(0xFFEF4444),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "EMERGENCY VERIFICATION",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sosReason,
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "If you do not respond, high-priority emergency alerts will automatically be broadcast over the mesh.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { safetyManager.dismissTwoStageSos(userMarkedSafe = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("I AM SAFE (CANCEL)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { safetyManager.triggerInstantSos("USER_ESCALATED_SOS", "User dispatched immediate SOS from modal") },
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("DISPATCH SOS IMMEDIATELY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // QR Verification Modal
    if (showQrVerificationModal && currentProfile != null) {
        val prof = currentProfile!!
        Dialog(onDismissRequest = { showQrVerificationModal = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SignalEmerald, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VERIFIABLE TOURIST QR", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.2.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Scannable offline by Park Rangers and Police", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Canvas
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PureWhite)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val gridSize = 10
                            val cellSize = size.width / gridSize
                            for (row in 0 until gridSize) {
                                for (col in 0 until gridSize) {
                                    val isCornerFinder = (row < 3 && col < 3) || (row < 3 && col >= gridSize - 3) || (row >= gridSize - 3 && col < 3)
                                    val hashBit = (prof.touristId.hashCode() xor (row * 31 + col * 17)) % 2 == 0
                                    if (isCornerFinder || hashBit) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(col * cellSize, row * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize * 0.9f, cellSize * 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(prof.fullName, fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
                    Text("ID: ${prof.touristId}", fontSize = 12.sp, color = SignalEmerald, fontFamily = FontFamily.Monospace)
                    Text("Blood Group: ${prof.bloodGroup}", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showQrVerificationModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "TOURIST SAFETY HUB",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite
                            )
                            Text(
                                "AI-Powered Corridor Protection",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
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
                        Icon(Icons.Rounded.Lock, contentDescription = "Authority View", tint = Color(0xFF38BDF8))
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
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF101C2E), Color(0xFF0D1420))
                            )
                        )
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CORRIDOR SAFETY MONITOR", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentZone?.name ?: "Mall Road Tourist Corridor", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text("GPS: ${String.format(Locale.US, "%.4f", currentLat)}, ${String.format(Locale.US, "%.4f", currentLon)}", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                safetyManager.triggerInstantSos("MANUAL_SOS_BUTTON", "User triggered manual SOS from Tourist Safety Hub")
                                Toast.makeText(context, "High-Priority SOS Dispatched over Mesh!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = PureWhite),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Text("SOS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Segmented Navigation Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .padding(4.dp)
                ) {
                    listOf("DIGITAL ID", "360 RADAR", "AI POSE", "BLOCKCHAIN").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == index) SurfaceElevated else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
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
            }

            // =========================================================================
            // TAB 0: DIGITAL ID & PRIVACY
            // =========================================================================
            if (selectedTab == 0) {
                if (currentProfile != null) {
                    val prof = currentProfile!!
                    item {
                        // Digital Pass Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E2838), Color(0xFF101724))
                                    )
                                )
                                .border(1.dp, SignalEmerald.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("DIGITAL TOURIST PASS", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SignalEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(prof.touristId, color = SignalEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(prof.fullName, color = PureWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("Nationality: ${prof.nationality} • Blood: ${prof.bloodGroup}", fontSize = 12.sp, color = TextSecondary)
                                Text("Emergency Contact: ${prof.emergencyContactName} (${prof.emergencyContactPhone})", fontSize = 11.sp, color = TextMuted)

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showQrVerificationModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.AccountBox, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SHOW VERIFIABLE QR PASS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Selective Privacy Consent Matrix
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("SELECTIVE PRIVACY CONSENT MATRIX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                                Text("You control which telemetry is shared with response agencies over the offline mesh.", fontSize = 11.sp, color = TextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Share Real-Time Coordinates", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Transmits GPS coordinates to nearby ranger beacons", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = consentSettings.shareLocationWithAuthorities,
                                        onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(shareLocationWithAuthorities = it)) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SignalEmerald, checkedTrackColor = SignalEmerald.copy(alpha = 0.3f))
                                    )
                                }

                                Divider(color = SurfaceBorderSubtle)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Share Medical Profile", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Includes allergies and blood group in emergency packets", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Switch(
                                        checked = consentSettings.shareMedicalDataInEmergency,
                                        onCheckedChange = { safetyManager.updateConsent(consentSettings.copy(shareMedicalDataInEmergency = it)) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = SignalEmerald, checkedTrackColor = SignalEmerald.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 1: 360 RADAR & SMART TRIP
            // =========================================================================
            if (selectedTab == 1) {
                item {
                    // 360 Radar Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(22.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("360 LIVE GEO-FENCE RADAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B1017))
                                    .border(1.dp, SignalEmerald.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val maxRadius = size.width / 2

                                    // Concentric rings
                                    listOf(0.33f, 0.66f, 1.0f).forEach { fraction ->
                                        drawCircle(
                                            color = Color(0xFF1F2937),
                                            radius = maxRadius * fraction,
                                            center = center,
                                            style = Stroke(width = 1.5f)
                                        )
                                    }

                                    // Crosshairs
                                    drawLine(Color(0xFF1F2937), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1.5f)
                                    drawLine(Color(0xFF1F2937), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1.5f)

                                    // User location blip (Center)
                                    drawCircle(SignalEmerald, radius = 6f, center = center)
                                    drawCircle(SignalEmerald.copy(alpha = 0.3f), radius = 14f, center = center)

                                    // Hazard indicator blip (North-East)
                                    val hazardPos = Offset(center.x + maxRadius * 0.55f, center.y - maxRadius * 0.5f)
                                    drawCircle(Color(0xFFEF4444), radius = 5f, center = hazardPos)
                                }

                                Text("100m", color = TextMuted, fontSize = 9.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Simulation Zone Trigger Buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { safetyManager.updateLocation(32.2396, 77.1887) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald.copy(alpha = 0.2f), contentColor = SignalEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text("Safe Base", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { safetyManager.updateLocation(32.4800, 77.1200) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24).copy(alpha = 0.2f), contentColor = Color(0xFFFBBF24)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text("Caution Trail", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { safetyManager.updateLocation(32.3700, 77.2400) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f), contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text("Danger Hazard", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: AI RISK & 33-POINT POSE
            // =========================================================================
            if (selectedTab == 2) {
                item {
                    // AI Multi-Factor Risk Metric Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AI RISK ENGINE (0-100)", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (aiRisk.score > 50) Color(0xFFEF4444).copy(alpha = 0.2f) else SignalEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(aiRisk.level.name, color = if (aiRisk.score > 50) Color(0xFFEF4444) else SignalEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("${aiRisk.score}%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (aiRisk.score > 50) Color(0xFFEF4444) else SignalEmerald)
                            Text(aiRisk.primaryRiskReason, fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                item {
                    // 33-Point MediaPipe Pose Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("33-POINT SKELETAL POSE TRACKER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF090D14)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    // Draw landmark joints
                                    landmarks.forEach { lm ->
                                        val x = lm.x * size.width
                                        val y = lm.y * size.height
                                        drawCircle(
                                            color = if (currentPose == PoseState.FALL_DETECTED) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                            radius = 4f,
                                            center = Offset(x, y)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pose Simulation Selectors
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PoseState.STANDING, PoseState.WALKING, PoseState.RUNNING, PoseState.FALL_DETECTED).forEach { state ->
                                    Button(
                                        onClick = { safetyManager.setPoseState(state) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentPose == state) SignalEmerald else SurfaceElevated,
                                            contentColor = if (currentPose == state) ObsidianBlack else PureWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Text(
                                            text = state.name.take(4),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 3: BLOCKCHAIN TRUST LEDGER
            // =========================================================================
            if (selectedTab == 3) {
                item {
                    Text("TAMPER-PROOF AUDIT BLOCKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(blockchainBlocks) { block ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("BLOCK #${block.index}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text(block.transactionType, color = SignalEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Hash: ${block.hash.take(14)}...${block.hash.takeLast(8)}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
