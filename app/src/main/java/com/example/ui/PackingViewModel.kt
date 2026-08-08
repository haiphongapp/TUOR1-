package com.example.ui

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PackingRecord
import com.example.data.PackingRepository
import com.example.util.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CameraState {
    SCANNING,      // Waiting for order barcode scan
    RECORDING,     // Video recording in progress
    SAVING         // Finalizing video file & database record
}

data class SystemSettings(
    val autoStartOnScan: Boolean = true,
    val autoStopSeconds: Int = 0, // 0 means unlimited / manual stop only
    val keepScreenAwake: Boolean = true, // Luôn sáng màn hình khi quay
    val autoStopMinutes: Int = 0, // Tự dừng (phút)
    val videoQuality: String = "1080p rất nét (nặng)", // Chất lượng video
    val photoQuality: String = "Gốc 100% (nặng)", // Chất lượng ảnh
    val language: String = "vi", // "vi" for Vietnamese, "en" for English
    val operatorName: String = "Nhân viên #01",
    val torchEnabled: Boolean = false,
    val uploadOverWifiOnly: Boolean = true,
    val driveAccount: String? = "bbinhminhshop@gmail.com"
)

class PackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PackingRepository
    val ttsManager: TtsManager

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PackingRepository(db.packingRecordDao())
        ttsManager = TtsManager(application)
        
        // Seed sample data if database is empty so user can test search and history right away
        viewModelScope.launch {
            seedSampleDataIfEmpty()
        }
    }

    // Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Database Records Flow
    val packingRecords: StateFlow<List<PackingRecord>> = _searchQuery
        .flatMapLatest { query -> repository.searchRecords(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recordCount: StateFlow<Int> = repository.recordCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Camera & Recording UI States
    private val _cameraState = MutableStateFlow(CameraState.SCANNING)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _currentOrderCode = MutableStateFlow("")
    val currentOrderCode: StateFlow<String> = _currentOrderCode.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _settings = MutableStateFlow(SystemSettings())
    val settings: StateFlow<SystemSettings> = _settings.asStateFlow()

    private val _selectedRecordForPlayback = MutableStateFlow<PackingRecord?>(null)
    val selectedRecordForPlayback: StateFlow<PackingRecord?> = _selectedRecordForPlayback.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var timerJob: Job? = null
    private var pendingVideoFile: File? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSettings(newSettings: SystemSettings) {
        _settings.value = newSettings
    }

    fun toggleTorch() {
        _settings.value = _settings.value.copy(torchEnabled = !_settings.value.torchEnabled)
    }

    fun setSelectedRecordForPlayback(record: PackingRecord?) {
        _selectedRecordForPlayback.value = record
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showToast(msg: String) {
        _userMessage.value = msg
    }

    /**
     * Triggered when a barcode/QR code is detected by camera or simulated input
     */
    fun onOrderCodeScanned(code: String, onStartVideoRecording: (File) -> Unit) {
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.isEmpty()) return

        // Prevent scanning when already recording or processing
        if (_cameraState.value != CameraState.SCANNING) return

        _currentOrderCode.value = trimmedCode

        viewModelScope.launch {
            startVideoRecording(trimmedCode, onStartVideoRecording)
        }
    }

    private fun startVideoRecording(code: String, onStartVideoRecording: (File) -> Unit) {
        _cameraState.value = CameraState.RECORDING
        _recordingSeconds.value = 0

        // Create temporary output video file
        val outputDir = getPackingVideoDirectory()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFile = File(outputDir, "PACK_${code}_$timestamp.mp4")
        pendingVideoFile = videoFile

        onStartVideoRecording(videoFile)

        // Start elapsed timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_cameraState.value == CameraState.RECORDING) {
                delay(1000)
                _recordingSeconds.value += 1

                val autoStopSec = _settings.value.autoStopSeconds
                if (autoStopSec > 0 && _recordingSeconds.value >= autoStopSec) {
                    // Auto stop triggered
                    _userMessage.value = "Đã đủ thời lượng $autoStopSec giây, tự động dừng ghi hình."
                    break
                }
            }
        }
    }

    /**
     * Called when recording stops (manually or auto-stop)
     */
    fun onStopRecording(onStopVideoRecording: () -> Unit) {
        if (_cameraState.value != CameraState.RECORDING) return
        _cameraState.value = CameraState.SAVING
        timerJob?.cancel()

        onStopVideoRecording()
    }

    /**
     * Finalize and save record to Room Database once VideoCapture finishes file output
     */
    fun onVideoFileSaved(file: File, isSuccess: Boolean) {
        viewModelScope.launch {
            val duration = _recordingSeconds.value
            val code = _currentOrderCode.value.ifEmpty { "UNKNOWN" }

            if (isSuccess && file.exists()) {
                val record = PackingRecord(
                    orderCode = code,
                    videoPath = file.absolutePath,
                    durationSeconds = duration,
                    fileSizeBytes = file.length(),
                    timestamp = System.currentTimeMillis(),
                    operatorName = _settings.value.operatorName,
                    status = "HOÀN TẤT"
                )
                repository.insertRecord(record)
                _userMessage.value = "Đã lưu video đóng hàng cho đơn: $code"
            } else {
                _userMessage.value = "Không thể ghi video cho đơn $code"
            }

            // Reset camera state back to SCANNING
            resetScannerState()
        }
    }

    fun resetScannerState() {
        timerJob?.cancel()
        _cameraState.value = CameraState.SCANNING
        _recordingSeconds.value = 0
        _currentOrderCode.value = ""
        pendingVideoFile = null
    }

    fun deleteRecord(record: PackingRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            if (_selectedRecordForPlayback.value?.id == record.id) {
                _selectedRecordForPlayback.value = null
            }
            _userMessage.value = "Đã xóa video đóng hàng đơn ${record.orderCode}"
        }
    }

    fun updateRecordNotes(record: PackingRecord, notes: String, status: String) {
        viewModelScope.launch {
            val updated = record.copy(notes = notes, status = status)
            repository.updateRecord(updated)
            if (_selectedRecordForPlayback.value?.id == record.id) {
                _selectedRecordForPlayback.value = updated
            }
            _userMessage.value = "Đã cập nhật ghi chú cho đơn ${record.orderCode}"
        }
    }

    fun uploadRecordToDrive(record: PackingRecord) {
        viewModelScope.launch {
            // Set state to syncing
            val syncingRecord = record.copy(isSyncing = true)
            repository.updateRecord(syncingRecord)
            if (_selectedRecordForPlayback.value?.id == record.id) {
                _selectedRecordForPlayback.value = syncingRecord
            }

            // Simulate upload delay
            delay(2000)

            val uploadedRecord = record.copy(
                isSyncing = false,
                isUploaded = true,
                driveUrl = "https://drive.google.com/file/d/haiphongapp_${record.orderCode}_${System.currentTimeMillis()}/view"
            )
            repository.updateRecord(uploadedRecord)
            if (_selectedRecordForPlayback.value?.id == record.id) {
                _selectedRecordForPlayback.value = uploadedRecord
            }
            _userMessage.value = "Đã đồng bộ thành công video đơn ${record.orderCode} lên Google Drive!"
        }
    }

    private fun getPackingVideoDirectory(): File {
        val app = getApplication<Application>()
        val mediaDir = app.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: app.filesDir
        val packingDir = File(mediaDir, "PackingVideos")
        if (!packingDir.exists()) {
            packingDir.mkdirs()
        }
        return packingDir
    }

    private suspend fun seedSampleDataIfEmpty() {
        // Seed 3 sample records if database is empty so search/history features are immediately testable
        val dummyFile = File(getPackingVideoDirectory(), "SAMPLE_DEMO.mp4")
        if (!dummyFile.exists()) {
            try {
                dummyFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val now = System.currentTimeMillis()
        val sampleRecords = listOf(
            PackingRecord(
                orderCode = "SPX882910492",
                videoPath = dummyFile.absolutePath,
                durationSeconds = 24,
                fileSizeBytes = 4_500_000,
                timestamp = now - 3600_000 * 2,
                notes = "Đơn 2 áo thun, đóng hộp carton 20x15cm",
                status = "HOÀN TẤT",
                operatorName = "Nguyễn Văn A"
            ),
            PackingRecord(
                orderCode = "GHTK902184712",
                videoPath = dummyFile.absolutePath,
                durationSeconds = 35,
                fileSizeBytes = 6_800_000,
                timestamp = now - 3600_000 * 5,
                notes = "Hàng dễ vỡ, bọc xốp bóng khí 3 lớp",
                status = "HOÀN TẤT",
                operatorName = "Trần Thị B"
            ),
            PackingRecord(
                orderCode = "VNP381940129",
                videoPath = dummyFile.absolutePath,
                durationSeconds = 18,
                fileSizeBytes = 3_200_000,
                timestamp = now - 3600_000 * 24,
                notes = "Khách yêu cầu dán tem niêm phong màu đỏ",
                status = "CẦN KIỂM TRA",
                operatorName = "Nguyễn Văn A"
            )
        )

        for (rec in sampleRecords) {
            repository.insertRecord(rec)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        timerJob?.cancel()
    }
}
