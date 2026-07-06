package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskFlowDatabase
import com.example.data.TaskRepository
import com.example.data.TaskWithSubtasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val prefsManager = PreferencesManager(application)

    init {
        val db = TaskFlowDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showConfetti = MutableStateFlow(false)
    val showConfetti: StateFlow<Boolean> = _showConfetti.asStateFlow()

    val userName: StateFlow<String> = prefsManager.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "User"
        )

    val themeMode: StateFlow<String> = prefsManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Light"
        )

    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            prefsManager.setThemeMode(theme)
        }
    }

    fun setOnboardingCompleted(name: String) {
        viewModelScope.launch {
            prefsManager.setOnboardingCompleted(name)
        }
    }

    val allTasks: StateFlow<List<TaskWithSubtasks>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTasks: StateFlow<List<TaskWithSubtasks>> = combine(allTasks, _searchQuery) { tasks, query ->
        if (query.isBlank()) {
            tasks
        } else {
            tasks.filter { 
                it.task.title.contains(query, ignoreCase = true) || 
                it.task.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(
        title: String,
        description: String,
        subtasks: List<String>,
        priority: String = "Medium",
        colorIndex: Int = 0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                priority = priority,
                colorIndex = colorIndex,
                notes = notes
            )
            val subtaskEntities = subtasks.map { SubtaskEntity(taskId = 0, title = it) }
            repository.insertTaskWithSubtasks(task, subtaskEntities)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskPin(task: TaskEntity) {
        updateTask(task.copy(isPinned = !task.isPinned))
    }

    fun toggleTaskFavorite(task: TaskEntity) {
        updateTask(task.copy(isFavorite = !task.isFavorite))
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            repository.insertSubtasks(listOf(SubtaskEntity(taskId = taskId, title = title)))
        }
    }

    fun toggleSubtaskCompletion(subtask: SubtaskEntity, taskWithSubtasks: TaskWithSubtasks) {
        viewModelScope.launch {
            val updatedSubtask = subtask.copy(isCompleted = !subtask.isCompleted)
            repository.updateSubtask(updatedSubtask)
            
            // Check if all subtasks are completed after this update
            val newSubtasks = taskWithSubtasks.subtasks.map { 
                if (it.id == subtask.id) updatedSubtask else it 
            }
            if (newSubtasks.isNotEmpty() && newSubtasks.all { it.isCompleted }) {
                triggerConfetti()
                updateTask(taskWithSubtasks.task.copy(isCompleted = true))
            }
        }
    }

    fun triggerConfetti() {
        _showConfetti.value = true
    }

    fun stopConfetti() {
        _showConfetti.value = false
    }

    fun reorderTasks(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            repository.updateTaskOrder(tasks.mapIndexed { index, task -> task.copy(sortOrder = index) })
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskFlowViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskFlowViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
