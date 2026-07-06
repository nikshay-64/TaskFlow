package com.example.ui.home

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskFlowViewModel
import com.example.ui.components.neumorphicFlat
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Yellow500
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: TaskFlowViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAllTasks: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val showConfetti by viewModel.showConfetti.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val activeTasks = tasks.filter { !it.task.isCompleted }
    val completedTasks = tasks.filter { it.task.isCompleted }

    val totalCount = tasks.size
    val completedCount = completedTasks.size
    val pendingCount = activeTasks.size
    val starredCount = tasks.count { it.task.isFavorite }
    val completionRate = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    // Confetti sound / trigger logic
    if (showConfetti) {
        val toneG = remember { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) }
        LaunchedEffect(Unit) {
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            delay(3000)
            viewModel.stopConfetti()
            toneG.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Header: Hamburger menu, notification bell, profile picture
            item {
                HomeTopBar(userName = userName)
            }

            // Stats Overview Card with Radial Gauge
            item {
                OverviewCard(
                    total = totalCount,
                    completed = completedCount,
                    pending = pendingCount,
                    starred = starredCount,
                    rate = completionRate
                )
            }

            // "Continue Working" Hero Card
            item {
                val continueTask = activeTasks.firstOrNull { it.subtasks.isNotEmpty() }
                ContinueWorkingCard(
                    taskWithSubtasks = continueTask,
                    onClick = {
                        if (continueTask != null) {
                            onNavigateToDetail(continueTask.task.id)
                        } else {
                            onNavigateToAllTasks()
                        }
                    }
                )
            }

            // Daily Quotes/Motivation Section
            item {
                QuoteSection()
            }

            // Recent Tasks List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        modifier = Modifier.clickable { onNavigateToAllTasks() }
                    )
                }
            }

            // Recent Task Cards (Only active tasks up to 5)
            val recentTasks = activeTasks.take(5)
            if (recentTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .neumorphicFlat(cornerRadius = 24.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = AccentBlue.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All tasks completed!",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Tap + below to add a new task.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(recentTasks, key = { it.task.id }) { taskWithSubtasks ->
                    HomeTaskListItem(
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
fun HomeTopBar(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, AccentBlue, CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))))
            ) {
                // Try to show App Logo if drawable exists, else fallback to text initials
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo_1783344552551),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Greetings
        val greeting = remember {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val dayOfWeek = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.SUNDAY -> "Sunday"
                java.util.Calendar.MONDAY -> "Monday"
                java.util.Calendar.TUESDAY -> "Tuesday"
                java.util.Calendar.WEDNESDAY -> "Wednesday"
                java.util.Calendar.THURSDAY -> "Thursday"
                java.util.Calendar.FRIDAY -> "Friday"
                java.util.Calendar.SATURDAY -> "Saturday"
                else -> ""
            }
            val timeGreeting = when (hour) {
                in 5..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..21 -> "Good Evening"
                else -> "Good Night"
            }
            if (dayOfWeek.isNotEmpty()) {
                "Happy $dayOfWeek! $timeGreeting,"
            } else {
                "$timeGreeting,"
            }
        }

        Text(
            text = greeting,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Text(
            text = "$userName 👋",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun OverviewCard(
    total: Int,
    completed: Int,
    pending: Int,
    starred: Int,
    rate: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .neumorphicFlat(cornerRadius = 24.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Statistics 2x2 grid
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCell(value = total.toString(), label = "Total Tasks", modifier = Modifier.weight(1f))
                        StatCell(value = completed.toString(), label = "Completed", modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCell(value = pending.toString(), label = "Pending", modifier = Modifier.weight(1f))
                        StatCell(value = starred.toString(), label = "Starred", modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Radial Completed wheel on the right
                Box(
                    modifier = Modifier.weight(0.7f),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedProgress by animateFloatAsState(targetValue = rate, label = "rate")
                    Canvas(modifier = Modifier.size(88.dp)) {
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.15f),
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
                            text = "${(rate * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Completion",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ContinueWorkingCard(
    taskWithSubtasks: TaskWithSubtasks?,
    onClick: () -> Unit
) {
    val cardTitle = taskWithSubtasks?.task?.title ?: "No active tasks"
    val subtasks = taskWithSubtasks?.subtasks ?: emptyList()
    val completedCount = subtasks.count { it.isCompleted }
    val progress = if (subtasks.isEmpty()) 0f else completedCount.toFloat() / subtasks.size
    val progressText = if (taskWithSubtasks != null) "$completedCount of ${subtasks.size} subtasks completed" else "Create or continue tracking your project"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .neumorphicFlat(cornerRadius = 24.dp)
            .background(
                brush = Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Briefcase icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Text section
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Working",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = cardTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = progressText,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (taskWithSubtasks != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action arrow pill button
            Box(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Continue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1D4ED8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteSection() {
    val quotes = listOf(
        "Make each day your masterpiece.",
        "Focus on being productive instead of busy.",
        "Your future is created by what you do today.",
        "Big journeys begin with small steps."
    )
    val quoteIndex = remember { (0..3).random() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .neumorphicFlat(cornerRadius = 16.dp, blurRadius = 8.dp, offset = (-2).dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "DAILY FOCUS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${quotes[quoteIndex]}\"",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun HomeTaskListItem(
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
            .padding(horizontal = 24.dp)
            .neumorphicFlat(cornerRadius = 20.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon box on the left
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

            // Title & Fraction Completed
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

            // Star icon toggle
            IconButton(
                onClick = { viewModel.toggleTaskFavorite(task) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = if (task.isFavorite) Yellow500 else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
