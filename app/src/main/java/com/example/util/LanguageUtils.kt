package com.example.util

object LanguageUtils {

    fun getTranslation(key: String, language: String): String {
        val isEn = language.lowercase() == "en"
        return when (key) {
            // Navigation
            "nav_home" -> if (isEn) "Home" else "Trang Chủ"
            "nav_record" -> if (isEn) "Record" else "Ghi Hình"
            "nav_history" -> if (isEn) "History" else "Tra Cứu"
            "nav_settings" -> if (isEn) "Settings" else "Cài Đặt"

            // Home Header & Stats
            "home_title" -> if (isEn) "PACKING VIDEO RECORDING" else "QUAY VIDEO ĐÓNG HÀNG"
            "filter_today" -> if (isEn) "TODAY" else "HÔM NAY"
            "filter_yesterday" -> if (isEn) "YESTERDAY" else "HÔM QUA"
            "filter_range" -> if (isEn) "DATE RANGE" else "KHOẢNG NGÀY"
            "stat_sent" -> if (isEn) "SENT ORDERS" else "ĐƠN GỬI ĐI"
            "stat_returned" -> if (isEn) "RETURNED" else "HÀNG HOÀN"

            // Action Tiles
            "action_record_video" -> if (isEn) "RECORD PACKING\nVIDEO" else "QUAY VIDEO\nĐÓNG HÀNG"
            "action_take_photo" -> if (isEn) "ORDER SNAPSHOT\nPHOTO" else "CHỤP ẢNH\nĐƠN HÀNG"
            "action_history" -> if (isEn) "SEARCH\nHISTORY" else "TRA CỨU\nLỊCH SỬ"
            "action_settings" -> if (isEn) "SYSTEM\nSETTINGS" else "CÀI ĐẶT\nHỆ THỐNG"

            // Settings
            "settings_title" -> if (isEn) "Packing Settings" else "Cài Đặt Đóng Gói"
            "settings_subtitle" -> if (isEn) "Customize recording limits, language and operator details" else "Tùy chỉnh giới hạn quay, ngôn ngữ và thông tin ca làm việc"
            "settings_lang" -> if (isEn) "App Language / Ngôn ngữ" else "Ngôn ngữ ứng dụng / Language"
            "settings_auto_start" -> if (isEn) "Auto Start on Scan" else "Tự động kích hoạt khi quét mã"
            "settings_auto_start_desc" -> if (isEn) "Start recording automatically right after barcode scan" else "Tự động quay video ngay sau khi quét"
            "settings_timer" -> if (isEn) "Recording Duration Limit" else "Thời gian tự động dừng quay"
            "settings_timer_desc" -> if (isEn) "Set to unlimited (manual stop) or auto-stop limit" else "Quay tối đa không giới hạn hoặc chọn thời lượng dừng tự động"
            "settings_operator" -> if (isEn) "Packing Staff Info" else "Thông tin nhân viên đóng gói"

            // Recording Screen
            "rec_scan_prompt" -> if (isEn) "ALIGN BARCODE / QR IN FRAME" else "HÃY ĐƯA MÃ VẠCH / QR VÀO KHUNG CÂU"
            "rec_recording_status" -> if (isEn) "RECORDING PACKING VIDEO" else "ĐANG GHI HÌNH ĐÓNG HÀNG"
            "rec_order_code" -> if (isEn) "Order Code" else "Mã đơn"
            "rec_stop_btn" -> if (isEn) "FINISH / STOP RECORDING" else "HOÀN THÀNH / DỪNG QUAY"
            "rec_sim_btn" -> if (isEn) "Simulate Barcode Scan" else "Giả Lập Quét Mã"

            // Drive Upload
            "drive_upload" -> if (isEn) "Upload to Google Drive" else "Tải Lên Google Drive"
            "drive_uploading" -> if (isEn) "Uploading video to Google Drive..." else "Đang tải video lên Google Drive..."
            "drive_success" -> if (isEn) "Uploaded to Google Drive successfully!" else "Đã tải video lên Google Drive thành công!"
            "drive_copy_link" -> if (isEn) "COPY DRIVE LINK" else "SAO CHÉP LINK DRIVE"

            else -> key
        }
    }
}
