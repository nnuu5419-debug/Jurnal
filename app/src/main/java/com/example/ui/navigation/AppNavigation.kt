package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.AccessStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeHubScreen

object AppRoutes {
    const val HOME = "home"
    const val ATTENDANCE = "attendance"
    const val ANALYTICS = "analytics"
    const val GROUPS = "groups"
    const val ADMIN = "admin"
    const val HISTORY = "history"
    const val GROUP_DETAIL = "group_detail/{groupId}"

    fun groupDetail(groupId: Long): String = "group_detail/$groupId"
}

private data class TopNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedShell(
    currentUser: UserAccount,
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingCount = uiState.allUsers.count { it.status == AccessStatus.PENDING }
    val isAdmin = currentUser.role == UserRole.ADMIN

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val topItems = remember(isAdmin) {
        listOf(
            TopNavItem(
                route = AppRoutes.HOME,
                label = "Башкы",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home"
            ),
            TopNavItem(
                route = AppRoutes.ATTENDANCE,
                label = "Жоктоо",
                selectedIcon = Icons.AutoMirrored.Filled.FactCheck,
                unselectedIcon = Icons.AutoMirrored.Outlined.FactCheck,
                testTag = "nav_attendance"
            ),
            TopNavItem(
                route = AppRoutes.ANALYTICS,
                label = "Аналитика",
                selectedIcon = Icons.Filled.Analytics,
                unselectedIcon = Icons.Outlined.Analytics,
                testTag = "nav_analytics"
            ),
            TopNavItem(
                route = AppRoutes.GROUPS,
                label = "Группалар",
                selectedIcon = Icons.Filled.Groups,
                unselectedIcon = Icons.Outlined.Groups,
                testTag = "nav_groups"
            ),
            TopNavItem(
                route = AppRoutes.ADMIN,
                label = if (isAdmin) "Админ" else "Профиль",
                selectedIcon = if (isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                unselectedIcon = if (isAdmin) Icons.Outlined.AdminPanelSettings else Icons.Outlined.Person,
                testTag = "nav_admin"
            )
        )
    }

    fun navigateToTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val currentTitle = when (currentDestination?.route) {
        AppRoutes.HOME -> "Жалал-Абад колледжи"
        AppRoutes.ATTENDANCE -> "Жумалык жоктоо"
        AppRoutes.ANALYTICS -> "Аналитика жана проценттер"
        AppRoutes.GROUPS -> "Группалар"
        AppRoutes.ADMIN -> if (isAdmin) "Админ панель" else "Менин профилим"
        AppRoutes.HISTORY -> "Жумалык отчёттор"
        AppRoutes.GROUP_DETAIL -> "Группа маалыматы"
        else -> "Жалал-Абад колледжи"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Жалал-Абад колледжи",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 14.dp).size(26.dp)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTopTab(item.route) },
                                icon = {
                                    if (item.route == AppRoutes.ADMIN && isAdmin && pendingCount > 0) {
                                        BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        topItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTopTab(item.route) },
                                icon = {
                                    if (item.route == AppRoutes.ADMIN && isAdmin && pendingCount > 0) {
                                        BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    }
                                },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME
                    ) {
                        composable(AppRoutes.HOME) {
                            HomeHubScreen(
                                currentUser = currentUser,
                                pendingUsersCount = pendingCount,
                                weeklySummary = uiState.weeklySummary,
                                onOpenAttendance = { navigateToTopTab(AppRoutes.ATTENDANCE) },
                                onOpenAnalytics = { navigateToTopTab(AppRoutes.ANALYTICS) },
                                onOpenGroups = { navigateToTopTab(AppRoutes.GROUPS) },
                                onOpenHistory = { navController.navigate(AppRoutes.HISTORY) },
                                onOpenAdminPanel = { navigateToTopTab(AppRoutes.ADMIN) }
                            )
                        }

                        composable(AppRoutes.ATTENDANCE) {
                            AttendanceScreen(
                                accessibleGroups = uiState.accessibleGroups,
                                selectedGroupId = uiState.selectedGroupId,
                                allStudents = uiState.allStudents,
                                allAttendance = uiState.allAttendance,
                                selectedWeekStartIso = uiState.selectedWeekStartIso,
                                selectedDayOfWeek = uiState.selectedDayOfWeek,
                                weeklySummary = uiState.weeklySummary,
                                userRole = currentUser.role,
                                onSelectGroup = viewModel::selectGroup,
                                onShiftWeek = viewModel::shiftWeek,
                                onResetToCurrentWeek = viewModel::resetToCurrentWeek,
                                onSelectDay = viewModel::selectDay,
                                onMarkAttendance = viewModel::markStudentAttendance,
                                onMarkAllDay = viewModel::markAllStudentsForDay,
                                onClearDay = viewModel::clearDayAttendance,
                                onGoToGroups = { navigateToTopTab(AppRoutes.GROUPS) }
                            )
                        }

                        composable(AppRoutes.ANALYTICS) {
                            AnalyticsScreen(
                                weeklySummary = uiState.weeklySummary,
                                onShiftWeek = viewModel::shiftWeek,
                                onResetToCurrentWeek = viewModel::resetToCurrentWeek,
                                onOpenGroupAttendance = { groupId ->
                                    viewModel.selectGroup(groupId)
                                    navigateToTopTab(AppRoutes.ATTENDANCE)
                                }
                            )
                        }

                        composable(AppRoutes.GROUPS) {
                            GroupsScreen(
                                accessibleGroups = uiState.accessibleGroups,
                                weeklySummary = uiState.weeklySummary,
                                userRole = currentUser.role,
                                onCreateGroup = viewModel::createGroup,
                                onOpenGroupDetail = { groupId ->
                                    navController.navigate(AppRoutes.groupDetail(groupId))
                                },
                                onOpenGroupAttendance = { groupId ->
                                    viewModel.selectGroup(groupId)
                                    navigateToTopTab(AppRoutes.ATTENDANCE)
                                }
                            )
                        }

                        composable(
                            route = AppRoutes.GROUP_DETAIL,
                            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                        ) { entry ->
                            val groupId = entry.arguments?.getLong("groupId") ?: -1L
                            val group = uiState.allGroups.firstOrNull { it.id == groupId }
                            val students = uiState.allStudents.filter { it.groupId == groupId }
                            GroupDetailScreen(
                                group = group,
                                students = students,
                                userRole = currentUser.role,
                                onBack = { navController.popBackStack() },
                                onUpdateGroup = viewModel::updateGroup,
                                onDeleteGroup = viewModel::deleteGroup,
                                onAddStudent = viewModel::addStudent,
                                onRenameStudent = viewModel::renameStudent,
                                onRemoveStudent = viewModel::removeStudent,
                                onOpenAttendance = { targetId ->
                                    viewModel.selectGroup(targetId)
                                    navigateToTopTab(AppRoutes.ATTENDANCE)
                                }
                            )
                        }

                        composable(AppRoutes.ADMIN) {
                            AdminPanelScreen(
                                currentUser = currentUser,
                                allUsers = uiState.allUsers,
                                allGroups = uiState.allGroups,
                                weeklySummary = uiState.weeklySummary,
                                onApproveUser = viewModel::approveUser,
                                onRevokeUser = viewModel::revokeUser,
                                onSetUserRole = viewModel::setUserRole,
                                onAssignGroups = viewModel::assignGroupsToStarosta,
                                onDeleteUser = viewModel::deleteUser,
                                onSignOut = viewModel::signOut
                            )
                        }

                        composable(AppRoutes.HISTORY) {
                            HistoryScreen(
                                weeklySummary = uiState.weeklySummary,
                                onBack = { navController.popBackStack() },
                                onSelectWeekAndOpenAnalytics = { weekIso ->
                                    viewModel.selectWeek(weekIso)
                                    navigateToTopTab(AppRoutes.ANALYTICS)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
