package com.example.toanhoc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.toanhoc.data.entity.SessionLogEntity

@Dao
interface SessionLogDao {
    @Insert
    suspend fun insertLog(log: SessionLogEntity)

    @Query("SELECT * FROM session_log WHERE grade = :grade ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentLogsForGrade(grade: Int): List<SessionLogEntity>
}
