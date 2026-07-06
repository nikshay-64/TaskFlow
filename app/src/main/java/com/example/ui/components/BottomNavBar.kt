package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToCompleted: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("bottom_nav_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = currentRoute == "home" || currentRoute == "all_tasks" || currentRoute.startsWith("task_detail"),
            onClick = onNavigateToHome,
            modifier = Modifier.testTag("nav_home_button").weight(1f)
        )
        NavBarItem(
            icon = Icons.Default.CheckCircle,
            label = "Done",
            isSelected = currentRoute == "completed",
            onClick = onNavigateToCompleted,
            modifier = Modifier.testTag("nav_completed_button").weight(1f)
        )
        
        // Add Button
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, spotColor = AccentBlue)
                .background(AccentBlue, CircleShape)
                .clickable(onClick = onNavigateToAdd)
                .testTag("add_task_fab"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        
        NavBarItem(
            icon = Icons.Default.Star,
            label = "Starred",
            isSelected = currentRoute == "favorites",
            onClick = onNavigateToFavorites,
            modifier = Modifier.testTag("nav_favorites_button").weight(1f)
        )
        NavBarItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = currentRoute == "settings",
            onClick = onNavigateToSettings,
            modifier = Modifier.testTag("nav_settings_button").weight(1f)
        )
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
