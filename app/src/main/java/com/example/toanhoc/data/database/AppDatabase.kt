package com.example.toanhoc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.toanhoc.data.dao.SessionLogDao
import com.example.toanhoc.data.dao.UserKnowledgeDao
import com.example.toanhoc.data.entity.SessionLogEntity
import com.example.toanhoc.data.entity.UserKnowledgeEntity

@Database(
    entities = [UserKnowledgeEntity::class, SessionLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userKnowledgeDao(): UserKnowledgeDao
    abstract fun sessionLogDao(): SessionLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toanhoc_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
