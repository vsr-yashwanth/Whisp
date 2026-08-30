package com.example.offlinechat

import com.example.offlinechat.data.UserAccount
import org.junit.Assert.*
import org.junit.Test

class AccountManagementAndAdminGateTest {

    // In-memory test repository simulating UserManager logic
    class TestAccountManager {
        private val users = mutableMapOf<String, UserAccount>()

        init {
            // Seed defaults
            register("admin", "whispadmin123", "SUPER_ADMIN")
            register("operator", "operator123", "NETWORK_ADMIN")
            register("yashwanth", "password123", "USER")
            register("user", "whisp123", "USER")
            register("alice", "alice123", "USER")
            register("bob", "bob123", "USER")
        }

        fun register(u: String, p: String, role: String = "USER"): Pair<Boolean, String> {
            val username = u.trim()
            val password = p.trim()
            if (username.length < 3) return Pair(false, "Username must be at least 3 characters")
            if (password.length < 4) return Pair(false, "Password must be at least 4 characters")
            if (users.containsKey(username.lowercase())) return Pair(false, "Username already exists")

            val acc = UserAccount(username = username, password = password, role = role, status = "ACTIVE")
            users[username.lowercase()] = acc
            return Pair(true, "Account created successfully")
        }

        fun authenticate(u: String, p: String): Pair<Boolean, String> {
            val acc = users[u.trim().lowercase()] ?: return Pair(false, "Invalid username or password")
            if (acc.status == "SUSPENDED") return Pair(false, "Account is suspended by administrator")
            if (acc.password != p.trim()) return Pair(false, "Invalid username or password")
            return Pair(true, "Authentication successful")
        }

        fun verifyAdminPassword(pass: String): Boolean {
            val p = pass.trim()
            return p == "whispadmin123" || p == "operator123" || users.values.any { 
                (it.role == "SUPER_ADMIN" || it.role == "NETWORK_ADMIN") && it.password == p && it.status == "ACTIVE" 
            }
        }

        fun toggleStatus(u: String): Boolean {
            val acc = users[u.trim().lowercase()] ?: return false
            val newStatus = if (acc.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
            users[u.trim().lowercase()] = acc.copy(status = newStatus)
            return true
        }

        fun deleteUser(u: String): Boolean {
            val key = u.trim().lowercase()
            if (key == "admin") return false // Root admin is protected
            return users.remove(key) != null
        }

        fun getUser(u: String): UserAccount? = users[u.trim().lowercase()]
        fun getAll(): List<UserAccount> = users.values.toList()
    }

    @Test
    fun testDefaultSeededAccountsExist() {
        val manager = TestAccountManager()
        val all = manager.getAll()
        assertTrue(all.size >= 6)
        assertNotNull(manager.getUser("admin"))
        assertEquals("SUPER_ADMIN", manager.getUser("admin")?.role)
        assertNotNull(manager.getUser("operator"))
        assertEquals("NETWORK_ADMIN", manager.getUser("operator")?.role)
        assertNotNull(manager.getUser("yashwanth"))
        assertEquals("USER", manager.getUser("yashwanth")?.role)
    }

    @Test
    fun testDynamicAccountRegistrationAndValidation() {
        val manager = TestAccountManager()

        // Short username failure
        val (shortUserOk, _) = manager.register("ab", "password123")
        assertFalse(shortUserOk)

        // Short password failure
        val (shortPassOk, _) = manager.register("newuser", "123")
        assertFalse(shortPassOk)

        // Valid registration
        val (validOk, validMsg) = manager.register("charlie", "charlie1234", "USER")
        assertTrue(validOk)
        assertEquals("Account created successfully", validMsg)

        // Duplicate registration rejection
        val (dupOk, dupMsg) = manager.register("charlie", "differentpass", "USER")
        assertFalse(dupOk)
        assertTrue(dupMsg.contains("already exists"))
    }

    @Test
    fun testAuthenticationAndSuspensionGating() {
        val manager = TestAccountManager()
        manager.register("david", "davidpass")

        // Valid credentials
        val (authSuccess, _) = manager.authenticate("david", "davidpass")
        assertTrue(authSuccess)

        // Invalid credentials
        val (wrongPass, _) = manager.authenticate("david", "wrongpassword")
        assertFalse(wrongPass)

        // Suspend user
        manager.toggleStatus("david")
        assertEquals("SUSPENDED", manager.getUser("david")?.status)

        // Suspended user cannot authenticate
        val (suspendedAuth, suspendedMsg) = manager.authenticate("david", "davidpass")
        assertFalse(suspendedAuth)
        assertTrue(suspendedMsg.contains("suspended"))

        // Reactivate user
        manager.toggleStatus("david")
        assertEquals("ACTIVE", manager.getUser("david")?.status)
        val (reactivatedAuth, _) = manager.authenticate("david", "davidpass")
        assertTrue(reactivatedAuth)
    }

    @Test
    fun testAdminGatePasswordVerification() {
        val manager = TestAccountManager()

        // Default admin master passwords
        assertTrue("Master admin password must unlock gate", manager.verifyAdminPassword("whispadmin123"))
        assertTrue("Master operator password must unlock gate", manager.verifyAdminPassword("operator123"))
        assertFalse("Random string must be rejected", manager.verifyAdminPassword("randompass"))

        // Dynamic Network Admin account
        manager.register("secops", "secops2026", "NETWORK_ADMIN")
        assertTrue("Dynamic Admin password must unlock gate", manager.verifyAdminPassword("secops2026"))

        // If suspended, admin gate is locked for that credential
        manager.toggleStatus("secops")
        assertFalse("Suspended Admin password must NOT unlock gate", manager.verifyAdminPassword("secops2026"))
    }

    @Test
    fun testRootAdminProtectionAndUserDeletion() {
        val manager = TestAccountManager()
        manager.register("eve", "evepass123")

        // Non-root deletion succeeds
        assertTrue(manager.deleteUser("eve"))
        assertNull(manager.getUser("eve"))

        // Root admin deletion is protected
        assertFalse("Root admin cannot be deleted", manager.deleteUser("admin"))
        assertNotNull(manager.getUser("admin"))
    }
}
