package com.example.offlinechat.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.data.FriendContact
import com.example.offlinechat.data.UserAccount
import com.example.offlinechat.data.UserManager
import com.example.offlinechat.network.ConnectionState
import com.example.offlinechat.network.PairingRequest
import com.example.offlinechat.network.Peer
import com.example.offlinechat.ui.theme.*
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userManager = remember { UserManager.getInstance(context) }
    val chatDao = remember { OfflineChatApp.instance.database.chatDao() }
    val authPrefs = remember { context.getSharedPreferences("whisp_auth_prefs", Context.MODE_PRIVATE) }
    val loggedInUser = authPrefs.getString("logged_in_user", "User") ?: "User"
    val loggedInRole = authPrefs.getString("logged_in_role", "USER") ?: "USER"

    val myBlockchainId = remember(loggedInUser) { UserAccount.computeBlockchainId(loggedInUser) }
    val friendsList by chatDao.getFriends().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: DIRECT CHATS, 1: MESH & SOS, 2: RADIO PEERS
    var showAdminGateDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

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

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            userManager = userManager,
            onFriendAdded = { friend ->
                coroutineScope.launch {
                    chatDao.insertFriend(friend)
                    showAddFriendDialog = false
                    Toast.makeText(context, "Added ${friend.displayName} to friends!", Toast.LENGTH_SHORT).show()
                    onNavigateToChat("direct_${friend.username}")
                }
            }
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
                Spacer(modifier = Modifier.height(10.dp))
                
                // 1. Sleek Blockchain Identity & User Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF14141E), Color(0xFF1A1A26))
                            )
                        )
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SignalEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, SignalEmerald.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = loggedInUser.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = SignalEmerald,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = loggedInUser,
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
                                    text = if (isGlobalActive) "Global Relay Active • Mesh Routing Online" else "Local Offline P2P • Delay-Tolerant Store & Forward",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isGlobalActive) SignalEmerald else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = SurfaceBorderSubtle, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Blockchain ID Address Strip with 1-Tap Copy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0D0D12))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Whisp Blockchain ID", myBlockchainId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Blockchain ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DECENTRALIZED BLOCKCHAIN ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = "${myBlockchainId.take(10)}...${myBlockchainId.takeLast(8)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PureWhite,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Rounded.Share, contentDescription = "Copy ID", tint = TitaniumLight, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. High-Priority Emergency Authorities SOS Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2A0E14), Color(0xFF1E0B10))
                            )
                        )
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToChat("EMERGENCY_SOS") }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Warning, contentDescription = "SOS", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("EMERGENCY AUTHORITIES SOS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Direct multi-hop broadcast to local authorities & emergency nodes", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Navigation Channel Switcher: DIRECT CHATS vs GENERAL MESH vs RADIO PEERS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(4.dp)
                ) {
                    listOf("DIRECT CHATS", "GENERAL MESH", "RADIO PEERS").forEachIndexed { index, title ->
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
                                color = if (selectedTab == index) PureWhite else TextMuted,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // TAB 0: 1-on-1 DIRECT FRIENDS CHATS
            if (selectedTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "FRIENDS & DIRECT CHATS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Button(
                            onClick = { showAddFriendDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD FRIEND", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (friendsList.isEmpty()) {
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
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = TitaniumDim, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No direct friends added yet", style = MaterialTheme.typography.bodyMedium, color = PureWhite, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Search a username or Blockchain ID to start a 1-on-1 private chat", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { showAddFriendDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalEmerald),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalEmerald),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Find & Add Friends", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(friendsList) { friend ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .clickable { onNavigateToChat("direct_${friend.username}") }
                                .padding(14.dp)
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
                                    Text(
                                        text = friend.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SignalEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = friend.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PureWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "@${friend.username}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = friend.lastMessageSnippet ?: "ID: ${friend.blockchainId.take(8)}...${friend.blockchainId.takeLast(6)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (friend.lastMessageSnippet != null) TextSecondary else TextMuted,
                                        maxLines = 1
                                    )
                                }
                                Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // TAB 1: GENERAL MESH BROADCAST
            if (selectedTab == 1) {
                item {
                    Text(
                        "GLOBAL CHANNELS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // General Mesh Broadcast
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
                                Text("Decentralized broadcast to all nodes in range", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // TAB 2: RADIO PEERS
            if (selectedTab == 2) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "RADIO FREQUENCY PEERS",
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
                                    "Scanning radio frequencies & nearby BLE nodes...",
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
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    userManager: UserManager,
    onFriendAdded: (FriendContact) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList() else userManager.searchUsers(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Search & Add Friend", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Search by username or paste a 0x... Blockchain ID to start a 1-on-1 personal direct chat.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Username or 0x... Blockchain ID") },
                    singleLine = true,
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

                if (searchResults.isNotEmpty()) {
                    Text("MATCHING USERS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        searchResults.forEach { user ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.username, fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 13.sp)
                                        Text(
                                            "${user.blockchainId.take(10)}...${user.blockchainId.takeLast(6)}",
                                            fontSize = 10.sp,
                                            color = SignalEmerald,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val friend = FriendContact(
                                                username = user.username,
                                                blockchainId = user.blockchainId,
                                                displayName = user.username.replaceFirstChar { it.uppercase() },
                                                role = user.role
                                            )
                                            onFriendAdded(friend)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("ADD", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (searchQuery.isNotBlank()) {
                    // Manual entry option for offline peers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Direct ID Friend Entry", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 12.sp)
                            Text("Add '@${searchQuery.trim()}' as a custom contact", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val clean = searchQuery.trim()
                                    val friend = FriendContact(
                                        username = clean,
                                        blockchainId = if (clean.startsWith("0x")) clean else UserAccount.computeBlockchainId(clean),
                                        displayName = clean.replaceFirstChar { it.uppercase() }
                                    )
                                    onFriendAdded(friend)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("ADD AS CONTACT & CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
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
