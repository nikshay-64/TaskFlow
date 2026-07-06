package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val priority: String = "Medium",
    val colorIndex: Int = 0,
    val notes: String = ""
)
