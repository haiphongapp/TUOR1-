package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packing_records")
data class PackingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderCode: String,
    val videoPath: String,
    val durationSeconds: Int,
    val fileSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val status: String = "HOÀN TẤT", // "HOÀN TẤT", "CẦN KIỂM TRA"
    val operatorName: String = "Nhân viên đóng gói",
    val isUploaded: Boolean = false,
    val isSyncing: Boolean = false,
    val driveUrl: String? = null
)
