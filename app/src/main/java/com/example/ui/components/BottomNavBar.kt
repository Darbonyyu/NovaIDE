package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class NavTab(
    val route: String,
    val labelRu: String,
    val labelEn: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Chat : NavTab("chat", "Чат", "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline)
    object Projects : NavTab("projects", "Проекты", "Projects", Icons.Filled.Folder, Icons.Outlined.FolderOpen)
    object History : NavTab("history", "История", "History", Icons.Filled.History, Icons.Outlined.History)
    object Api : NavTab("api", "API", "API", Icons.Filled.Key, Icons.Outlined.Key)
    object Settings : NavTab("settings", "Настройки", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val ALL_TABS = listOf(
    NavTab.Chat,
    NavTab.Projects,
    NavTab.History,
    NavTab.Api,
    NavTab.Settings
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    language: String = "RU",
    onTabSelected: (NavTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        ALL_TABS.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                        contentDescription = tab.labelRu
                    )
                },
                label = {
                    Text(
                        text = if (language == "RU") tab.labelRu else tab.labelEn,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
