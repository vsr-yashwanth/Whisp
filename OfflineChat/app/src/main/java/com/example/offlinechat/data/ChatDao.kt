package com.example.offlinechat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun getConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(conversationId: String): List<Message>
    
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessageCount(): Flow<Int>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    // Store-and-Forward Buffered Packet Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBufferedPacket(packet: BufferedPacket)

    @Query("SELECT * FROM buffered_packets WHERE recipientId = :recipientId OR recipientId = 'ALL' ORDER BY priority DESC, createdAt ASC")
    suspend fun getBufferedPacketsForRecipient(recipientId: String): List<BufferedPacket>

    @Query("SELECT * FROM buffered_packets WHERE expiresAt > :now ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    suspend fun getTopBufferedPackets(now: Long, limit: Int = 100): List<BufferedPacket>

    @Query("DELETE FROM buffered_packets WHERE packetId = :packetId")
    suspend fun deleteBufferedPacket(packetId: String)

    @Query("DELETE FROM buffered_packets WHERE expiresAt <= :now")
    suspend fun deleteExpiredBufferedPackets(now: Long): Int

    @Query("UPDATE buffered_packets SET retryCount = retryCount + 1 WHERE packetId = :packetId")
    suspend fun incrementRetryCount(packetId: String)

    @Query("SELECT COUNT(*) FROM buffered_packets")
    fun getBufferedPacketCount(): Flow<Int>

    // DTN Bundle Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDtnBundle(bundle: DtnBundleEntity)

    @Query("SELECT * FROM dtn_bundles WHERE destination = :destination OR destination = 'ALL' ORDER BY priority DESC, creationTime ASC")
    suspend fun getDtnBundlesForDestination(destination: String): List<DtnBundleEntity>

    @Query("SELECT * FROM dtn_bundles WHERE custodyState = :state ORDER BY priority DESC, creationTime ASC")
    suspend fun getDtnBundlesByState(state: String): List<DtnBundleEntity>

    @Query("SELECT * FROM dtn_bundles WHERE expirationTime > :now ORDER BY priority DESC, creationTime ASC")
    suspend fun getActiveDtnBundles(now: Long): List<DtnBundleEntity>

    @Query("UPDATE dtn_bundles SET custodyState = :state WHERE bundleId = :bundleId")
    suspend fun updateDtnBundleCustodyState(bundleId: String, state: String)

    @Query("DELETE FROM dtn_bundles WHERE bundleId = :bundleId")
    suspend fun deleteDtnBundle(bundleId: String)

    @Query("DELETE FROM dtn_bundles WHERE expirationTime <= :now")
    suspend fun deleteExpiredDtnBundles(now: Long): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM dtn_bundles")
    fun getTotalDtnStorageBytes(): Flow<Long>

    @Query("SELECT * FROM dtn_bundles ORDER BY priority ASC, creationTime ASC LIMIT :limit")
    suspend fun getEvictionCandidates(limit: Int): List<DtnBundleEntity>

    @Query("SELECT COUNT(*) FROM dtn_bundles")
    fun getDtnBundleCount(): Flow<Int>

    // Peer Encounter History Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePeerEncounter(encounter: PeerEncounterEntity)

    @Query("SELECT * FROM peer_encounters WHERE peerId = :peerId LIMIT 1")
    suspend fun getPeerEncounter(peerId: String): PeerEncounterEntity?

    @Query("SELECT * FROM peer_encounters ORDER BY lastSeen DESC")
    fun getAllPeerEncounters(): Flow<List<PeerEncounterEntity>>

    // CRDT Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrdtOperation(op: CrdtOperationEntity)

    @Query("SELECT * FROM crdt_operations WHERE documentId = :documentId ORDER BY lamportClock ASC, timestamp ASC")
    suspend fun getCrdtOperationsForDocument(documentId: String): List<CrdtOperationEntity>

    @Query("SELECT COALESCE(MAX(lamportClock), 0) FROM crdt_operations WHERE documentId = :documentId")
    suspend fun getMaxLamportClock(documentId: String): Long

    // Network Epoch Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNetworkEpoch(epoch: NetworkEpochEntity)

    @Query("SELECT * FROM network_epochs ORDER BY epochNumber DESC LIMIT 1")
    suspend fun getLatestNetworkEpoch(): NetworkEpochEntity?

    // Friends & Contacts Directory Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendContact)

    @Query("SELECT * FROM friends ORDER BY isFavorite DESC, lastMessageTime DESC, displayName ASC")
    fun getFriends(): Flow<List<FriendContact>>

    @Query("SELECT * FROM friends WHERE username = :username LIMIT 1")
    suspend fun getFriend(username: String): FriendContact?

    @Query("SELECT * FROM friends WHERE blockchainId = :blockchainId LIMIT 1")
    suspend fun getFriendByBlockchainId(blockchainId: String): FriendContact?

    @Query("DELETE FROM friends WHERE username = :username")
    suspend fun deleteFriend(username: String)

    @Query("UPDATE friends SET lastMessageSnippet = :snippet, lastMessageTime = :time WHERE username = :username")
    suspend fun updateFriendLastMessage(username: String, snippet: String, time: Long)

    @Query("SELECT COUNT(*) FROM friends")
    fun getFriendCount(): Flow<Int>

    // -------------------------------------------------------------
    // WHISP — Smart Tourist Safety & Incident Response DAO Methods
    // -------------------------------------------------------------

    // 1. Tourist Profiles
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTouristProfile(profile: TouristProfile)

    @Query("SELECT * FROM safety_tourist_profiles WHERE touristId = :touristId LIMIT 1")
    suspend fun getTouristProfile(touristId: String): TouristProfile?

    @Query("SELECT * FROM safety_tourist_profiles ORDER BY registeredAt DESC")
    fun getAllTouristProfiles(): Flow<List<TouristProfile>>

    // 2. Geo-Fence Zones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeoFenceZone(zone: GeoFenceZone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeoFenceZones(zones: List<GeoFenceZone>)

    @Query("SELECT * FROM safety_geofence_zones ORDER BY riskWeight DESC")
    fun getAllGeoFenceZones(): Flow<List<GeoFenceZone>>

    @Query("SELECT * FROM safety_geofence_zones WHERE zoneId = :zoneId LIMIT 1")
    suspend fun getGeoFenceZone(zoneId: String): GeoFenceZone?

    // 3. Trip Itineraries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripItinerary(trip: TripItinerary)

    @Query("SELECT * FROM safety_trip_itineraries WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    fun getActiveTripItinerary(): Flow<TripItinerary?>

    @Query("SELECT * FROM safety_trip_itineraries ORDER BY createdAt DESC")
    fun getAllTripItineraries(): Flow<List<TripItinerary>>

    @Query("UPDATE safety_trip_itineraries SET waypointsJson = :waypointsJson, currentCheckpointIndex = :currentIndex, status = :status WHERE tripId = :tripId")
    suspend fun updateTripProgress(tripId: String, waypointsJson: String, currentIndex: Int, status: String)

    // 4. Incidents & Emergency SOS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: SafetyIncident)

    @Query("SELECT * FROM safety_incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<SafetyIncident>>

    @Query("SELECT * FROM safety_incidents WHERE status != 'RESOLVED' ORDER BY timestamp DESC")
    fun getActiveIncidents(): Flow<List<SafetyIncident>>

    @Query("UPDATE safety_incidents SET status = :status, assignedAgency = :assignedAgency, responderNotes = :notes WHERE incidentId = :incidentId")
    suspend fun updateIncidentStatus(incidentId: String, status: IncidentStatus, assignedAgency: ResponseAgency, notes: String)

    @Query("SELECT COUNT(*) FROM safety_incidents WHERE status != 'RESOLVED'")
    fun getActiveIncidentCount(): Flow<Int>

    // 5. Targeted CCTV Cameras
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCctvCamera(camera: CctvCamera)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCctvCameras(cameras: List<CctvCamera>)

    @Query("SELECT * FROM safety_cctv_cameras ORDER BY lastEventTimestamp DESC")
    fun getAllCctvCameras(): Flow<List<CctvCamera>>

    // 6. Blockchain Trust Ledger
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockchainBlock(block: BlockchainBlockEntity)

    @Query("SELECT * FROM safety_blockchain_blocks ORDER BY `index` ASC")
    fun getAllBlockchainBlocks(): Flow<List<BlockchainBlockEntity>>

    @Query("SELECT * FROM safety_blockchain_blocks ORDER BY `index` DESC LIMIT 1")
    suspend fun getLatestBlockchainBlock(): BlockchainBlockEntity?

    @Query("SELECT COUNT(*) FROM safety_blockchain_blocks")
    fun getBlockchainBlockCount(): Flow<Int>
}
