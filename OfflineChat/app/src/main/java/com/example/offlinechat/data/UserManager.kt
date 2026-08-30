package com.example.offlinechat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class UserAccount(
    val username: String,
    val password: String,
    val role: String = "USER", // USER, NETWORK_ADMIN, SUPER_ADMIN
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("password", password)
        obj.put("role", role)
        obj.put("status", status)
        obj.put("createdAt", createdAt)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): UserAccount {
            return UserAccount(
                username = obj.optString("username", ""),
                password = obj.optString("password", ""),
                role = obj.optString("role", "USER"),
                status = obj.optString("status", "ACTIVE"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

class UserManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("whisp_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context.applicationContext).also { instance = it }
            }
        }

        val DEFAULT_ACCOUNTS = listOf(
            UserAccount("admin", "whispadmin123", role = "SUPER_ADMIN", status = "ACTIVE"),
            UserAccount("operator", "operator123", role = "NETWORK_ADMIN", status = "ACTIVE")
        )
    }

    init {
        ensureDefaultAccountsSeeded()
    }

    private fun ensureDefaultAccountsSeeded() {
        val existing = getCustomUsersJson()
        if (existing.length() == 0) {
            val arr = JSONArray()
            DEFAULT_ACCOUNTS.forEach { arr.put(it.toJson()) }
            prefs.edit().putString("custom_users_list", arr.toString()).apply()
        }
    }

    private fun getCustomUsersJson(): JSONArray {
        val raw = prefs.getString("custom_users_list", "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun saveUsers(users: List<UserAccount>) {
        val arr = JSONArray()
        users.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("custom_users_list", arr.toString()).apply()
    }

    @Synchronized
    fun getAllUsers(): List<UserAccount> {
        val arr = getCustomUsersJson()
        val list = mutableListOf<UserAccount>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i)
            if (obj != null) {
                list.add(UserAccount.fromJson(obj))
            }
        }
        if (list.isEmpty()) {
            return DEFAULT_ACCOUNTS
        }
        return list
    }

    @Synchronized
    fun findUser(username: String): UserAccount? {
        val clean = username.trim()
        return getAllUsers().firstOrNull { it.username.equals(clean, ignoreCase = true) }
    }

    @Synchronized
    fun registerAccount(username: String, password: String, role: String = "USER"): Pair<Boolean, String> {
        val cleanUser = username.trim()
        val cleanPass = password.trim()

        if (cleanUser.length < 3) {
            return Pair(false, "Username must be at least 3 characters.")
        }
        if (!cleanUser.matches(Regex("^[a-zA-Z0-9_.-]+$"))) {
            return Pair(false, "Username can only contain letters, numbers, dots, and hyphens.")
        }
        if (cleanPass.length < 4) {
            return Pair(false, "Password must be at least 4 characters.")
        }

        val all = getAllUsers().toMutableList()
        if (all.any { it.username.equals(cleanUser, ignoreCase = true) }) {
            return Pair(false, "Username '$cleanUser' is already taken.")
        }

        val newAcc = UserAccount(
            username = cleanUser,
            password = cleanPass,
            role = role,
            status = "ACTIVE",
            createdAt = System.currentTimeMillis()
        )
        all.add(newAcc)
        saveUsers(all)

        return Pair(true, "Account '$cleanUser' created successfully!")
    }

    @Synchronized
    fun toggleUserStatus(username: String): Boolean {
        val clean = username.trim()
        val all = getAllUsers().toMutableList()
        val index = all.indexOfFirst { it.username.equals(clean, ignoreCase = true) }
        if (index != -1) {
            val current = all[index]
            val nextStatus = if (current.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
            all[index] = current.copy(status = nextStatus)
            saveUsers(all)
            return true
        }
        return false
    }

    @Synchronized
    fun deleteUser(username: String): Boolean {
        val clean = username.trim()
        if (clean.equals("admin", ignoreCase = true)) {
            return false // Prevent deletion of root admin
        }
        val all = getAllUsers().toMutableList()
        val removed = all.removeAll { it.username.equals(clean, ignoreCase = true) }
        if (removed) {
            saveUsers(all)
            return true
        }
        return false
    }

    fun verifyAdminPassword(password: String): Boolean {
        val clean = password.trim()
        if (clean == "whispadmin123" || clean == "operator123") {
            return true
        }
        val admins = getAllUsers().filter { it.role == "SUPER_ADMIN" || it.role == "NETWORK_ADMIN" }
        return admins.any { it.password == clean && it.status == "ACTIVE" }
    }

    fun isAdmin(username: String): Boolean {
        val user = findUser(username) ?: return username.equals("admin", ignoreCase = true)
        return (user.role == "SUPER_ADMIN" || user.role == "NETWORK_ADMIN") && user.status == "ACTIVE"
    }

    suspend fun syncRegistrationToRelay(username: String, password: String, role: String = "USER") = withContext(Dispatchers.IO) {
        val targets = listOf("http://10.0.2.2:8088", "http://192.168.1.3:8088", "http://127.0.0.1:8088", "http://127.0.0.1:8080")
        for (base in targets) {
            try {
                val endpoint = if (base.contains("8080")) "$base/api/v1/auth/register" else "$base/api/auth/register"
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 600
                conn.readTimeout = 600
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val json = JSONObject()
                json.put("username", username.trim())
                json.put("password", password.trim())
                json.put("role", role)

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()
                conn.responseCode // Read response
            } catch (e: Exception) {
                // Ignore failure if relay is offline
            }
        }
    }
}
