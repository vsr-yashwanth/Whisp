package com.example.offlinechat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.offlinechat.data.ChatDatabase
import com.example.offlinechat.network.HybridMeshTransport
import com.example.offlinechat.network.PeerTransport
import com.example.offlinechat.network.WebServerService
import com.example.offlinechat.security.CryptoManager
import com.example.offlinechat.ui.AdminScreen
import com.example.offlinechat.ui.ChatScreen
import com.example.offlinechat.ui.HomeScreen
import com.example.offlinechat.ui.SettingsScreen
import com.example.offlinechat.ui.theme.OfflineChatTheme

class MainActivity : ComponentActivity() {

    private val app get() = applicationContext as OfflineChatApp
    private val database: ChatDatabase get() = app.database
    private val cryptoManager: CryptoManager get() = app.cryptoManager
    private val transport: PeerTransport get() = app.transport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start background foreground service for web server
        try {
            val serviceIntent = Intent(this, WebServerService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            // Ignore if service startup is restricted
        }

        setContent {
            OfflineChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val discoveredPeers by transport.discoveredPeers.collectAsState()
                    val connectionState by transport.connectionState.collectAsState()
                    val pairingRequest by transport.pairingRequest.collectAsState()
                    val isGlobalActive by (transport as? HybridMeshTransport)?.isGlobalGatewayActive?.collectAsState() ?: remember { mutableStateOf(false) }

                    // Request Runtime Permissions for Physical BLE & Wi-Fi Direct radios
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val myName = "User-${android.os.Build.MODEL.take(6)}"
                        transport.startAdvertising(myName)
                        transport.startDiscovery(myName)
                    }

                    LaunchedEffect(Unit) {
                        val permissionsToRequest = mutableListOf<String>()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                discoveredPeers = discoveredPeers,
                                connectionState = connectionState,
                                isGlobalActive = isGlobalActive,
                                pairingRequest = pairingRequest,
                                onConnectToPeer = { peer ->
                                    transport.connectToPeer(peer)
                                },
                                onNavigateToChat = { peerId ->
                                    navController.navigate("chat/$peerId")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin")
                                }
                            )
                        }
                        composable("chat/{peerId}") { backStackEntry ->
                            val peerId = backStackEntry.arguments?.getString("peerId") ?: "General Chat"
                            
                            val chatViewModel: ChatViewModel = viewModel(
                                key = "chat_$peerId",
                                factory = remember(peerId) {
                                    ChatViewModel.Factory(
                                        transport = transport,
                                        cryptoManager = cryptoManager,
                                        chatDao = database.chatDao(),
                                        conversationId = peerId
                                    )
                                }
                            )

                            ChatScreen(
                                peerName = peerId,
                                viewModel = chatViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("admin") {
                            AdminScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
