package com.example.toanhoc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.toanhoc.data.local.dao.MathAcademicDao
import com.example.toanhoc.data.local.dao.StudentProfileDao
import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import com.example.toanhoc.data.local.entity.StudentProfileEntity

@Database(
    entities = [
        KnowledgeNodeEntity::class,
        PrerequisiteEdgeEntity::class,
        StudentMasteryEntity::class,
        MisconceptionLogEntity::class,
        DailySessionLogEntity::class,
        StudentProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mathAcademicDao(): MathAcademicDao
    abstract fun studentProfileDao(): StudentProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toanhoc_academic_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
