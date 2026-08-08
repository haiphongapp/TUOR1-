package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PackingViewModel

@Composable
fun HomeScreen(
    viewModel: PackingViewModel,
    onNavigateToRecord: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.packingRecords.collectAsState()
    val recordCount by viewModel.recordCount.collectAsState()

    var selectedFilter by remember { mutableStateOf("HÔM NAY") } // HÔM NAY, HÔM QUA, KHOẢNG NGÀY

    val todayRecordsCount = remember(records, selectedFilter) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L
        when (selectedFilter) {
            "HÔM NAY" -> records.count { now - it.timestamp < oneDayMillis }
            "HÔM QUA" -> records.count { (now - it.timestamp) in oneDayMillis..(2 * oneDayMillis) }
            else -> records.size
        }
    }

    val returnedCount = remember(records, selectedFilter) {
        records.count { it.status == "CẦN KIỂM TRA" }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Logo Header: Haiphongapp
        HaiphongAppLogoHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Date Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("HÔM NAY", "HÔM QUA", "KHOẢNG NGÀY").forEach { filterName ->
                val isSelected = selectedFilter == filterName
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) Color(0xFFFF7A00) else Color(0xFF23272F)
                        )
                        .clickable { selectedFilter = filterName },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filterName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Stats Card (Navy Blue Container)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F6E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sent Orders
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ĐƠN GỬI ĐI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$todayRecordsCount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF7A00)
                    )
                }

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                // Returned Orders
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HÀNG HOÀN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$returnedCount",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFA855F7)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Primary Action Grid (Row 1: Video Record & Photo Snapshot)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Orange Tile: QUAY VIDEO ĐÓNG HÀNG
            Surface(
                onClick = onNavigateToRecord,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFFF7A00),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "QUAY VIDEO\nĐÓNG HÀNG",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // White Tile: CHỤP ẢNH ĐƠN HÀNG
            Surface(
                onClick = onNavigateToRecord,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFF3E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFFFF7A00),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CHỤP ẢNH\nĐƠN HÀNG",
                        color = Color(0xFFFF7A00),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Secondary Action Grid (Row 2: History Search & Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // White Tile: TRA CỨU LỊCH SỬ
            Surface(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TRA CỨU\nLỊCH SỬ",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // Green Tile: CÀI ĐẶT HỆ THỐNG
            Surface(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF16A34A),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CÀI ĐẶT\nHỆ THỐNG",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HaiphongAppLogoHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE0F2FE),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "📦", fontSize = 32.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Haiphongapp",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0284C7)
                )
                Text(
                    text = "Packing",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF7A00)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "QUAY VIDEO ĐÓNG HÀNG",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1E293B),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CloudManagerDialog(
    recordCount: Int,
    onDismiss: () -> Unit,
    onCopyLink: (String) -> Unit
) {
    val cloudUrl = "https://drive.google.com/drive/folders/carrot_packing_videos"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = Color(0xFF1E88E5),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Quản Lý Link Cloud Video",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Toàn bộ $recordCount video đóng hàng được đồng bộ tự động lên thư mục Google Drive Cloud của cửa hàng.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cloudUrl,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onCopyLink(cloudUrl) }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCopyLink(cloudUrl) }) {
                Text("SAO CHÉP LINK CLOUD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ĐÓNG")
            }
        }
    )
}

@Composable
fun DisputeClaimDialog(
    viewModel: PackingViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val records by viewModel.packingRecords.collectAsState()

    var selectedOrderCode by remember { mutableStateOf("") }
    var disputeReason by remember { mutableStateOf("Khách báo giao thiếu hàng / Hàng vỡ hỏng") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = Color(0xFFFF7A00),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Tạo Hồ Sơ Kháng Nghị Sàn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Xuất thông tin video đóng hàng kèm mã vạch để làm bằng chứng khiếu nại TikTok Shop, Shopee, Lazada:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = selectedOrderCode,
                    onValueChange = { selectedOrderCode = it },
                    label = { Text("Nhập hoặc chọn Mã Đơn Hàng") },
                    placeholder = { Text("Ví dụ: SPX998241031") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = disputeReason,
                    onValueChange = { disputeReason = it },
                    label = { Text("Lý do kháng nghị") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val codeText = if (selectedOrderCode.isNotBlank()) selectedOrderCode else "SPX998241031"
                    val claimText = """
                        [HỒ SƠ KHÁNG NGHỊ BẰNG CHỨNG ĐÓNG HÀNG]
                        Mã đơn hàng: $codeText
                        Lý do: $disputeReason
                        Trạng thái video: Đã ghi hình tự động & Lưu vết vạch tem.
                        Link video đóng gói: https://drive.google.com/drive/folders/carrot_packing_videos?search=$codeText
                    """.trimIndent()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("DisputeClaim", claimText))
                    viewModel.showToast("Đã chép Hồ Sơ Kháng Nghị đơn $codeText!")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00))
            ) {
                Text("TẠO & CHÉP BẰNG CHỨNG")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY")
            }
        }
    )
}
