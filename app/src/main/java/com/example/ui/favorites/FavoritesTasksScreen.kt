package com.example.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Work
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

@Composable
fun FavoritesTasksScreen(viewModel: TaskFlowViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val favoriteTasks = tasks.filter { it.task.isFavorite && !it.task.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("favorites_screen")
    ) {
        Text(
            text = "Starred Tasks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        if (favoriteTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "No Favorites",
                        tint = Yellow500.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No starred tasks yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Star tasks on your dashboard to save them here!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(favoriteTasks, key = { it.task.id }) { taskWithSubtasks ->
                    FavoriteTaskCard(
                        taskWithSubtasks = taskWithSubtasks,
                        onUnfavorite = {
                            viewModel.toggleTaskFavorite(taskWithSubtasks.task)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteTaskCard(taskWithSubtasks: TaskWithSubtasks, onUnfavorite: () -> Unit) {
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Category Icon Box
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

            // Title Column
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
            Spacer(modifier = Modifier.width(12.dp))

            // Unstar button
            IconButton(onClick = onUnfavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Unstar",
                    tint = Yellow500
                )
            }
        }
    }
}
