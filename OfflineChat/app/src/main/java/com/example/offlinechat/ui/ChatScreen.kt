package com.example.offlinechat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.ChatMessage
import com.example.offlinechat.ChatViewModel
import com.example.offlinechat.data.UserAccount
import com.example.offlinechat.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    peerName: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    var inspectingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val isSosChannel = peerName == "EMERGENCY_SOS"
    val isDirectChat = peerName.startsWith("direct_")
    val directUsername = if (isDirectChat) peerName.removePrefix("direct_") else peerName
    val directBlockchainId = remember(directUsername) { UserAccount.computeBlockchainId(directUsername) }

    val displayTitle = when {
        isSosChannel -> "EMERGENCY AUTHORITIES SOS"
        isDirectChat -> directUsername.replaceFirstChar { it.uppercase() }
        else -> peerName
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (inspectingMessage != null) {
        RouteInspectorDialog(
            message = inspectingMessage!!,
            onDismiss = { inspectingMessage = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayTitle,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSosChannel) Color(0xFFFCA5A5) else PureWhite
                            )
                            if (isSosChannel) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("PRIORITY 100", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isSosChannel) Icons.Rounded.Warning else Icons.Rounded.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = if (isSosChannel) Color(0xFFEF4444) else SignalEmerald
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    isSosChannel -> "Direct Multi-hop Authority Broadcast"
                                    isDirectChat -> "ID: ${directBlockchainId.take(10)}...${directBlockchainId.takeLast(6)}"
                                    else -> "Hardware AES-256-GCM • Mesh Broadcast"
                                },
                                fontSize = 10.sp,
                                color = if (isSosChannel) Color(0xFFFCA5A5) else TextSecondary,
                                fontFamily = if (isDirectChat) FontFamily.Monospace else FontFamily.Default
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
                    containerColor = if (isSosChannel) Color(0xFF1E0B10) else ObsidianBlack,
                    titleContentColor = PureWhite
                )
            )
        },
        containerColor = ObsidianBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat area
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSosChannel) Color(0xFFEF4444).copy(alpha = 0.15f) else SurfaceDark)
                                .border(1.dp, if (isSosChannel) Color(0xFFEF4444).copy(alpha = 0.5f) else SurfaceBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isSosChannel) Icons.Rounded.Warning else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (isSosChannel) Color(0xFFEF4444) else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isSosChannel) "High-Priority Emergency Authority Channel" else if (isDirectChat) "1-on-1 Direct Private Chat" else "End-to-End Encrypted Session",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSosChannel) "Broadcasts are prioritized and relayed across all intermediate nodes." else "Messages are linked to Blockchain IDs for offline delay-tolerant routing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages.reversed(), key = { it.id }) { msg ->
                        ChatBubble(
                            msg = msg,
                            isSos = isSosChannel,
                            onInspectRoute = { inspectingMessage = it }
                        )
                    }
                }
            }

            // Quick Emergency Presets (Only on Emergency Channel)
            if (isSosChannel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E0B10))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "QUICK EMERGENCY BROADCASTS:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCA5A5),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                viewModel.sendMessage("MEDICAL EMERGENCY: Urgent medical assistance required at current coordinates.", isEmergency = true)
                            },
                            label = { Text("Medical", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2A0E14), labelColor = Color(0xFFFCA5A5))
                        )
                        AssistChip(
                            onClick = {
                                viewModel.sendMessage("FIRE / HAZARD: Active fire outbreak detected in local zone.", isEmergency = true)
                            },
                            label = { Text("Fire", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2A0E14), labelColor = Color(0xFFFCA5A5))
                        )
                        AssistChip(
                            onClick = {
                                viewModel.sendMessage("SEARCH & RESCUE: Trapped / stranded individuals need rescue.", isEmergency = true)
                            },
                            label = { Text("Rescue", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2A0E14), labelColor = Color(0xFFFCA5A5))
                        )
                    }
                }
            }

            // Bottom Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBlack)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                if (isSosChannel) "Broadcast emergency alert..." else "Encrypted message...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = if (isSosChannel) Color(0xFFEF4444) else SignalEmerald,
                            unfocusedBorderColor = SurfaceBorderSubtle,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText, isEmergency = isSosChannel)
                                    messageText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText, isEmergency = isSosChannel)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSosChannel) Color(0xFFEF4444) else PureWhite)
                    ) {
                        Icon(
                            Icons.Rounded.Send,
                            contentDescription = "Send",
                            tint = if (isSosChannel) Color.White else ObsidianBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    msg: ChatMessage,
    isSos: Boolean = false,
    onInspectRoute: (ChatMessage) -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(msg.timestamp) { timeFormatter.format(Date(msg.timestamp)) }
    val isMine = msg.isFromMe
    val hopCount = maxOf(msg.hopTrace.size, 1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMine) 18.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isSos && !isMine) Color(0xFF2A0E14)
                        else if (isMine) PureWhite
                        else SurfaceDark
                    )
                    .then(
                        if (!isMine) Modifier.border(1.dp, if (isSos) Color(0xFFEF4444).copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                        else Modifier
                    )
                    .clickable { onInspectRoute(msg) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = msg.text,
                        color = if (isMine) ObsidianBlack else if (isSos) Color(0xFFFCA5A5) else TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isMine) FontWeight.Medium else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(9.dp),
                            tint = if (isMine) ObsidianBlack.copy(alpha = 0.6f) else TextSecondary
                        )
                        Text(
                            text = if (hopCount == 1) "Direct P2P" else "$hopCount Hops",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMine) ObsidianBlack.copy(alpha = 0.65f) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (isMine) {
                    Text(
                        text = "• ${msg.status.lowercase()}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun RouteInspectorDialog(
    message: ChatMessage,
    onDismiss: () -> Unit
) {
    val hops = message.hopTrace

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Share, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Packet Route Audit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PureWhite)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Payload Content: \"${message.text.take(60)}${if (message.text.length > 60) "..." else ""}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (hops.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Text(
                            "Direct 1-Hop Local Packet Transmission.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(hops) { hop ->
                            val hopTime = remember(hop.timestamp) {
                                SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(hop.timestamp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(SignalEmerald.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = hop.nodeName.take(1).uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SignalEmerald
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hop.nodeName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "${hop.transport} • $hopTime • +${hop.latencyMs}ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PureWhite)
            }
        }
    )
}
