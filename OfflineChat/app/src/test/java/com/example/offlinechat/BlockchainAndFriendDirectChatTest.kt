package com.example.offlinechat

import com.example.offlinechat.data.FriendContact
import com.example.offlinechat.data.UserAccount
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import org.junit.Assert.*
import org.junit.Test

class BlockchainAndFriendDirectChatTest {

    @Test
    fun testBlockchainIdGenerationAndDeterminism() {
        val user1 = "yashwanth"
        val user2 = "alice"

        val id1_first = UserAccount.computeBlockchainId(user1)
        val id1_second = UserAccount.computeBlockchainId(user1)
        val id2 = UserAccount.computeBlockchainId(user2)

        // Must start with standard 0x prefix
        assertTrue("Blockchain ID must start with 0x", id1_first.startsWith("0x"))
        assertEquals("Blockchain ID must be 42 characters (0x + 40 hex chars)", 42, id1_first.length)

        // Must be deterministic
        assertEquals("Identical username must yield identical blockchain ID", id1_first, id1_second)

        // Case insensitivity
        val id1_upper = UserAccount.computeBlockchainId("YASHWANTH")
        assertEquals("Case insensitivity must be preserved", id1_first, id1_upper)

        // Distinct users must have distinct blockchain IDs
        assertNotEquals("Distinct users must have distinct blockchain IDs", id1_first, id2)
    }

    @Test
    fun testFriendContactEntityAndDirectAddressing() {
        val friendUsername = "bob"
        val friendBlockchainId = UserAccount.computeBlockchainId(friendUsername)

        val friend = FriendContact(
            username = friendUsername,
            blockchainId = friendBlockchainId,
            displayName = "Bob Dylan",
            role = "USER",
            lastMessageSnippet = "Hey there over BLE!",
            lastMessageTime = System.currentTimeMillis()
        )

        assertEquals("bob", friend.username)
        assertTrue(friend.blockchainId.startsWith("0x"))
        assertEquals("Bob Dylan", friend.displayName)
        assertNotNull(friend.lastMessageSnippet)
    }

    @Test
    fun testDirectMeshPacketBlockchainAddressing() {
        val sender = "alice"
        val recipient = "bob"
        val senderBlockchainId = UserAccount.computeBlockchainId(sender)
        val recipientBlockchainId = UserAccount.computeBlockchainId(recipient)

        val packet = MeshPacket(
            protocolVersion = 4,
            packetType = PacketType.MESSAGE,
            senderId = sender,
            recipientId = recipient,
            senderBlockchainId = senderBlockchainId,
            recipientBlockchainId = recipientBlockchainId,
            conversationId = "direct_$recipient",
            payload = "ENCRYPTED_TEXT_BASE64"
        )

        val jsonString = packet.toJsonString()
        val deserialized = MeshPacket.fromJsonString(jsonString)

        assertNotNull(deserialized)
        assertEquals(sender, deserialized?.senderId)
        assertEquals(recipient, deserialized?.recipientId)
        assertEquals(senderBlockchainId, deserialized?.senderBlockchainId)
        assertEquals(recipientBlockchainId, deserialized?.recipientBlockchainId)
        assertEquals("direct_bob", deserialized?.conversationId)
    }

    @Test
    fun testEmergencySosPacketPriorityAndAuthorityAddressing() {
        val sender = "citizen_node"
        val senderBlockchainId = UserAccount.computeBlockchainId(sender)

        val sosPacket = MeshPacket(
            protocolVersion = 4,
            packetType = PacketType.SOS,
            senderId = sender,
            recipientId = "AUTHORITY_BROADCAST",
            senderBlockchainId = senderBlockchainId,
            recipientBlockchainId = "ALL",
            priority = PacketPriority.SOS,
            conversationId = "EMERGENCY_SOS",
            payload = "SOS_PAYLOAD_BASE64"
        )

        assertEquals(PacketType.SOS, sosPacket.packetType)
        assertEquals(PacketPriority.SOS, sosPacket.priority)
        assertEquals(100, sosPacket.priority)
        assertEquals("AUTHORITY_BROADCAST", sosPacket.recipientId)
        assertEquals("EMERGENCY_SOS", sosPacket.conversationId)

        val json = sosPacket.toJsonString()
        val parsed = MeshPacket.fromJsonString(json)
        assertNotNull(parsed)
        assertEquals(PacketType.SOS, parsed?.packetType)
        assertEquals(100, parsed?.priority)
    }

    @Test
    fun testOfflineDelayTolerantCustodyMatchingByBlockchainId() {
        val targetUser = "offline_traveler"
        val targetBlockchainId = UserAccount.computeBlockchainId(targetUser)

        // Intermediate relay node storing DTN bundle
        val directPacket = MeshPacket(
            protocolVersion = 4,
            senderId = "alice",
            recipientId = targetUser,
            senderBlockchainId = UserAccount.computeBlockchainId("alice"),
            recipientBlockchainId = targetBlockchainId,
            payload = "OFFLINE_BUNDLE_DATA"
        )

        // When offline traveler turns on and authenticates
        val loggedInUserBlockchainId = UserAccount.computeBlockchainId("offline_traveler")

        val matchesTarget = directPacket.recipientBlockchainId == loggedInUserBlockchainId
        assertTrue("Stored DTN bundle must match traveler's unique blockchain ID upon reconnection", matchesTarget)
    }
}
