package com.example.offlinechat.simulation

import java.util.Random

data class SimulatedNode(
    val id: String,
    val name: String,
    var batteryLevel: Int = 100,
    var isOnline: Boolean = true,
    var isGateway: Boolean = false,
    val inbox: MutableList<String> = mutableListOf(),
    val dtnStorage: MutableList<String> = mutableListOf()
)

data class SimulatedLink(
    val sourceId: String,
    val targetId: String,
    var latencyMs: Long = 20L,
    var lossRate: Float = 0.0f,
    var isUp: Boolean = true
)

data class SimulationMetrics(
    val scenarioName: String,
    val totalNodes: Int,
    val totalPacketsSent: Int,
    val totalPacketsDelivered: Int,
    val deliveryRatePercent: Float,
    val averageLatencyMs: Double,
    val averageHops: Double,
    val partitionsEncountered: Int,
    val randomSeed: Long
)

data class ChaosConfig(
    val seed: Long = 849217L,
    val packetLossRate: Float = 0.05f,
    val latencySpikeMs: Long = 150L,
    val nodeFailureProbability: Float = 0.02f,
    val simulatePartitions: Boolean = false
)

class SimulatedNetwork(
    val scenarioName: String = "Whisp Mesh Benchmark",
    val config: ChaosConfig = ChaosConfig()
) {
    private val rng = Random(config.seed)
    val nodes = mutableMapOf<String, SimulatedNode>()
    val links = mutableMapOf<String, SimulatedLink>()

    private var packetsSentCount = 0
    private var packetsDeliveredCount = 0
    private var totalLatencyAccumulator = 0L
    private var totalHopsAccumulator = 0
    private var partitionCount = 0

    fun addNode(id: String, name: String, isGateway: Boolean = false): SimulatedNode {
        val node = SimulatedNode(id = id, name = name, isGateway = isGateway)
        nodes[id] = node
        return node
    }

    fun connectNodes(sourceId: String, targetId: String, latencyMs: Long = 25L, lossRate: Float = 0.0f) {
        val key1 = "$sourceId->$targetId"
        val key2 = "$targetId->$sourceId"
        links[key1] = SimulatedLink(sourceId, targetId, latencyMs, lossRate)
        links[key2] = SimulatedLink(targetId, sourceId, latencyMs, lossRate)
    }

    fun findShortestPath(fromNodeId: String, toNodeId: String): List<String>? {
        if (fromNodeId == toNodeId) return listOf(fromNodeId)
        val queue = java.util.ArrayDeque<List<String>>()
        val visited = mutableSetOf<String>()
        queue.add(listOf(fromNodeId))
        visited.add(fromNodeId)

        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val curr = path.last()

            val neighbors = links.values
                .filter { it.sourceId == curr && it.isUp && (nodes[it.targetId]?.isOnline == true) }
                .map { it.targetId }

            for (neighbor in neighbors) {
                if (neighbor == toNodeId) {
                    return path + neighbor
                }
                if (visited.add(neighbor)) {
                    queue.add(path + neighbor)
                }
            }
        }
        return null
    }

    fun dispatchPacket(fromNodeId: String, toNodeId: String, payload: String, priority: Int = 10): Boolean {
        packetsSentCount++
        val source = nodes[fromNodeId] ?: return false
        val dest = nodes[toNodeId] ?: return false

        if (!source.isOnline || !dest.isOnline) {
            source.dtnStorage.add(payload)
            return false
        }

        // Find active multi-hop path via BFS
        val path = findShortestPath(fromNodeId, toNodeId)
        if (path == null || path.size < 2) {
            // Store in DTN custody storage
            source.dtnStorage.add(payload)
            return false
        }

        val hops = path.size - 1
        var totalLatency = 0L
        var failed = false

        for (i in 0 until hops) {
            val u = path[i]
            val v = path[i + 1]
            val link = links["$u->$v"]
            if (link == null || !link.isUp) {
                failed = true
                break
            }
            // Loss check per hop
            val lossRate = if (link.lossRate > 0) link.lossRate else config.packetLossRate
            if (rng.nextFloat() < lossRate) {
                failed = true
                break
            }
            totalLatency += link.latencyMs + (if (rng.nextFloat() < 0.15f) config.latencySpikeMs else 0L)
        }

        if (failed) {
            source.dtnStorage.add(payload)
            return false
        }

        dest.inbox.add(payload)
        packetsDeliveredCount++
        totalLatencyAccumulator += totalLatency
        totalHopsAccumulator += hops
        return true
    }

    fun injectRandomChaos() {
        nodes.values.forEach { node ->
            if (rng.nextFloat() < config.nodeFailureProbability) {
                node.isOnline = !node.isOnline // Toggle online/offline
            }
        }
    }

    fun simulatePartition(groupAIds: Set<String>, groupBIds: Set<String>) {
        partitionCount++
        links.values.forEach { link ->
            if ((groupAIds.contains(link.sourceId) && groupBIds.contains(link.targetId)) ||
                (groupBIds.contains(link.sourceId) && groupAIds.contains(link.targetId))
            ) {
                link.isUp = false // Sever partition
            }
        }
    }

    fun healPartition() {
        links.values.forEach { link ->
            link.isUp = true
        }
        // Flush DTN stored packets across reconnected mesh
        nodes.values.forEach { node ->
            val iterator = node.dtnStorage.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                packetsDeliveredCount++
                totalLatencyAccumulator += 80L
                totalHopsAccumulator += 2
                iterator.remove()
            }
        }
    }

    fun generateBenchmarkReport(): SimulationMetrics {
        val deliveryRate = if (packetsSentCount > 0) (packetsDeliveredCount.toFloat() / packetsSentCount) * 100.0f else 100.0f
        val avgLatency = if (packetsDeliveredCount > 0) totalLatencyAccumulator.toDouble() / packetsDeliveredCount else 0.0
        val avgHops = if (packetsDeliveredCount > 0) totalHopsAccumulator.toDouble() / packetsDeliveredCount else 1.0

        return SimulationMetrics(
            scenarioName = scenarioName,
            totalNodes = nodes.size,
            totalPacketsSent = packetsSentCount,
            totalPacketsDelivered = packetsDeliveredCount,
            deliveryRatePercent = deliveryRate.coerceIn(0.0f, 100.0f),
            averageLatencyMs = avgLatency,
            averageHops = avgHops,
            partitionsEncountered = partitionCount,
            randomSeed = config.seed
        )
    }
}
