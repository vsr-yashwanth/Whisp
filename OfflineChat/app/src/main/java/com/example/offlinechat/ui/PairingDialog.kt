package com.example.offlinechat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.offlinechat.network.PairingRequest
import com.example.offlinechat.ui.theme.*

@Composable
fun PairingDialog(
    request: PairingRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        containerColor = SurfaceDark,
        title = {
            Text("PEER AUTHENTICATION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, style = MaterialTheme.typography.titleMedium, color = PureWhite)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Compare this cryptographic pairing code with ${request.peerName}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.authenticationToken,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PureWhite,
                        letterSpacing = 2.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm", color = ObsidianBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Reject", color = TextSecondary)
            }
        }
    )
}
