package com.example.toanhoc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toanhoc.data.entity.UserKnowledgeEntity

@Dao
interface UserKnowledgeDao {
    @Query("SELECT * FROM user_knowledge WHERE grade = :grade")
    suspend fun getKnowledgeForGrade(grade: Int): List<UserKnowledgeEntity>

    @Query("SELECT * FROM user_knowledge WHERE kcId = :kcId LIMIT 1")
    suspend fun getKnowledgeByKcId(kcId: String): UserKnowledgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveKnowledge(entity: UserKnowledgeEntity)
}
