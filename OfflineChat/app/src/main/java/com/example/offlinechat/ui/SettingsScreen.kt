package com.example.offlinechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.titleMedium, color = PureWhite) },
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
            Text("DEVICE & IDENTITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Node Name", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PureWhite)
                    }
                    Divider(color = SurfaceBorderSubtle)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Local Node ID", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Node-${android.os.Build.MODEL.take(10)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PureWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("SECURITY & HARDWARE CRYPTO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Storage Cipher", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("AES-256-GCM (Keystore)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PureWhite)
                    }
                    Divider(color = SurfaceBorderSubtle)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Transit Security", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Zero-Knowledge AEAD", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SignalEmerald)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
            ) {
                Text("Wipe Local Encrypted Database", color = ErrorMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
