package com.example.toanhoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val fullName: String = "Học sinh",
    val birthYear: Int = 2017,
    val selectedGrade: Int = 1,
    val currentAcademicTier: String = "TRUNG_BINH",
    val streakDays: Int = 1,
    val selfReliancePercentage: Int = 85,
    val totalFocusMinutesLast7Days: Int = 105
)
