package com.example.offlinechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.ui.theme.*
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrdtNotesScreen(
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as OfflineChatApp
    val crdtEngine = app.crdtEngine

    val documentStates by crdtEngine.documentStates.collectAsState()
    val eventPlanDoc = documentStates["event_plan"] ?: emptyMap()

    var newTaskText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CRDT COLLABORATION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text("OFFLINE-FIRST SHARED CHECKLIST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalEmerald, letterSpacing = 0.5.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBlack,
                    titleContentColor = PureWhite
                )
            )
        },
        containerColor = ObsidianBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Info Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DOCUMENT: Shared Event Plan", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PureWhite)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SignalEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("P2P CRDT SYNC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalEmerald)
                        }
                    }
                    Text(
                        "Edits made offline sync deterministically across the mesh with zero data loss and no central server.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Task Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add shared task...", color = TextMuted, fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = SurfaceBorder,
                        unfocusedBorderColor = SurfaceBorderSubtle,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .clickable {
                            if (newTaskText.isNotBlank()) {
                                val key = "task_${System.currentTimeMillis()}"
                                val valObj = JSONObject().apply {
                                    put("text", newTaskText.trim())
                                    put("completed", false)
                                }
                                crdtEngine.updateField("event_plan", key, valObj.toString())
                                newTaskText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add", tint = ObsidianBlack)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("COLLABORATIVE ITEMS (${eventPlanDoc.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (eventPlanDoc.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .border(1.dp, SurfaceBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items in shared document yet. Add one above!", fontSize = 12.sp, color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eventPlanDoc.entries.toList(), key = { it.key }) { (key, valueJson) ->
                        val parsed = try { JSONObject(valueJson) } catch (e: Exception) { null }
                        val text = parsed?.optString("text", valueJson) ?: valueJson
                        val isCompleted = parsed?.optBoolean("completed", false) ?: false

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isCompleted,
                                        onCheckedChange = { checked ->
                                            val updated = JSONObject().apply {
                                                put("text", text)
                                                put("completed", checked)
                                            }
                                            crdtEngine.updateField("event_plan", key, updated.toString())
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SignalEmerald,
                                            uncheckedColor = TextSecondary,
                                            checkmarkColor = ObsidianBlack
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCompleted) TextMuted else PureWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = { crdtEngine.deleteField("event_plan", key) }
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
