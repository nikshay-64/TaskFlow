package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, SubtaskEntity::class], version = 2, exportSchema = false)
abstract class TaskFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var Instance: TaskFlowDatabase? = null

        fun getDatabase(context: Context): TaskFlowDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TaskFlowDatabase::class.java, "taskflow_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
