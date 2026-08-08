package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PackingViewModel
import com.example.ui.components.GoogleSignInDialog
import com.example.util.LanguageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PackingViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()
    val lang = settings.language
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Screen Title
        Text(
            text = LanguageUtils.getTranslation("settings_title", lang),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = LanguageUtils.getTranslation("settings_subtitle", lang),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Language Selection (Ngôn Ngữ)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageUtils.getTranslation("settings_lang", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = settings.language == "vi",
                        onClick = { viewModel.updateSettings(settings.copy(language = "vi")) },
                        label = { Text("Tiếng Việt 🇻🇳", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = settings.language == "en",
                        onClick = { viewModel.updateSettings(settings.copy(language = "en")) },
                        label = { Text("English 🇬🇧", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Scanner & Auto Start
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageUtils.getTranslation("settings_auto_start", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageUtils.getTranslation("settings_auto_start_desc", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (lang == "en") "Automatically start video recording when barcode is scanned" else "Không cần bấm nút bắt đầu thủ công khi đưa mã vào khung",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.autoStartOnScan,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(autoStartOnScan = it))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Auto Stop Timer Duration (Set to Unlimited by default)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageUtils.getTranslation("settings_timer", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageUtils.getTranslation("settings_timer_desc", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val timeOptions = if (lang == "en") listOf(
                        0 to "Unlimited (Max)",
                        30 to "30 sec",
                        60 to "60 sec",
                        120 to "2 min"
                    ) else listOf(
                        0 to "Không giới hạn (Tối đa)",
                        30 to "30 giây",
                        60 to "60 giây",
                        120 to "2 phút"
                    )

                    timeOptions.forEach { (seconds, label) ->
                        FilterChip(
                            selected = settings.autoStopSeconds == seconds,
                            onClick = {
                                viewModel.updateSettings(settings.copy(autoStopSeconds = seconds))
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (settings.autoStopSeconds == seconds) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CAMERA SETTINGS CARD (THÔNG SỐ CAMERA)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF262626))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "THÔNG SỐ CAMERA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.ui.graphics.Color(0xFFFF7A00)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 1: Luôn sáng màn hình khi quay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Luôn sáng màn hình khi quay",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tắt để app tự giảm sáng khi quay, giúp tiết kiệm pin và giảm nóng máy.",
                            fontSize = 12.sp,
                            color = androidx.compose.ui.graphics.Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = settings.keepScreenAwake,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(keepScreenAwake = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item 2: Tự dừng (phút)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tự dừng (phút)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )

                    var showMinuteMenu by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { showMinuteMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF333333),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF555555))
                        ) {
                            Text(
                                text = "${settings.autoStopMinutes}",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showMinuteMenu,
                            onDismissRequest = { showMinuteMenu = false }
                        ) {
                            listOf(0, 1, 2, 3, 5, 10, 15, 30).forEach { mins ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(if (mins == 0) "0 (Không giới hạn)" else "$mins phút") },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(autoStopMinutes = mins))
                                        showMinuteMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item 3: Chất lượng video
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chất lượng video",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    var showVideoMenu by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { showVideoMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF333333),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF555555))
                        ) {
                            Text(
                                text = settings.videoQuality,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showVideoMenu,
                            onDismissRequest = { showVideoMenu = false }
                        ) {
                            listOf(
                                "1080p rất nét (nặng)",
                                "720p nét vừa (khuyên dùng)",
                                "480p tiết kiệm bộ nhớ"
                            ).forEach { q ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(videoQuality = q))
                                        showVideoMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item 4: Chất lượng ảnh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chất lượng ảnh",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    var showPhotoMenu by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { showPhotoMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            color = androidx.compose.ui.graphics.Color(0xFF333333),
                            border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF555555))
                        ) {
                            Text(
                                text = settings.photoQuality,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = showPhotoMenu,
                            onDismissRequest = { showPhotoMenu = false }
                        ) {
                            listOf(
                                "Gốc 100% (nặng)",
                                "Nén 80% vừa",
                                "Nén 50% nhẹ"
                            ).forEach { pq ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(pq) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(photoQuality = pq))
                                        showPhotoMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Operator Profile
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageUtils.getTranslation("settings_operator", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = settings.operatorName,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(operatorName = it))
                    },
                    label = { Text(if (lang == "en") "Staff Name / Shift Code" else "Tên nhân viên / Mã ca làm việc") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Cloud Storage Card (LƯU TRỮ ĐÁM MÂY)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LƯU TRỮ ĐÁM MÂY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Upload over Wi-Fi toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Upload qua Wi-Fi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = settings.uploadOverWifiOnly,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(uploadOverWifiOnly = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Account Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (settings.driveAccount != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = settings.driveAccount!!,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = {
                                    viewModel.updateSettings(settings.copy(driveAccount = null))
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Đăng xuất", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Chưa kết nối Google Drive",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = {
                                        showGoogleSignInDialog = true
                                    }
                                ) {
                                    Text("Kết nối Drive", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. System Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Haiphongapp Quay Video Đóng Hàng v2.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lang == "en") "Automated packing video recorder with timestamp overlay & Google Drive upload." else "Hỗ trợ tự động nhận diện mã QR/Barcode, chèn ngày giờ lên video và tải lên Google Drive.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showGoogleSignInDialog) {
            GoogleSignInDialog(
                onDismiss = { showGoogleSignInDialog = false },
                onAccountSelected = { email ->
                    viewModel.updateSettings(settings.copy(driveAccount = email))
                    viewModel.showToast("Đã kết nối Google Drive ($email) thành công!")
                }
            )
        }
    }
}

