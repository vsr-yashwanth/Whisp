package com.example.offlinechat.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    LaunchedEffect(Unit) {
        chatDao.getAllMessages().collect { msgs ->
            recentMessages = msgs.take(15)
            totalMessageCount = msgs.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NETWORK GRID", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text(
                            text = if (isGlobalActive) "GLOBAL GATEWAY ACTIVE" else "LOCAL P2P MESH",
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
                        icon = Icons.Rounded.Lock,
                        title = "Encryption",
                        value = "AES-GCM",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        icon = Icons.Rounded.List,
                        title = "Messages",
                        value = "$totalMessageCount",
                        modifier = Modifier.weight(1f)
                    )
                }

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
                    NodeItemCard(peer = peer)
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
fun NetworkRadarCard(
    peers: List<Peer>,
    isGlobal: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f

            // Concentric rings
            val rings = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            rings.forEach { ratio ->
                drawCircle(
                    color = SurfaceBorder,
                    radius = maxRadius * ratio,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Crosshairs
            drawLine(
                color = SurfaceBorderSubtle,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = SurfaceBorderSubtle,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Expanding Pulse Ring
            drawCircle(
                color = PureWhite.copy(alpha = (1f - pulseRadius) * 0.15f),
                radius = maxRadius * pulseRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Sweeping Beam
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val beamEnd = Offset(
                center.x + (maxRadius * cos(sweepRad)).toFloat(),
                center.y + (maxRadius * sin(sweepRad)).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    listOf(PureWhite.copy(alpha = 0.4f), Color.Transparent),
                    start = center,
                    end = beamEnd
                ),
                start = center,
                end = beamEnd,
                strokeWidth = 2.dp.toPx()
            )

            // Center Node
            drawCircle(color = PureWhite, radius = 4.dp.toPx(), center = center)

            // Peer Blips
            peers.forEachIndexed { index, peer ->
                val angle = (index * (360f / maxOf(peers.size, 1)) + 45f)
                val rad = Math.toRadians(angle.toDouble())
                val distRatio = 0.45f + (index % 3) * 0.2f
                val blipPos = Offset(
                    center.x + (maxRadius * distRatio * cos(rad)).toFloat(),
                    center.y + (maxRadius * distRatio * sin(rad)).toFloat()
                )

                val blipColor = if (peer.endpointId.startsWith("Global")) SignalEmerald else PureWhite
                drawCircle(color = blipColor.copy(alpha = 0.2f), radius = 8.dp.toPx(), center = blipPos)
                drawCircle(color = blipColor, radius = 3.5.dp.toPx(), center = blipPos)
            }
        }

        // Radar HUD Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MESH TOPOLOGY • ${peers.size} PEERS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isGlobal) SignalEmerald else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGlobal) "GLOBAL" else "LOCAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    letterSpacing = 0.5.sp
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
            Icon(icon, contentDescription = null, tint = TitaniumLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PureWhite)
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun NodeItemCard(peer: Peer) {
    val isGlobal = peer.endpointId.startsWith("Global")
    val badgeText = if (isGlobal) "GLOBAL" else if (peer.name.contains("Bridge")) "BRIDGE" else "BLE"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isGlobal) Icons.Rounded.Place else Icons.Rounded.Share,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(peer.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PureWhite)
                Text("ID: ${peer.endpointId.take(18)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(badgeText, color = TitaniumLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PacketTraceCard(msg: Message, cryptoManager: com.example.offlinechat.security.CryptoManager) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(msg.timestamp) { timeFormatter.format(Date(msg.timestamp)) }
    val decrypted = remember(msg.encryptedPayload) { cryptoManager.decryptFromStorage(msg.encryptedPayload) }

    val hops = remember(msg.hopTrace) {
        try {
            val arr = JSONArray(msg.hopTrace)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val nodeName = obj.optString("nodeName", obj.optString("nodeId"))
                val transport = obj.optString("transport", "P2P")
                val latency = obj.optLong("latencyMs", 0L)
                list.add("$nodeName ($transport +${latency}ms)")
            }
            list
        } catch (e: Exception) {
            listOf("Direct Peer Delivery")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\"$decrypted\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite,
                    modifier = Modifier.weight(1f)
                )
                Text(text = formattedTime, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                hops.forEachIndexed { index, hop ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (index == 0) TitaniumLight else SignalEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hop ${index + 1}: $hop",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
