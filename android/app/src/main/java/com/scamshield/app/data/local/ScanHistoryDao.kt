package com.scamshield.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(record: ScanRecordEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_history WHERE isThreat = 1 ORDER BY timestamp DESC")
    fun getThreatsOnly(): Flow<List<ScanRecordEntity>>

    @Query("SELECT COUNT(*) FROM scan_history")
    fun getTotalScansCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_history WHERE isThreat = 1")
    fun getThreatsCount(): Flow<Int>

    @Query("SELECT * FROM scan_history WHERE isThreat = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getRecentThreat(): Flow<ScanRecordEntity?>

    @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
    fun getScanById(id: Long): Flow<ScanRecordEntity?>

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}
