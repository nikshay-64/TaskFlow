package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isPinned DESC, sortOrder ASC")
    fun getAllTasks(): Flow<List<TaskWithSubtasks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Update
    suspend fun updateTasks(tasks: List<TaskEntity>)

    @Transaction
    suspend fun insertTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        val taskId = insertTask(task)
        val newSubtasks = subtasks.map { it.copy(taskId = taskId) }
        insertSubtasks(newSubtasks)
    }
}
