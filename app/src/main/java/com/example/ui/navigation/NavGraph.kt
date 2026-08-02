package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.exams.ExamsScreen
import com.example.ui.screens.exams.ExamsViewModel
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.focus.FocusViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.progress.ProgressViewModel
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.schedule.ScheduleViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.subjects.SubjectDetailScreen
import com.example.ui.screens.subjects.SubjectsScreen
import com.example.ui.screens.subjects.SubjectsViewModel

@Composable
fun NavGraph(
    homeViewModel: HomeViewModel,
    scheduleViewModel: ScheduleViewModel,
    subjectsViewModel: SubjectsViewModel,
    focusViewModel: FocusViewModel,
    progressViewModel: ProgressViewModel,
    examsViewModel: ExamsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = BottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (screen.icon != null) {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToFocus = { subId, chapId ->
                        if (subId != null) {
                            focusViewModel.selectSubjectAndChapter(subId, chapId)
                        }
                        navController.navigate(Screen.Focus.route)
                    },
                    onNavigateToExams = { navController.navigate(Screen.Exams.route) },
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                    onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) }
                )
            }

            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onNavigateToFocus = { subId, chapId ->
                        focusViewModel.selectSubjectAndChapter(subId, chapId)
                        navController.navigate(Screen.Focus.route)
                    }
                )
            }

            composable(Screen.Subjects.route) {
                SubjectsScreen(
                    viewModel = subjectsViewModel,
                    onSubjectClick = { subjectId ->
                        navController.navigate(Screen.SubjectDetail.createRoute(subjectId))
                    }
                )
            }

            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
                SubjectDetailScreen(
                    subjectId = subjectId,
                    viewModel = subjectsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Focus.route) {
                FocusScreen(viewModel = focusViewModel)
            }

            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = progressViewModel)
            }

            composable(Screen.Exams.route) {
                ExamsScreen(viewModel = examsViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
