package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<TaskWithSubtasks>> = taskDao.getAllTasks()

    suspend fun insertTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        taskDao.insertTaskWithSubtasks(task, subtasks)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        taskDao.updateSubtask(subtask)
    }

    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>) {
        taskDao.insertSubtasks(subtasks)
    }

    suspend fun updateTaskOrder(tasks: List<TaskEntity>) {
        taskDao.updateTasks(tasks)
    }
}
