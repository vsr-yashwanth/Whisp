package com.example.offlinechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.data.UserManager
import com.example.offlinechat.network.ConnectionState
import com.example.offlinechat.network.PairingRequest
import com.example.offlinechat.network.Peer
import com.example.offlinechat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    discoveredPeers: List<Peer> = emptyList(),
    connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    isGlobalActive: Boolean = false,
    pairingRequest: PairingRequest? = null,
    onConnectToPeer: (Peer) -> Unit = {},
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToCrdtNotes: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userManager = remember { UserManager.getInstance(context) }
    val authPrefs = remember { context.getSharedPreferences("whisp_auth_prefs", android.content.Context.MODE_PRIVATE) }
    val loggedInUser = authPrefs.getString("logged_in_user", "yashwanth") ?: "yashwanth"
    val loggedInRole = authPrefs.getString("logged_in_role", "USER") ?: "USER"

    var showAdminGateDialog by remember { mutableStateOf(false) }

    if (pairingRequest != null) {
        PairingDialog(
            request = pairingRequest,
            onAccept = { pairingRequest.accept() },
            onReject = { pairingRequest.reject() }
        )
    }

    if (showAdminGateDialog) {
        AdminAuthGateDialog(
            onDismiss = { showAdminGateDialog = false },
            onAuthorize = {
                showAdminGateDialog = false
                onNavigateToAdmin()
            },
            userManager = userManager
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "WHISP",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (connectionState == ConnectionState.CONNECTED || isGlobalActive) SignalEmerald else TitaniumDim)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCrdtNotes) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Shared Notes", tint = TitaniumLight, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = {
                        // Admin is not readily accessible to normal users: Requires Admin auth
                        if (loggedInRole == "SUPER_ADMIN" || loggedInRole == "NETWORK_ADMIN" || loggedInUser == "admin") {
                            onNavigateToAdmin()
                        } else {
                            showAdminGateDialog = true
                        }
                    }) {
                        Icon(Icons.Rounded.Lock, contentDescription = "Network Grid Admin", tint = TitaniumLight, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TitaniumLight, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Rounded.ExitToApp, contentDescription = "Log Out / Switch User", tint = TitaniumLight, modifier = Modifier.size(20.dp))
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
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sleek Minimalist Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "User: $loggedInUser",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (loggedInRole == "SUPER_ADMIN" || loggedInRole == "NETWORK_ADMIN") SignalEmerald.copy(alpha = 0.2f) else SurfaceElevated)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = loggedInRole,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (loggedInRole == "SUPER_ADMIN" || loggedInRole == "NETWORK_ADMIN") SignalEmerald else TextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (isGlobalActive) "MongoDB Synced • Global Relay Online" else "MongoDB Synced • Local Offline P2P",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isGlobalActive) SignalEmerald else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isGlobalActive || connectionState == ConnectionState.CONNECTED) SignalEmerald else TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGlobalActive) "GLOBAL" else if (connectionState == ConnectionState.CONNECTED) "ONLINE" else "STANDALONE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGlobalActive || connectionState == ConnectionState.CONNECTED) PureWhite else TextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // General Chat shortcut
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToChat("General Chat") }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("General Mesh Broadcast", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = PureWhite)
                            Text("Stored encrypted in hardware Keystore DB", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "DISCOVERED PEERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (discoveredPeers.isNotEmpty()) {
                        Text(
                            "${discoveredPeers.size} active",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (discoveredPeers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark.copy(alpha = 0.5f))
                            .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = TitaniumLight
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Scanning radio frequencies...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(discoveredPeers) { peer ->
                    val isGlobal = peer.endpointId.startsWith("Global")
                    val tagText = if (isGlobal) "GLOBAL RELAY" else if (peer.name.contains("Bridge")) "BRIDGE" else "BLE"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onConnectToPeer(peer)
                                onNavigateToChat(peer.name.ifBlank { peer.endpointId })
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGlobal) {
                                    Icon(Icons.Rounded.Place, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        text = peer.name.take(1).uppercase().ifBlank { "P" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = peer.name.ifBlank { "Peer ${peer.endpointId}" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "ID: ${peer.endpointId.take(22)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tagText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGlobal) SignalEmerald else TitaniumLight
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AdminAuthGateDialog(
    onDismiss: () -> Unit,
    onAuthorize: () -> Unit,
    userManager: UserManager
) {
    var adminPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun checkAuth() {
        if (userManager.verifyAdminPassword(adminPassword)) {
            onAuthorize()
        } else {
            errorMessage = "Invalid Admin Master Key. Access Denied."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Admin Control Plane", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This node is operating under Zero-Trust policy. Enter the Administrative Master Key to access network topology and controls.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = {
                        adminPassword = it
                        errorMessage = null
                    },
                    label = { Text("Admin Master Key / Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { checkAuth() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalEmerald,
                        unfocusedBorderColor = SurfaceBorderSubtle,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedLabelColor = SignalEmerald
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Text(err, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { checkAuth() },
                colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("AUTHORIZE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

