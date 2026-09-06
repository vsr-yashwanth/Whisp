package com.example.offlinechat.network

import java.util.concurrent.CopyOnWriteArrayList

data class AdminAuditEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val adminIdentity: String = "Operator-Admin",
    val action: String,
    val resource: String,
    val result: String = "SUCCESS",
    val reason: String = "Routine operator maintenance"
)

/**
 * Thread-safe, bounded in-memory audit log manager for administrative actions.
 */
class AdminAuditManager(private val maxCapacity: Int = 500) {

    private val auditLogs = CopyOnWriteArrayList<AdminAuditEvent>()

    init {
        logAction("SYSTEM_BOOT", "AdminControlPlane", "SUCCESS", "Whisp Admin Control Plane initialized")
    }

    fun logAction(action: String, resource: String, result: String = "SUCCESS", reason: String = "Operator action") {
        if (auditLogs.size >= maxCapacity) {
            auditLogs.removeAt(0)
        }
        auditLogs.add(
            AdminAuditEvent(
                action = action,
                resource = resource,
                result = result,
                reason = reason
            )
        )
    }

    fun getAllLogs(): List<AdminAuditEvent> {
        return auditLogs.reversed()
    }

    fun clear() {
        auditLogs.clear()
        logAction("AUDIT_RESET", "AuditLogs", "SUCCESS", "Audit logs cleared by SuperAdmin")
    }
}
