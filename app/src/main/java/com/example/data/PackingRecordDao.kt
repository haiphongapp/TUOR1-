package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingRecordDao {

    @Query("SELECT * FROM packing_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PackingRecord>>

    @Query("SELECT * FROM packing_records WHERE orderCode LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecords(query: String): Flow<List<PackingRecord>>

    @Query("SELECT * FROM packing_records WHERE orderCode = :orderCode ORDER BY timestamp DESC")
    fun getRecordsByOrderCode(orderCode: String): Flow<List<PackingRecord>>

    @Query("SELECT * FROM packing_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): PackingRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PackingRecord): Long

    @Update
    suspend fun updateRecord(record: PackingRecord)

    @Delete
    suspend fun deleteRecord(record: PackingRecord)

    @Query("DELETE FROM packing_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM packing_records")
    fun getRecordCount(): Flow<Int>
}
