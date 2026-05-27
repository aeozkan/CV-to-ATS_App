package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AtsReportDao {
    @Query("SELECT * FROM ats_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<AtsReportEntity>>

    @Insert
    suspend fun insertReport(report: AtsReportEntity): Long

    @Query("DELETE FROM ats_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM ats_reports")
    suspend fun deleteAllReports()
}
