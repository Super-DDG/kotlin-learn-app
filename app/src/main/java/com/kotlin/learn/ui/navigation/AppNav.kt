package com.kotlin.learn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.progress.ProgressStore
import com.kotlin.learn.data.progress.UserProgress
import com.kotlin.learn.ui.screens.HomeScreen
import com.kotlin.learn.ui.screens.LessonListScreen
import com.kotlin.learn.ui.screens.PlaceholderScreen

object Routes {
    const val HOME = "home"
    const val MODULE = "module/{moduleId}"
    const val LESSON = "lesson/{lessonId}"
    fun module(moduleId: String) = "module/$moduleId"
    fun lesson(lessonId: String) = "lesson/$lessonId"
}

@Composable
fun AppNav(progressStore: ProgressStore) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { backStackEntry ->
            val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
            HomeScreen(
                progress = progress,
                onModuleClick = { module -> navController.navigate(Routes.module(module.id)) },
            )
        }
        composable(Routes.MODULE) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            val module = Curriculum.modules.firstOrNull { it.id == moduleId }
            val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
            if (module == null) {
                // TODO(任务 11): 替换为 NotFoundScreen()
                PlaceholderScreen(text = "module 不存在：$moduleId")
            } else {
                LessonListScreen(
                    module = module,
                    progress = progress,
                    onLessonClick = { lesson -> navController.navigate(Routes.lesson(lesson.id)) },
                )
            }
        }
        composable(Routes.LESSON) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            // 任务 11 替换为 LessonScreen
            PlaceholderScreen(text = "lesson: $lessonId")
        }
    }
}
