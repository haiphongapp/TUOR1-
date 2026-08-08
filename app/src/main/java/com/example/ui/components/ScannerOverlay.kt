package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CameraState
import com.example.util.FormatUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScannerOverlay(
    cameraState: CameraState,
    orderCode: String,
    recordingSeconds: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    
    // Live date-time string
    var currentDateTimeString by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault())
        while (true) {
            currentDateTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Laser scan line animation
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserLine"
    )

    // Pulsating red recording dot
    val recordAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "redDot"
    )

    Box(modifier = modifier.fillMaxSize()) {

        // Draw Scanner Frame & Target Box when in Scanning state
        if (cameraState == CameraState.SCANNING) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boxWidth = size.width * 0.8f
                val boxHeight = size.height * 0.35f
                val left = (size.width - boxWidth) / 2
                val top = (size.height - boxHeight) / 2 - 40.dp.toPx()

                // Semi-transparent overlay outside scan area
                drawRect(
                    color = Color.Black.copy(alpha = 0.45f)
                )

                // Clear the middle box
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                )

                // Target Corner Accents (Cyan/Green)
                val cornerLength = 28.dp.toPx()
                val strokeWidth = 5.dp.toPx()
                val cornerColor = Color(0xFF10B981)

                // Top Left
                drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

                // Top Right
                drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLength, top), strokeWidth)
                drawLine(cornerColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLength), strokeWidth)

                // Bottom Left
                drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left + cornerLength, top + boxHeight), strokeWidth)
                drawLine(cornerColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLength), strokeWidth)

                // Bottom Right
                drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLength, top + boxHeight), strokeWidth)
                drawLine(cornerColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - cornerLength), strokeWidth)

                // Animated Laser Line
                val laserY = top + (boxHeight * laserYRatio)
                drawLine(
                    color = cornerColor.copy(alpha = 0.85f),
                    start = Offset(left + 10.dp.toPx(), laserY),
                    end = Offset(left + boxWidth - 10.dp.toPx(), laserY),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            }
        }

        // Real-time Date-Time Overlay Watermark (Auto inserted onto video preview)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentDateTimeString,
                    color = Color(0xFF22C55E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Status Header / Badge
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (cameraState) {
                CameraState.SCANNING -> {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        contentColor = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HÃY ĐƯA MÃ VẠCH / QR VÀO KHUNG CÂU",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                CameraState.RECORDING -> {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF991B1B).copy(alpha = 0.95f),
                        contentColor = Color.White,
                        shadowElevation = 10.dp,
                        modifier = Modifier.border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = recordAlpha))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ĐANG GHI HÌNH ĐÓNG HÀNG [${FormatUtils.formatDuration(recordingSeconds)}]",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    if (orderCode.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "Mã đơn: $orderCode",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                CameraState.SAVING -> {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF065F46).copy(alpha = 0.9f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏳ ĐANG XỬ LÝ LƯU VIDEO...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

