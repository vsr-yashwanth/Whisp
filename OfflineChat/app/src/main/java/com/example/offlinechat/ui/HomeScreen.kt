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
import com.example.offlinechat.data.GeoZoneType
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
    

    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userManager = remember { UserManager.getInstance(context) }
    val chatDao = remember { OfflineChatApp.instance.database.chatDao() }
    val authPrefs = remember { context.getSharedPreferences("whisp_auth_prefs", Context.MODE_PRIVATE) }
    val loggedInUser = authPrefs.getString("logged_in_user", "User") ?: "User"
    val loggedInRole = authPrefs.getString("logged_in_role", "USER") ?: "USER"

    val myBlockchainId = remember(loggedInUser) { UserAccount.computeBlockchainId(loggedInUser) }
    val friendsList by chatDao.getFriends().collectAsState(initial = emptyList())
    val activeIncidents by chatDao.getActiveIncidentCount().collectAsState(initial = 0)

    // 0: CHATS, 1: IDENTITY, 2: NETWORK
    var selectedNavTab by remember { mutableStateOf(0) }
    var chatSearchQuery by remember { mutableStateOf("") }
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
                    Toast.makeText(context, "Added ${friend.displayName} to friends", Toast.LENGTH_SHORT).show()
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
                            letterSpacing = 2.sp,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (connectionState == ConnectionState.CONNECTED || isGlobalActive) SignalEmerald.copy(alpha = 0.2f) else TitaniumDim.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (connectionState == ConnectionState.CONNECTED || isGlobalActive) SignalEmerald else TitaniumDim)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectionState == ConnectionState.CONNECTED || isGlobalActive) "MESH ONLINE" else "STANDBY",
                                    color = if (connectionState == ConnectionState.CONNECTED || isGlobalActive) SignalEmerald else TitaniumLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = TitaniumLight, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Rounded.ExitToApp, contentDescription = "Log Out", tint = TitaniumLight, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBlack,
                    titleContentColor = PureWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D111A),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = { Icon(Icons.Rounded.Email, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 11.sp, fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SignalEmerald,
                        selectedTextColor = SignalEmerald,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = SignalEmerald.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(Icons.Rounded.AccountBox, contentDescription = "Identity") },
                    label = { Text("Identity", fontSize = 11.sp, fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFBBF24),
                        selectedTextColor = Color(0xFFFBBF24),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0xFFFBBF24).copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedNavTab == 3,
                    onClick = { selectedNavTab = 3 },
                    icon = { Icon(Icons.Rounded.Share, contentDescription = "Network") },
                    label = { Text("Network", fontSize = 11.sp, fontWeight = if (selectedNavTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = PureWhite,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = SurfaceElevated
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedNavTab == 0) {
                FloatingActionButton(
                    onClick = { showAddFriendDialog = true },
                    containerColor = SignalEmerald,
                    contentColor = ObsidianBlack,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New Contact / Chat")
                }
            }
        },
        containerColor = ObsidianBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedNavTab) {
                0 -> ChatsTabView(
                    chatSearchQuery = chatSearchQuery,
                    onSearchQueryChange = { chatSearchQuery = it },
                    friendsList = friendsList,
                    activeIncidents = activeIncidents,
                    onNavigateToChat = onNavigateToChat,
                    onOpenAddFriend = { showAddFriendDialog = true }
                )
                1 -> IdentityTabView(
                    username = loggedInUser,
                    role = loggedInRole,
                    blockchainId = myBlockchainId,
                    onOpenFullHub = onNavigateToSettings
                )
                2 -> NetworkTabView(
                    discoveredPeers = discoveredPeers,
                    connectionState = connectionState,
                    isGlobalActive = isGlobalActive,
                    loggedInRole = loggedInRole,
                    loggedInUser = loggedInUser,
                    onConnectToPeer = onConnectToPeer,
                    onNavigateToChat = onNavigateToChat,
                    onOpenCrdtNotes = onNavigateToCrdtNotes,
                    onOpenAdmin = {
                        if (loggedInRole == "SUPER_ADMIN" || loggedInRole == "NETWORK_ADMIN" || loggedInUser == "admin") {
                            onNavigateToAdmin()
                        } else {
                            showAdminGateDialog = true
                        }
                    },
                )
            }
        }
    }
}

