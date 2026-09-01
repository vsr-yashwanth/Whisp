package com.example.offlinechat.ui.safety

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.data.*
import com.example.offlinechat.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorityDispatchScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as OfflineChatApp
    val safetyManager = app.safetyManager
    val chatDao = app.database.chatDao()

    val incidents by chatDao.getAllIncidents().collectAsState(initial = emptyList())
    val activeIncidentCount by chatDao.getActiveIncidentCount().collectAsState(initial = 0)
    val tourists by chatDao.getAllTouristProfiles().collectAsState(initial = emptyList())
    val zones by chatDao.getAllGeoFenceZones().collectAsState(initial = emptyList())
    val cameras by chatDao.getAllCctvCameras().collectAsState(initial = emptyList())

    // 0: INCIDENT TRIAGE, 1: CCTV INVESTIGATION, 2: LIVE MONITORING, 3: IMPACT ANALYTICS
    var selectedTab by remember { mutableStateOf(0) }
    var inspectingIncident by remember { mutableStateOf<SafetyIncident?>(null) }
    var cctvPoseFilter by remember { mutableStateOf("ALL") }

    if (inspectingIncident != null) {
        val inc = inspectingIncident!!
        var assignedAgency by remember(inc) { mutableStateOf(inc.assignedAgency) }
        var status by remember(inc) { mutableStateOf(inc.status) }
        var notes by remember(inc) { mutableStateOf(inc.responderNotes) }

        Dialog(onDismissRequest = { inspectingIncident = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ObsidianBlack)
                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INCIDENT #${inc.incidentId}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(inc.severity.name, color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(inc.touristName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Tourist ID: ${inc.touristId} • Zone: ${inc.zoneName}", fontSize = 11.sp, color = TextSecondary)
                    Text("GPS Coordinates: ${inc.latitude}, ${inc.longitude}", fontSize = 11.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trigger: ${inc.triggerSource} | Pose: ${inc.postureState.name} | Risk: ${inc.riskScore}%", fontSize = 11.sp, color = Color(0xFFFBBF24))

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = SurfaceBorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("ASSIGN RESPONSE AGENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ResponseAgency.POLICE_CONTROL, ResponseAgency.MEDICAL_AMBULANCE, ResponseAgency.FOREST_RANGERS).forEach { agency ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (assignedAgency == agency) SignalEmerald.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (assignedAgency == agency) SignalEmerald else SurfaceBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { assignedAgency = agency }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = agency.name.replace("_", " ").take(10),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (assignedAgency == agency) SignalEmerald else PureWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("UPDATE DISPATCH STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(IncidentStatus.ACKNOWLEDGED, IncidentStatus.DISPATCHED, IncidentStatus.RESOLVED).forEach { st ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (status == st) Color(0xFF38BDF8).copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (status == st) Color(0xFF38BDF8) else SurfaceBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { status = st }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (status == st) Color(0xFF38BDF8) else PureWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { inspectingIncident = null },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = PureWhite),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                safetyManager.updateIncidentStatus(inc.incidentId, status, assignedAgency, notes)
                                inspectingIncident = null
                                Toast.makeText(context, "Incident updated & broadcast to responders!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SAVE & DISPATCH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
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
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "WHISP AUTHORITY DISPATCH",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite
                            )
                            Text(
                                "Multi-Agency Emergency Coordination",
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

                // Authority HUD Overview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1C1318), Color(0xFF121420))
                            )
                        )
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTIVE EMERGENCY INCIDENTS", fontSize = 10.sp, color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("$activeIncidentCount REQUIRE DISPATCH", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (activeIncidentCount > 0) Color(0xFFEF4444) else SignalEmerald)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("POLICE • MEDICS • RANGERS", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(4.dp)
                ) {
                    listOf("INCIDENT TRIAGE", "CCTV SEARCH", "LIVE RADAR", "IMPACT 66%").forEachIndexed { index, title ->
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
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) Color(0xFF38BDF8) else TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =========================================================================
            // TAB 0: INCIDENT TRIAGE & DISPATCH
            // =========================================================================
            if (selectedTab == 0) {
                if (incidents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(16.dp))
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All tourist corridors secure", color = PureWhite, fontWeight = FontWeight.Bold)
                                Text("No active high-risk SOS incidents reported.", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    items(incidents) { inc ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF141822))
                                .border(1.dp, if (inc.status != IncidentStatus.RESOLVED) Color(0xFFEF4444).copy(alpha = 0.5f) else SurfaceBorderSubtle, RoundedCornerShape(16.dp))
                                .clickable { inspectingIncident = inc }
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(inc.incidentId, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SurfaceElevated)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(inc.triggerSource, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (inc.status) {
                                                    IncidentStatus.REPORTED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                    IncidentStatus.ACKNOWLEDGED -> Color(0xFFFBBF24).copy(alpha = 0.2f)
                                                    IncidentStatus.DISPATCHED -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                                    IncidentStatus.RESOLVED -> SignalEmerald.copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = inc.status.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (inc.status) {
                                                IncidentStatus.REPORTED -> Color(0xFFEF4444)
                                                IncidentStatus.ACKNOWLEDGED -> Color(0xFFFBBF24)
                                                IncidentStatus.DISPATCHED -> Color(0xFF38BDF8)
                                                IncidentStatus.RESOLVED -> SignalEmerald
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(inc.touristName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Zone: ${inc.zoneName} • Posture: ${inc.postureState.name}", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Assigned: ${inc.assignedAgency.name} • Tap to view & dispatch", fontSize = 10.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 1: TARGETED CCTV INVESTIGATION SEARCH
            // =========================================================================
            if (selectedTab == 1) {
                item {
                    Text("TARGETED CCTV CAMERA SEARCH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Posture & Anomaly Filter Selector
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "WALKING", "RUNNING", "FALL_DETECTED").forEach { filter ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (cctvPoseFilter == filter) SignalEmerald.copy(alpha = 0.2f) else SurfaceDark)
                                    .border(1.dp, if (cctvPoseFilter == filter) SignalEmerald else SurfaceBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { cctvPoseFilter = filter }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.replace("_", " "),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cctvPoseFilter == filter) SignalEmerald else PureWhite
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                items(cameras) { cam ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F141E))
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cam.cameraId, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SignalEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVE STREAM", color = SignalEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(cam.locationName, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Detections: ${cam.activeDetections}", fontSize = 10.sp, color = Color(0xFFFBBF24))
                            if (cam.matchedTouristId.isNotEmpty()) {
                                Text("Matched Tourist ID: ${cam.matchedTouristId}", fontSize = 10.sp, color = SignalEmerald, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 2: LIVE RADAR & ZONE MONITORING
            // =========================================================================
            if (selectedTab == 2) {
                item {
                    Text("ACTIVE GEO-FENCE REGIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(zones) { zone ->
                    val zoneColor = when (zone.zoneType) {
                        GeoZoneType.SAFE -> SignalEmerald
                        GeoZoneType.CAUTION -> Color(0xFFFBBF24)
                        GeoZoneType.RESTRICTED -> Color(0xFFEF4444)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDark)
                            .border(1.dp, zoneColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(zoneColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(zone.name, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${zone.zoneType.name} • Radius: ${zone.radiusMeters.toInt()}m • Risk Weight: ${zone.riskWeight}", fontSize = 10.sp, color = TextSecondary)
                                Text(zone.description, fontSize = 10.sp, color = TextMuted, maxLines = 1)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("REGISTERED TOURISTS IN CORRIDOR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(tourists) { t ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F141E))
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t.fullName.take(1), color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.fullName, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("TID: ${t.touristId} • ${t.nationality} • Blood: ${t.bloodGroup}", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 3: IMPACT & 66% RESPONSE TIME REDUCTION
            // =========================================================================
            if (selectedTab == 3) {
                item {
                    // Response Time Reduction Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF101B2E), Color(0xFF0F121C))
                                )
                            )
                            .border(1.dp, SignalEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SAVING LIVES: RESPONSE TIME", color = SignalEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SignalEmerald)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("66% REDUCTION", color = ObsidianBlack, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("18.2 min", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                                    Text("Before Whisp", fontSize = 10.sp, color = TextMuted)
                                }
                                Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = SignalEmerald, modifier = Modifier.padding(top = 8.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("6.1 min", fontSize = 22.sp, fontWeight = FontWeight.Black, color = SignalEmerald)
                                    Text("With Whisp", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = SurfaceBorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Combines GPS, Geo-fencing, 33-point MediaPipe Keypoint Anomaly Detection, and Decentralized Mesh Relays for instantaneous emergency intervention.",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Annual Incidents Prevented Projection Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("POTENTIAL INCIDENTS PREVENTED ANNUALLY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("2026: 350", "2027: 600", "2028: 900", "2029: 1200", "2030: 1500+").forEach { stat ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(stat.split(":")[1].trim(), color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(stat.split(":")[0], color = TextMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
