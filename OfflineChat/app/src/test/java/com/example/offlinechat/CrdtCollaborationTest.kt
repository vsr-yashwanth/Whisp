package com.example.offlinechat

import com.example.offlinechat.crdt.CrdtOpType
import com.example.offlinechat.crdt.CrdtOperation
import com.example.offlinechat.crdt.LWWMapCrdt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrdtCollaborationTest {

    @Test
    fun testOfflineConcurrentEditsDeterministicMerge() {
        val mapA = LWWMapCrdt("shared-event-plan")
        val mapB = LWWMapCrdt("shared-event-plan")

        // User A offline: Sets "sound_check" = "DONE" at Lamport Clock 10
        val opA = CrdtOperation(
            documentId = "shared-event-plan",
            actorId = "User-Alice",
            lamportClock = 10L,
            timestamp = 1000L,
            type = CrdtOpType.SET,
            key = "sound_check",
            valueJson = "DONE"
        )

        // User B offline: Sets "registration" = "READY" at Lamport Clock 12
        val opB = CrdtOperation(
            documentId = "shared-event-plan",
            actorId = "User-Bob",
            lamportClock = 12L,
            timestamp = 1050L,
            type = CrdtOpType.SET,
            key = "registration",
            valueJson = "READY"
        )

        mapA.apply(opA)
        mapB.apply(opB)

        // Cross-sync when network reconnects
        mapA.apply(opB)
        mapB.apply(opA)

        // Both replicas must converge to identical state without data loss!
        assertEquals("DONE", mapA.get("sound_check"))
        assertEquals("READY", mapA.get("registration"))
        assertEquals(mapA.getAll(), mapB.getAll())
    }

    @Test
    fun testConflictingKeyDeterministicResolution() {
        val map = LWWMapCrdt("status-doc")

        // Concurrent edit on same key "stage_status"
        val opEarly = CrdtOperation(
            documentId = "status-doc",
            actorId = "User-Alice",
            lamportClock = 5L,
            timestamp = 500L,
            key = "stage_status",
            valueJson = "PREPARING"
        )

        val opLater = CrdtOperation(
            documentId = "status-doc",
            actorId = "User-Bob",
            lamportClock = 8L, // Higher Lamport clock wins
            timestamp = 800L,
            key = "stage_status",
            valueJson = "COMPLETED"
        )

        map.apply(opEarly)
        map.apply(opLater)
        assertEquals("COMPLETED", map.get("stage_status"))

        // Even if opEarly arrives after opLater (out of order delivery), it must not overwrite
        val mapReversed = LWWMapCrdt("status-doc")
        mapReversed.apply(opLater)
        mapReversed.apply(opEarly)
        assertEquals("COMPLETED", mapReversed.get("stage_status"))
    }

    @Test
    fun testDeletionTombstoneHandling() {
        val map = LWWMapCrdt("doc-delete")

        val setOp = CrdtOperation(
            documentId = "doc-delete",
            actorId = "User-Alice",
            lamportClock = 1L,
            key = "item-1",
            valueJson = "Value"
        )
        map.apply(setOp)
        assertEquals("Value", map.get("item-1"))

        val delOp = CrdtOperation(
            documentId = "doc-delete",
            actorId = "User-Alice",
            lamportClock = 2L,
            type = CrdtOpType.DELETE,
            key = "item-1"
        )
        map.apply(delOp)
        assertNull(map.get("item-1"))
    }
}
