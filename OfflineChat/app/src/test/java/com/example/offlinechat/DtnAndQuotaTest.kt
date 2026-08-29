package com.example.offlinechat

import com.example.offlinechat.network.dtn.BundleCustodyState
import com.example.offlinechat.network.dtn.DtnBundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtnAndQuotaTest {

    @Test
    fun testDtnBundleSerializationAndCustody() {
        val bundle = DtnBundle(
            bundleId = "b-12345",
            messageId = "msg-12345",
            source = "Node-Alice",
            destination = "Node-Bob",
            creationTime = 1000000L,
            expirationTime = 2000000L,
            ttl = 60,
            priority = 50,
            hopCount = 2,
            replicationCount = 1,
            maxReplications = 3,
            payload = "ZW5jcnlwdGVkX3BheWxvYWQ=",
            custodyState = BundleCustodyState.STORED,
            deliveryProbability = 0.85f
        )

        val jsonStr = bundle.toJsonString()
        val parsed = DtnBundle.fromJsonString(jsonStr)

        assertNotNull(parsed)
        assertEquals("b-12345", parsed!!.bundleId)
        assertEquals("Node-Alice", parsed.source)
        assertEquals("Node-Bob", parsed.destination)
        assertEquals(BundleCustodyState.STORED, parsed.custodyState)
        assertEquals(0.85f, parsed.deliveryProbability, 0.01f)
        assertTrue(parsed.canReplicate())
        assertFalse(parsed.isExpired(1500000L))
        assertTrue(parsed.isExpired(2500000L))
    }

    @Test
    fun testBundleInventoryDiffCalculation() {
        val localBundles = setOf("b-1", "b-2")
        val remoteInventory = listOf("b-2", "b-3", "b-4")

        val missingLocally = remoteInventory.filter { !localBundles.contains(it) }
        assertEquals(listOf("b-3", "b-4"), missingLocally)
    }
}
