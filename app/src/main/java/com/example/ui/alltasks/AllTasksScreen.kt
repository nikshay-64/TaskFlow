package com.example.ui.alltasks

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskFlowViewModel
import com.example.ui.components.neumorphicFlat
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.Yellow500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    viewModel: TaskFlowViewModel,
    onNavigateToDetail: (Long) -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pinned", "Starred", "Completed")

    val displayedTasks = when (selectedFilter) {
        "Pinned" -> tasks.filter { it.task.isPinned && !it.task.isCompleted }
        "Starred" -> tasks.filter { it.task.isFavorite && !it.task.isCompleted }
        "Completed" -> tasks.filter { it.task.isCompleted }
        else -> tasks.filter { !it.task.isCompleted } // Show all active tasks by default
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("all_tasks_screen")
    ) {
        // Custom Top Bar with Search & Filter Icons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }

        // Custom Styled Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .neumorphicFlat(cornerRadius = 16.dp, blurRadius = 8.dp, offset = (-2).dp)
                .testTag("search_bar"),
            placeholder = { Text("Search tasks...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        // Horizontal filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .neumorphicFlat(
                            cornerRadius = 14.dp,
                            blurRadius = if (isSelected) 2.dp else 6.dp,
                            offset = if (isSelected) (-1).dp else 3.dp
                        )
                        .background(
                            color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // List of filtered tasks
        if (displayedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tasks found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(displayedTasks, key = { it.task.id }) { taskWithSubtasks ->
                    AllTaskListItem(
                        taskWithSubtasks = taskWithSubtasks,
                        viewModel = viewModel,
                        onClick = { onNavigateToDetail(taskWithSubtasks.task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AllTaskListItem(
    taskWithSubtasks: TaskWithSubtasks,
    viewModel: TaskFlowViewModel,
    onClick: () -> Unit
) {
    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val completedCount = subtasks.count { it.isCompleted }
    val progress = if (subtasks.isEmpty()) 0f else completedCount.toFloat() / subtasks.size

    val iconBgColor = when (task.colorIndex) {
        0 -> Color(0xFF3B82F6).copy(alpha = 0.15f)
        1 -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
        2 -> Color(0xFF10B981).copy(alpha = 0.15f)
        3 -> Color(0xFFF59E0B).copy(alpha = 0.15f)
        4 -> Color(0xFFEC4899).copy(alpha = 0.15f)
        else -> Color(0xFF3B82F6).copy(alpha = 0.15f)
    }
    val iconColor = when (task.colorIndex) {
        0 -> Color(0xFF2563EB)
        1 -> Color(0xFF7C3AED)
        2 -> Color(0xFF059669)
        3 -> Color(0xFFD97706)
        4 -> Color(0xFFDB2777)
        else -> Color(0xFF2563EB)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphicFlat(cornerRadius = 20.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Custom Category Icon Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgColor, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completedCount / ${subtasks.size} subtasks",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = iconColor,
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Pin & Favorite controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleTaskPin(task) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin",
                        tint = if (task.isPinned) AccentBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleTaskFavorite(task) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = if (task.isFavorite) Yellow500 else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
