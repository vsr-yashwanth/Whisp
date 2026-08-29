package com.example.offlinechat.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("yashwanth") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val prefs = remember { context.getSharedPreferences("whisp_auth_prefs", Context.MODE_PRIVATE) }

    fun performLogin(u: String, p: String) {
        if (u.isBlank() || p.isBlank()) {
            errorMessage = "Please enter both username and password"
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val success = withContext(Dispatchers.IO) {
                var authOk = false
                val cleanUser = u.trim()
                val cleanPass = p.trim()

                // 1. Check local pre-injected credentials first (offline guaranteed)
                val isLocalValid = (cleanUser == "yashwanth" && cleanPass == "password123") ||
                        (cleanUser == "user" && cleanPass == "whisp123") ||
                        (cleanUser == "admin" && cleanPass == "whispadmin123") ||
                        (cleanUser == "alice" && cleanPass == "alice123") ||
                        (cleanUser == "bob" && cleanPass == "bob123")

                if (isLocalValid) {
                    authOk = true
                }

                // 2. Also try MongoDB relay server on port 8088 if reachable
                val targets = listOf("http://10.0.2.2:8088", "http://192.168.1.3:8088", "http://127.0.0.1:8088")
                for (base in targets) {
                    try {
                        val url = URL("$base/api/auth/login")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.connectTimeout = 800
                        conn.readTimeout = 800
                        conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json")

                        val json = JSONObject()
                        json.put("username", cleanUser)
                        json.put("password", cleanPass)

                        val writer = OutputStreamWriter(conn.outputStream)
                        writer.write(json.toString())
                        writer.flush()
                        writer.close()

                        if (conn.responseCode == 200) {
                            authOk = true
                            break
                        }
                    } catch (e: Exception) {
                        // Keep checking
                    }
                }
                authOk
            }

            isLoading = false
            if (success) {
                prefs.edit()
                    .putString("logged_in_user", u.trim())
                    .putBoolean("is_logged_in", true)
                    .apply()
                Toast.makeText(context, "Welcome to Whisp, ${u.trim()}!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(u.trim())
            } else {
                errorMessage = "Invalid credentials. Please verify your MongoDB login."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Logo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Whisp Logo",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "WHISP SECURE MESH",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "Local MongoDB Auth (whisp_db)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF2A2A36),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                    },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { performLogin(username, password) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF2A2A36),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Credential Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B1B24), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRE-INJECTED MONGODB ACCOUNTS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                username = "yashwanth"
                                password = "password123"
                            },
                            label = { Text("yashwanth", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                username = "user"
                                password = "whisp123"
                            },
                            label = { Text("user", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                username = "admin"
                                password = "whispadmin123"
                            },
                            label = { Text("admin", fontSize = 11.sp) }
                        )
                    }
                }

                // Error Message Banner
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Submit Button
                Button(
                    onClick = { performLogin(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SIGN IN TO WHISP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