// =========================================================================
// TAB 0: CHATS & CHANNELS
// =========================================================================
@Composable
private fun ChatsTabView(
    chatSearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    friendsList: List<FriendContact>,
    activeIncidents: Int,
    onNavigateToChat: (String) -> Unit,
    onOpenAddFriend: () -> Unit
) {
    val filteredFriends = remember(chatSearchQuery, friendsList) {
        if (chatSearchQuery.isBlank()) friendsList
        else friendsList.filter {
            it.displayName.contains(chatSearchQuery, ignoreCase = true) ||
            it.username.contains(chatSearchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = chatSearchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search chats and contacts...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (chatSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = SignalEmerald,
                    unfocusedBorderColor = SurfaceBorderSubtle,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Dedicated Emergency Authorities SOS Channel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF261014), Color(0xFF190C10))
                        )
                    )
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToChat("emergency_authorities") }
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("EMERGENCY AUTHORITIES SOS", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PRIORITY 100", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ObsidianBlack)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (activeIncidents > 0) "$activeIncidents ACTIVE INCIDENTS REPORTED" else "Dedicated Police & Medical Emergency Channel",
                            color = if (activeIncidents > 0) Color(0xFFFCA5A5) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (activeIncidents > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }

        // Global Mesh Broadcast Channel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF101B2E), Color(0xFF0F1420))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToChat("General Mesh") }
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("General Mesh Broadcast", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Public decentralized room for all local radio peers", color = TextSecondary, fontSize = 11.sp)
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
            }
        }

        // Direct 1-on-1 Chats Section Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DIRECT 1-ON-1 CHATS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    "${friendsList.size} Contacts",
                    fontSize = 11.sp,
                    color = SignalEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (filteredFriends.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No direct chats yet", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap '+' below to add a contact by username or ID", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(filteredFriends) { friend ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(18.dp))
                        .clickable { onNavigateToChat("direct_${friend.username}") }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.displayName.take(1).uppercase(),
                                color = SignalEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(friend.displayName, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@${friend.username} • ${friend.blockchainId.take(8)}...${friend.blockchainId.takeLast(4)}",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TitaniumLight, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: SAFETY RADAR & SOS
// =========================================================================
@Composable
private fun SafetyTabView(
    aiRiskScore: Int,
    aiRiskLevel: String,
    zoneName: String,
    zoneType: GeoZoneType,
    onOpenFullSafetyHub: () -> Unit,
    onOpenAuthorityDesk: () -> Unit
) {
    val zoneColor = when (zoneType) {
        GeoZoneType.SAFE -> SignalEmerald
        GeoZoneType.CAUTION -> Color(0xFFFBBF24)
        GeoZoneType.RESTRICTED -> Color(0xFFEF4444)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F1E2A), Color(0xFF0B141C))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("WHISP TOURIST SAFETY HUB", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Smart Corridor Protection", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PureWhite)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SignalEmerald)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE", color = ObsidianBlack, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // AI Risk Metric
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceDark)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("AI RISK SCORE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$aiRiskScore%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (aiRiskScore > 50) Color(0xFFEF4444) else SignalEmerald)
                                Text(aiRiskLevel, fontSize = 10.sp, color = TextSecondary)
                            }
                        }

                        // Current Zone Metric
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceDark)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("CURRENT ZONE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(zoneName.take(14), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = zoneColor)
                                Text(zoneType.name, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onOpenFullSafetyHub,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPEN 360 RADAR & AI POSE HUB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Multi-Agency Authority Dispatch Quick Access
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF181520))
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .clickable { onOpenAuthorityDesk() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-Agency Authority Dispatch Desk", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Police, Medical, and Rangers Incident Coordination", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: DIGITAL TOURIST ID & PRIVACY
// =========================================================================
@Composable
private fun IdentityTabView(
    username: String,
    role: String,
    blockchainId: String,
    onOpenFullHub: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Apple Wallet Style Pass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E2838), Color(0xFF101724))
                        )
                    )
                    .border(1.dp, SignalEmerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(22.dp)
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
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("VERIFIED ID", color = SignalEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(username.replaceFirstChar { it.uppercase() }, color = PureWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Text("Role: $role • Registered Tourist", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = SurfaceBorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("BLOCKCHAIN DECENTRALIZED IDENTITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = blockchainId,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Blockchain ID", blockchainId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ID to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = "Copy", tint = TitaniumLight, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOpenFullHub,
                        colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = ObsidianBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AccountBox, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHOW VERIFIABLE QR CARD & CONSENTS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 3: NETWORK & MESH GRID
// =========================================================================
@Composable
private fun NetworkTabView(
    discoveredPeers: List<Peer>,
    connectionState: ConnectionState,
    isGlobalActive: Boolean,
    loggedInRole: String,
    loggedInUser: String,
    onConnectToPeer: (Peer) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onOpenCrdtNotes: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenAuthorityDesk: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Network Health Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text("DECENTRALIZED RADIO TOPOLOGY", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${discoveredPeers.size}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PureWhite)
                            Text("Discovered Peers", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column {
                            Text(if (isGlobalActive) "ACTIVE" else "OFFLINE", fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (isGlobalActive) SignalEmerald else TitaniumDim)
                            Text("Relay Gateway", fontSize = 11.sp, color = TextSecondary)
                        }
                        Column {
                            Text(connectionState.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("Local Radio State", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Shared CRDT Notes Tile
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(18.dp))
                    .clickable { onOpenCrdtNotes() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CRDT Collaborative Field Notes", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Offline shared checklists with zero-conflict merging", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TitaniumLight, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Admin Grid Console Tile
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(18.dp))
                    .clickable { onOpenAdmin() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Network Grid Operator Console", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Topology inspector, PRoPHET routing, and DTN custody", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = TitaniumLight, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =========================================================================
// DIALOGS: ADD FRIEND & ADMIN AUTH GATE
// =========================================================================
@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    userManager: UserManager,
    onFriendAdded: (FriendContact) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else userManager.searchUsers(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = SignalEmerald, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Add New Contact", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "Search registered users by username or paste their 0x... Blockchain ID to initiate 1-on-1 encrypted messaging.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search username or 0x...", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SignalEmerald,
                        unfocusedBorderColor = SurfaceBorderSubtle,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedLabelColor = SignalEmerald
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchResults.isNotEmpty()) {
                    Text("MATCHING USERS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        searchResults.forEach { user ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated)
                                    .padding(12.dp)
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
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("ADD", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Direct ID Friend Entry", fontWeight = FontWeight.Bold, color = PureWhite, fontSize = 12.sp)
                            Text("Add '@${searchQuery.trim()}' as a custom contact", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("ADD AS CONTACT & CHAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(20.dp),
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
