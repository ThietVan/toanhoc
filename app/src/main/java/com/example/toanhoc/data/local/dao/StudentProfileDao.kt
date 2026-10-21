package com.example.toanhoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toanhoc.data.local.entity.StudentProfileEntity

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: StudentProfileEntity)
}
