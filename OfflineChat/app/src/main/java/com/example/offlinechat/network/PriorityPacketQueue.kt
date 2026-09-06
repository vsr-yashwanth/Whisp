package com.example.offlinechat.network

import java.util.PriorityQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PriorityPacketQueue(
    private val maxCapacity: Int = 1000,
    private val highPriorityPreemptionRatio: Int = 5 // Allow 1 lower-priority packet every 5 high-priority ones if queued
) {

    private data class QueuedItem(
        val packet: MeshPacket,
        val enqueueIndex: Long
    ) : Comparable<QueuedItem> {
        override fun compareTo(other: QueuedItem): Int {
            // 1. Higher priority number comes first
            val prioDiff = other.packet.priority.compareTo(this.packet.priority)
            if (prioDiff != 0) return prioDiff
            // 2. Earlier enqueue index comes first (FIFO within same priority)
            return this.enqueueIndex.compareTo(other.enqueueIndex)
        }
    }

    private val lock = ReentrantLock()
    private val highPriorityQueue = PriorityQueue<QueuedItem>()
    private val normalPriorityQueue = PriorityQueue<QueuedItem>()
    private var sequenceCounter: Long = 0L
    private var highPriorityDispatchCount: Int = 0

    fun enqueue(packet: MeshPacket): Boolean {
        lock.withLock {
            val totalSize = highPriorityQueue.size + normalPriorityQueue.size
            if (totalSize >= maxCapacity) {
                // If capacity is reached, drop the lowest priority item in normalQueue to make room for SOS/High priority
                if (packet.priority >= PacketPriority.IMPORTANT && normalPriorityQueue.isNotEmpty()) {
                    normalPriorityQueue.poll()
                } else if (totalSize >= maxCapacity) {
                    return false // Drop lowest priority
                }
            }

            val item = QueuedItem(packet, sequenceCounter++)
            if (packet.priority >= PacketPriority.IMPORTANT) {
                highPriorityQueue.offer(item)
            } else {
                normalPriorityQueue.offer(item)
            }
            return true
        }
    }

    fun poll(): MeshPacket? {
        lock.withLock {
            // SOS and Emergency packets are always preemptive
            val topHigh = highPriorityQueue.peek()
            if (topHigh != null && topHigh.packet.priority >= PacketPriority.SOS) {
                return highPriorityQueue.poll()?.packet
            }

            // Fair scheduling between IMPORTANT and NORMAL
            if (highPriorityQueue.isNotEmpty() && normalPriorityQueue.isNotEmpty()) {
                if (highPriorityDispatchCount >= highPriorityPreemptionRatio) {
                    highPriorityDispatchCount = 0
                    return normalPriorityQueue.poll()?.packet
                } else {
                    highPriorityDispatchCount++
                    return highPriorityQueue.poll()?.packet
                }
            } else if (highPriorityQueue.isNotEmpty()) {
                return highPriorityQueue.poll()?.packet
            } else if (normalPriorityQueue.isNotEmpty()) {
                return normalPriorityQueue.poll()?.packet
            }
            return null
        }
    }

    fun peek(): MeshPacket? {
        lock.withLock {
            val topHigh = highPriorityQueue.peek()
            val topNormal = normalPriorityQueue.peek()
            return when {
                topHigh != null && topNormal != null -> if (topHigh <= topNormal) topHigh.packet else topNormal.packet
                topHigh != null -> topHigh.packet
                topNormal != null -> topNormal.packet
                else -> null
            }
        }
    }

    fun size(): Int {
        lock.withLock {
            return highPriorityQueue.size + normalPriorityQueue.size
        }
    }

    fun isEmpty(): Boolean {
        lock.withLock {
            return highPriorityQueue.isEmpty() && normalPriorityQueue.isEmpty()
        }
    }

    fun clear() {
        lock.withLock {
            highPriorityQueue.clear()
            normalPriorityQueue.clear()
            highPriorityDispatchCount = 0
        }
    }
}
