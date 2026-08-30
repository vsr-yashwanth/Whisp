package com.example.offlinechat.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.data.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userManager = remember { UserManager.getInstance(context) }

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Form fields
    var username by remember { mutableStateOf("yashwanth") }
    var password by remember { mutableStateOf("password123") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val prefs = remember { context.getSharedPreferences("whisp_auth_prefs", Context.MODE_PRIVATE) }

    fun performLogin(u: String, p: String) {
        if (u.isBlank() || p.isBlank()) {
            errorMessage = "Please enter both username and password"
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val cleanUser = u.trim()
            val cleanPass = p.trim()

            var authOk = false
            var userRole = "USER"

            // 1. Check local UserManager (includes pre-seeded and dynamically registered accounts)
            val localUser = userManager.findUser(cleanUser)
            if (localUser != null) {
                if (localUser.status == "SUSPENDED") {
                    isLoading = false
                    errorMessage = "Account '$cleanUser' has been suspended by network administrator."
                    return@launch
                }
                if (localUser.password == cleanPass) {
                    authOk = true
                    userRole = localUser.role
                }
            }

            // 2. Also try MongoDB relay server on port 8088 if reachable
            if (!authOk) {
                withContext(Dispatchers.IO) {
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
                }
            }

            isLoading = false
            if (authOk) {
                prefs.edit()
                    .putString("logged_in_user", cleanUser)
                    .putString("logged_in_role", userRole)
                    .putBoolean("is_logged_in", true)
                    .apply()
                Toast.makeText(context, "Welcome to Whisp, $cleanUser!", Toast.LENGTH_SHORT).show()
                onLoginSuccess(cleanUser)
            } else {
                errorMessage = "Invalid username or password. Please verify your credentials."
            }
        }
    }

    fun performRegister(u: String, p: String, confirmP: String) {
        val cleanUser = u.trim()
        val cleanPass = p.trim()
        val cleanConfirm = confirmP.trim()

        if (cleanUser.isBlank() || cleanPass.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }

        if (cleanPass != cleanConfirm) {
            errorMessage = "Passwords do not match"
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val (registered, msg) = userManager.registerAccount(cleanUser, cleanPass, role = "USER")
            if (registered) {
                // Sync with relay server in background
                userManager.syncRegistrationToRelay(cleanUser, cleanPass, role = "USER")

                isLoading = false
                successMessage = "Account '$cleanUser' created successfully! You can now log in."
                Toast.makeText(context, "Account created! Signing in...", Toast.LENGTH_SHORT).show()
                
                // Auto sign in with the new account
                performLogin(cleanUser, cleanPass)
            } else {
                isLoading = false
                errorMessage = msg
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .padding(20.dp),
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
                            text = "Zero-Trust Identity & Auth",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Tab Selector: SIGN IN vs CREATE ACCOUNT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1B24))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.SIGN_IN) Color.White else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                errorMessage = null
                                successMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SIGN IN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMode == AuthMode.SIGN_IN) Color.Black else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.CREATE_ACCOUNT) Color(0xFF10B981) else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.CREATE_ACCOUNT
                                errorMessage = null
                                successMessage = null
                                if (username == "yashwanth") username = ""
                                if (password == "password123") password = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CREATE ACCOUNT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMode == AuthMode.CREATE_ACCOUNT) Color.Black else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

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
                    label = { Text(if (authMode == AuthMode.CREATE_ACCOUNT) "Choose Password (min 4 chars)" else "Password") },
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
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (authMode == AuthMode.CREATE_ACCOUNT) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (authMode == AuthMode.SIGN_IN) performLogin(username, password)
                        }
                    ),
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

                // Confirm Password (Only in CREATE ACCOUNT mode)
                AnimatedVisibility(visible = authMode == AuthMode.CREATE_ACCOUNT) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { performRegister(username, password, confirmPassword) }
                        ),
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
                }

                // Quick Credential Chips (Only shown on Sign In)
                if (authMode == AuthMode.SIGN_IN) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B1B24), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PRE-CONFIGURED SEED ACCOUNTS:",
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
                                    username = "alice"
                                    password = "alice123"
                                },
                                label = { Text("alice", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Success Message Banner
                successMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                    onClick = {
                        if (authMode == AuthMode.SIGN_IN) {
                            performLogin(username, password)
                        } else {
                            performRegister(username, password, confirmPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (authMode == AuthMode.SIGN_IN) Color.White else Color(0xFF10B981),
                        contentColor = Color.Black
                    ),
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
                            text = if (authMode == AuthMode.SIGN_IN) "SIGN IN TO WHISP" else "REGISTER & SIGN IN",
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

