package com.example.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CameraState
import com.example.ui.PackingViewModel
import com.example.ui.components.CameraView
import com.example.ui.components.ScannerOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordScreen(
    viewModel: PackingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    val cameraState by viewModel.cameraState.collectAsState()
    val currentOrderCode by viewModel.currentOrderCode.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var isFrontCamera by remember { mutableStateOf(false) }
    var showSimulatorDialog by remember { mutableStateOf(false) }
    var simulatedCodeInput by remember { mutableStateOf("") }

    // Callbacks provided by CameraView to trigger recording start/stop on CameraX
    var startRecordingTrigger by remember { mutableStateOf<((File) -> Unit)?>(null) }
    var stopRecordingTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (!permissionsState.allPermissionsGranted) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cần quyền Máy ảnh & Micro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Phần mềm cần quyền truy cập camera để quét mã vạch tem đóng hàng và micro để tự động ghi hình video đóng gói.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { permissionsState.launchMultiplePermissionRequest() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CẤP QUYỀN TRUY CẬP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {

        // 1. Live Camera Feed
        CameraView(
            cameraState = cameraState,
            torchEnabled = settings.torchEnabled,
            isFrontCamera = isFrontCamera,
            onBarcodeScanned = { code ->
                startRecordingTrigger?.let { startFn ->
                    viewModel.onOrderCodeScanned(code, startFn)
                }
            },
            onVideoFileSaved = { file, success ->
                viewModel.onVideoFileSaved(file, success)
            },
            onVideoCaptureReady = { startFn, stopFn ->
                startRecordingTrigger = startFn
                stopRecordingTrigger = stopFn
            }
        )

        // 2. Scanner Overlay & Target Frame
        ScannerOverlay(
            cameraState = cameraState,
            orderCode = currentOrderCode,
            recordingSeconds = recordingSeconds
        )

        // 3. Bottom Control Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (cameraState == CameraState.RECORDING) {
                    // Big Recording Stop Action Button
                    Button(
                        onClick = {
                            stopRecordingTrigger?.invoke()
                            viewModel.onStopRecording {
                                stopRecordingTrigger?.invoke()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HOÀN THÀNH / DỪNG QUAY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                } else {
                    // Quick Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Flashlight Toggle
                        IconButton(
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (settings.torchEnabled) Color(0xFFF59E0B) else Color(0xFF334155),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (settings.torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = Color.White
                            )
                        }

                        // Simulator Button (Very helpful for testing without physical barcode labels)
                        FilledTonalButton(
                            onClick = {
                                simulatedCodeInput = "SPX${(100000000..999999999).random()}"
                                showSimulatorDialog = true
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Giả Lập Quét Mã", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Flip Camera Front/Rear
                        IconButton(
                            onClick = { isFrontCamera = !isFrontCamera },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF334155), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFrontCamera) Icons.Default.CameraFront else Icons.Default.CameraRear,
                                contentDescription = "Flip Camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Code Simulator Dialog
    if (showSimulatorDialog) {
        AlertDialog(
            onDismissRequest = { showSimulatorDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Giả Lập Quét Tem Đóng Hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Nhập hoặc tạo mã đơn hàng thử nghiệm để kích hoạt quy trình tự động đọc mã vạch và quay video:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = simulatedCodeInput,
                        onValueChange = { simulatedCodeInput = it },
                        label = { Text("Mã vạch / QR đơn hàng") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { simulatedCodeInput = "SPX${(100000000..999999999).random()}" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tạo SPX", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { simulatedCodeInput = "GHTK${(100000000..999999999).random()}" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tạo GHTK", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSimulatorDialog = false
                        startRecordingTrigger?.let { startFn ->
                            viewModel.onOrderCodeScanned(simulatedCodeInput, startFn)
                        }
                    },
                    enabled = simulatedCodeInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("QUÉT MÃ & BẮT ĐẦU")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulatorDialog = false }) {
                    Text("HỦY")
                }
            }
        )
    }
}
