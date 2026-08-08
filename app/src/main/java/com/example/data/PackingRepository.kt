package com.example.data

import kotlinx.coroutines.flow.Flow
import java.io.File

class PackingRepository(private val dao: PackingRecordDao) {

    val allRecords: Flow<List<PackingRecord>> = dao.getAllRecords()
    val recordCount: Flow<Int> = dao.getRecordCount()

    fun searchRecords(query: String): Flow<List<PackingRecord>> {
        return if (query.isBlank()) {
            dao.getAllRecords()
        } else {
            dao.searchRecords(query.trim())
        }
    }

    fun getRecordsByOrderCode(orderCode: String): Flow<List<PackingRecord>> {
        return dao.getRecordsByOrderCode(orderCode.trim())
    }

    suspend fun insertRecord(record: PackingRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun updateRecord(record: PackingRecord) {
        dao.updateRecord(record)
    }

    suspend fun deleteRecord(record: PackingRecord) {
        // Remove video file from disk if present
        try {
            val file = File(record.videoPath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dao.deleteRecord(record)
    }
}
