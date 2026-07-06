package com.example.ui.taskdetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubtaskEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskFlowViewModel
import com.example.ui.components.neumorphicFlat
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Yellow500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    viewModel: TaskFlowViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val taskWithSubtasks = tasks.firstOrNull { it.task.id == taskId }

    var showAddSubtaskDialog by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }

    if (taskWithSubtasks == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Task not found", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val completedCount = subtasks.count { it.isCompleted }
    val progress = if (subtasks.isEmpty()) 0f else completedCount.toFloat() / subtasks.size

    // Color-coded details based on task.colorIndex
    val cardBgGradient = when (task.colorIndex) {
        0 -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        1 -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
        2 -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        3 -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
        4 -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D)))
        else -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTaskPin(task) }) {
                        Icon(
                            imageVector = if (task.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (task.isPinned) AccentBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleTaskFavorite(task) }) {
                        Icon(
                            imageVector = if (task.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (task.isFavorite) Yellow500 else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.deleteTask(task)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.testTag("task_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Hero Card Briefcase Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicFlat(cornerRadius = 24.dp)
                        .background(cardBgGradient, shape = RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Priority: ${task.priority}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Progress Radial Ring Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicFlat(cornerRadius = 24.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount of ${subtasks.size} subtasks completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Circular Progress Indicator with custom Canvas
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
                            Canvas(modifier = Modifier.size(70.dp)) {
                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.2f),
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = AccentBlue,
                                    startAngle = -90f,
                                    sweepAngle = animatedProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // Subtasks checklist
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Subtasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(AccentBlue.copy(alpha = 0.1f), shape = CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$completedCount/${subtasks.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                    IconButton(
                        onClick = { showAddSubtaskDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentBlue.copy(alpha = 0.1f), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Subtask", tint = AccentBlue)
                    }
                }
            }

            if (subtasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subtasks created for this task.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(subtasks, key = { it.id }) { subtask ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicFlat(cornerRadius = 16.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                            .clickable { viewModel.toggleSubtaskCompletion(subtask, taskWithSubtasks) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Checkbox circle style
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 2.dp,
                                    color = if (subtask.isCompleted) Emerald500 else AccentBlue.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .background(
                                    color = if (subtask.isCompleted) Emerald500 else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (subtask.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = subtask.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (subtask.isCompleted) 0.5f else 1f),
                            textDecoration = if (subtask.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Details card (Priority, Dates, Notes)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicFlat(cornerRadius = 24.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Priority",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val dotColor = when (task.priority.lowercase()) {
                                    "high" -> Color.Red
                                    "medium" -> Color.Yellow
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(dotColor, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.priority,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        if (task.notes.isNotBlank()) {
                            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                            Column {
                                Text(
                                    text = "Notes",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.notes,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSubtaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubtaskDialog = false },
            title = { Text("Add Subtask", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    placeholder = { Text("Enter subtask title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubtaskTitle.isNotBlank()) {
                            viewModel.addSubtask(task.id, newSubtaskTitle.trim())
                            newSubtaskTitle = ""
                            showAddSubtaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubtaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
