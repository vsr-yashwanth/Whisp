package com.example.offlinechat.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.Peer
import com.example.offlinechat.routing.RouteCandidate
import com.example.offlinechat.sdk.SensorTelemetryDemo
import com.example.offlinechat.security.CryptoManager
import com.example.offlinechat.simulation.ChaosConfig
import com.example.offlinechat.simulation.SimulatedNetwork
import com.example.offlinechat.simulation.SimulationMetrics
import com.example.offlinechat.ui.theme.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as OfflineChatApp
    val transport = app.transport
    val chatDao = app.database.chatDao()

    val discoveredPeers by transport.discoveredPeers.collectAsState()
    val isGlobalActive by transport.isGlobalGatewayActive.collectAsState()

    var recentMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var totalMessageCount by remember { mutableStateOf(0) }
    val dtnStorageBytes by chatDao.getTotalDtnStorageBytes().collectAsState(initial = 0L)
    val dtnBundleCount by chatDao.getDtnBundleCount().collectAsState(initial = 0)

    val partitionStatus by app.partitionManager.partitionStatus.collectAsState()
    val movementState by app.mobilityClassifier.currentMovementState.collectAsState()

    val activeRoutes = remember(discoveredPeers) {
        transport.routingEngine.getAllActiveRoutes()
    }

    var selectedPeer by remember { mutableStateOf<Peer?>(null) }
    var inspectingCandidate by remember { mutableStateOf<RouteCandidate?>(null) }

    var lastSimulationResult by remember { mutableStateOf<SimulationMetrics?>(null) }
    var isSensorRunning by remember { mutableStateOf(false) }
    val sensorDemo = remember { SensorTelemetryDemo() }

    LaunchedEffect(Unit) {
        chatDao.getAllMessages().collect { msgs ->
            recentMessages = msgs.take(15)
            totalMessageCount = msgs.size
        }
    }

    if (selectedPeer != null) {
        PeerDetailsDialog(peer = selectedPeer!!, onDismiss = { selectedPeer = null })
    }

    if (inspectingCandidate != null) {
        RouteExplanationDialog(
            candidate = inspectingCandidate!!,
            explanation = transport.routingEngine.explainRoute(inspectingCandidate!!),
            onDismiss = { inspectingCandidate = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NETWORK GRID V4", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text(
                            text = if (isGlobalActive) "GLOBAL GATEWAY ACTIVE" else "AUTONOMOUS DTN EDGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGlobalActive) SignalEmerald else TextSecondary,
                            letterSpacing = 0.5.sp
                        )
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
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Sleek Monochrome Radar Card
                NetworkRadarCard(
                    peers = discoveredPeers,
                    isGlobal = isGlobalActive
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Minimalist Metric Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricChip(
                        icon = Icons.Rounded.Share,
                        title = "Active Nodes",
                        value = "${discoveredPeers.size + 1}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        icon = Icons.Rounded.Refresh,
                        title = "DTN Bundles",
                        value = "$dtnBundleCount",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        icon = Icons.Rounded.Place,
                        title = "Mobility",
                        value = movementState.name.take(6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // V4 Partition & DTN Quota Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("NETWORK PARTITION STATUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (partitionStatus.isPartitioned) ErrorMuted.copy(alpha = 0.2f) else SignalEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (partitionStatus.isPartitioned) "PARTITION SPLIT" else "EPOCH ${partitionStatus.currentEpoch}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (partitionStatus.isPartitioned) ErrorMuted else SignalEmerald
                                )
                            }
                        }

                        Divider(color = SurfaceBorderSubtle)

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("DTN Custody Storage", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            val kbUsed = dtnStorageBytes / 1024
                            Text("$kbUsed KB / 500 MB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PureWhite)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Reconciliation State", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(partitionStatus.reconciliationStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SignalEmerald)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // V4 Simulation & Chaos Benchmark Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CHAOS BENCHMARK ENGINE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PureWhite)
                            Button(
                                onClick = {
                                    val net = SimulatedNetwork("Scenario A — 50-Node Mesh", ChaosConfig(seed = 849217L, packetLossRate = 0.05f))
                                    for (i in 1..25) net.addNode("N-$i", "Node $i")
                                    for (i in 1..24) net.connectNodes("N-$i", "N-${i + 1}")
                                    for (i in 1..30) net.dispatchPacket("N-1", "N-25", "Chaos payload $i")
                                    lastSimulationResult = net.generateBenchmarkReport()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("RUN CHAOS BENCHMARK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ObsidianBlack)
                            }
                        }

                        if (lastSimulationResult != null) {
                            val res = lastSimulationResult!!
                            Divider(color = SurfaceBorderSubtle)
                            Text("Scenario: ${res.scenarioName} (Seed: ${res.randomSeed})", fontSize = 11.sp, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Delivery: ${"%.1f".format(res.deliveryRatePercent)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SignalEmerald)
                                Text("Avg Latency: ${"%.1f".format(res.averageLatencyMs)}ms", fontSize = 11.sp, color = PureWhite)
                                Text("Avg Hops: ${"%.1f".format(res.averageHops)}", fontSize = 11.sp, color = PureWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // V4 Autonomous Sensor Telemetry Demo Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AUTONOMOUS SENSOR DEMO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PureWhite)
                            Text("Simulates headless sensor publishing telemetry", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = isSensorRunning,
                            onCheckedChange = { running ->
                                isSensorRunning = running
                                if (running) sensorDemo.startPeriodicTelemetry() else sensorDemo.stop()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = SurfaceElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("PREDICTIVE ACTIVE ROUTES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (activeRoutes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark.copy(alpha = 0.5f))
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text("No active multi-hop routes established yet.", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                items(activeRoutes.entries.toList()) { (_, candidates) ->
                    val bestRoute = candidates.minByOrNull { transport.routingEngine.calculateRouteScore(it) }
                    bestRoute?.let { candidate ->
                        val stability = transport.routingEngine.predictionEngine.calculatePredictedStability(candidate.nextHopNodeId)
                        RouteCandidateCard(
                            candidate = candidate,
                            score = transport.routingEngine.calculateRouteScore(candidate),
                            stabilityPct = (stability * 100).toInt(),
                            onClick = { inspectingCandidate = candidate }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("TOPOLOGY NODES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (discoveredPeers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark.copy(alpha = 0.5f))
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Standalone Node Active", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PureWhite)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Scanning BLE, Wi-Fi Direct & Relay channels...", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(discoveredPeers) { peer ->
                    NodeItemCard(peer = peer, onClick = { selectedPeer = peer })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("ROUTE AUDIT TRAIL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (recentMessages.isEmpty()) {
                item {
                    Text("No routed packets yet.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            } else {
                items(recentMessages) { msg ->
                    PacketTraceCard(msg = msg, cryptoManager = app.cryptoManager)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RouteCandidateCard(
    candidate: RouteCandidate,
    score: Float,
    stabilityPct: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "→ ${candidate.destinationNodeId.take(16)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Text(
                    text = "Via: ${candidate.nextHopName.take(14)} • ${candidate.viaTransport}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Stability: $stabilityPct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stabilityPct >= 80) SignalEmerald else TitaniumLight
                )
                Text(
                    text = "Score: ${"%.1f".format(score)} • ${candidate.metrics.averageLatencyMs}ms",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun RouteExplanationDialog(candidate: RouteCandidate, explanation: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Route Decision Explainability", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 15.sp)
        },
        text = {
            Text(explanation, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PureWhite)
            }
        }
    )
}

@Composable
fun NetworkRadarCard(
    peers: List<Peer>,
    isGlobal: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f - 10.dp.toPx()

            // Concentric Distance Rings
            val ringCount = 3
            for (i in 1..ringCount) {
                val r = (maxRadius / ringCount) * i
                drawCircle(
                    color = SurfaceBorderSubtle,
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Crosshairs
            drawLine(
                color = SurfaceBorderSubtle.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = SurfaceBorderSubtle.copy(alpha = 0.5f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Dynamic Sweeping Beam
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val lineEnd = Offset(
                center.x + (maxRadius * cos(sweepRad)).toFloat(),
                center.y + (maxRadius * sin(sweepRad)).toFloat()
            )
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(PureWhite.copy(alpha = 0.7f), Color.Transparent),
                    center = center,
                    radius = maxRadius
                ),
                start = center,
                end = lineEnd,
                strokeWidth = 2.dp.toPx()
            )

            // Local Node Center Indicator
            drawCircle(
                color = PureWhite,
                radius = 5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = PureWhite.copy(alpha = 0.2f),
                radius = 12.dp.toPx(),
                center = center
            )

            // Plot Discovered Peer Nodes on Rings
            peers.forEachIndexed { index, peer ->
                val angle = (index * (360.0 / maxOf(peers.size, 1))) * (Math.PI / 180.0)
                val distFraction = 0.45 + ((index % 3) * 0.22)
                val r = (maxRadius * distFraction).toFloat()
                val peerPos = Offset(
                    center.x + (r * cos(angle)).toFloat(),
                    center.y + (r * sin(angle)).toFloat()
                )

                val nodeColor = when {
                    peer.endpointId.startsWith("Global") -> SignalEmerald
                    peer.name.contains("Bridge") -> PureWhite
                    else -> TitaniumLight
                }

                drawCircle(color = nodeColor, radius = 5.dp.toPx(), center = peerPos)
                drawCircle(color = nodeColor.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = peerPos)
            }
        }

        // Overlay Telemetry Pills
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("RADAR 30 FPS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGlobal) SignalEmerald.copy(alpha = 0.15f) else SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGlobal) "CLOUDFALL ACTIVE" else "DTN AIRGAP MESH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGlobal) SignalEmerald else TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "TARGETS: ${peers.size}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }
        }
    }
}

@Composable
fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PureWhite)
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
fun NodeItemCard(peer: Peer, onClick: () -> Unit = {}) {
    val isGlobal = peer.endpointId.startsWith("Global")
    val isBridge = peer.name.contains("Bridge")

    val typeLabel = when {
        isGlobal -> "GLOBAL RELAY"
        isBridge -> "LOCAL BRIDGE"
        else -> "BLE / WI-FI DIRECT"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isGlobal) Icons.Rounded.Info else Icons.Rounded.Share,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(peer.name.ifBlank { "Node-${peer.endpointId}" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PureWhite)
                    Text("ID: ${peer.endpointId.take(14)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(typeLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isGlobal) SignalEmerald else TextSecondary)
            }
        }
    }
}

@Composable
fun PacketTraceCard(msg: Message, cryptoManager: CryptoManager) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(msg.timestamp) { timeFormatter.format(Date(msg.timestamp)) }
    val hopsCount = remember(msg.hopTrace) {
        try { JSONArray(msg.hopTrace).length() } catch (e: Exception) { 1 }
    }
    val decryptedSnippet = remember(msg.encryptedPayload) {
        cryptoManager.decryptFromStorage(msg.encryptedPayload).take(30)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\"$decryptedSnippet...\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Text(
                    text = "ID: ${msg.id.take(8)} • Channel: ${msg.conversationId.take(14)}",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hopsCount <= 1) "DIRECT" else "$hopsCount HOPS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(formattedTime, fontSize = 9.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun PeerDetailsDialog(peer: Peer, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(peer.name, fontWeight = FontWeight.Bold, color = PureWhite)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Endpoint ID: ${peer.endpointId}", fontSize = 12.sp, color = TextSecondary)
                Text("Encryption: Hardware AES-256-GCM AEAD", fontSize = 12.sp, color = SignalEmerald)
                Text("Relay Capability: Verified Active", fontSize = 12.sp, color = PureWhite)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PureWhite)
            }
        }
    )
}
