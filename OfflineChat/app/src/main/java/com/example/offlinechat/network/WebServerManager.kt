package com.example.offlinechat.network

import android.content.Context
import android.util.Base64
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.Message
import com.example.offlinechat.security.CryptoManager
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Serializable
data class DeviceInfo(val id: String, val name: String, val status: String, val transportType: String = "LOCAL_BRIDGE")

@Serializable
data class StatsResponse(val messageCount: Int, val isSecure: Boolean, val activePeers: Int, val isGlobalGatewayActive: Boolean = false)

@Serializable
data class MeshPacketRequest(val payload: String)

@Serializable
data class MongoMessageDocument(
    val _id: String,
    val conversationId: String,
    val senderId: String,
    val decryptedText: String,
    val encryptedPayload: String,
    val timestamp: Long,
    val status: String,
    val hopTrace: String = "[]"
)

@Serializable
data class SendMessageWebRequest(
    val conversationId: String = "General Chat",
    val text: String
)

@Serializable
data class GenericWebResponse(
    val success: Boolean,
    val message: String
)

class WebServerManager(
    private val context: Context,
    private val chatDao: ChatDao,
    private val transport: PeerTransport,
    private val cryptoManager: CryptoManager
) {
    private var server: io.ktor.server.engine.ApplicationEngine? = null

    fun start() {
        if (server != null) return
        
        CoroutineScope(Dispatchers.IO).launch {
            server = embeddedServer(CIO, host = "127.0.0.1", port = 8080) {
                install(ContentNegotiation) {
                    json()
                }
                install(CORS) {
                    anyHost()
                }

                routing {
                    // API Endpoints
                    get("/api/devices") {
                        val isConnected = transport.connectionState.value == ConnectionState.CONNECTED
                        val peers = transport.discoveredPeers.value.map { peer ->
                            val status = if (isConnected) "Connected" else "Discovered"
                            val type = when {
                                peer.endpointId.startsWith("Global") -> "GLOBAL_RELAY"
                                peer.name.contains("Bridge") -> "LOCAL_BRIDGE"
                                else -> "BLE_MESH"
                            }
                            DeviceInfo(peer.endpointId, peer.name.ifBlank { "Peer-${peer.endpointId}" }, status, type)
                        }
                        call.respond(peers)
                    }
                    
                    get("/api/stats") {
                        val count = chatDao.getTotalMessageCount().firstOrNull() ?: 0
                        val peersCount = transport.discoveredPeers.value.size
                        val isGlobal = if (transport is HybridMeshTransport) transport.isGlobalGatewayActive.value else false
                        call.respond(StatsResponse(count, true, peersCount, isGlobal))
                    }

                    // Mesh Peer Discovery Ping
                    get("/api/mesh/ping") {
                        val myId = "Node-${android.os.Build.MODEL.replace(" ", "")}"
                        val myName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                        call.respond(mapOf("id" to myId, "name" to myName, "status" to "ACTIVE"))
                    }

                    // Mesh Packet Relay for LAN Bridge
                    post("/api/mesh/packet") {
                        try {
                            val req = call.receive<MeshPacketRequest>()
                            val data = Base64.decode(req.payload, Base64.NO_WRAP)
                            com.example.offlinechat.OfflineChatApp.instance.processIncomingRawPacket(data, "LOCAL_BRIDGE")
                            call.respond(GenericWebResponse(true, "Packet received"))
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.BadRequest, GenericWebResponse(false, e.localizedMessage ?: "Invalid packet"))
                        }
                    }

                    // Serve Static Assets from Android 'assets/web/' directory
                    get("/{...}") {
                        val path = call.request.path().removePrefix("/")
                        val target = if (path.isEmpty() || path == "/") "web/index.html" else "web/$path"
                        
                        try {
                            val stream = this@WebServerManager.context.assets.open(target)
                            val bytes = stream.readBytes()
                            stream.close()
                            
                            val contentType = when {
                                target.endsWith(".html") -> ContentType.Text.Html
                                target.endsWith(".css") -> ContentType.Text.CSS
                                target.endsWith(".js") -> ContentType.Text.JavaScript
                                target.endsWith(".png") -> ContentType.Image.PNG
                                else -> ContentType.Application.OctetStream
                            }
                            call.respondBytes(bytes, contentType)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.NotFound, "File Not Found: $target")
                        }
                    }
                }
            }
            server?.start(wait = false)
        }
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
}
