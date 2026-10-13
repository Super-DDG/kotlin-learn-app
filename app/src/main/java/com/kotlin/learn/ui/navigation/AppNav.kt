package com.kotlin.learn.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.progress.ProgressStore
import com.kotlin.learn.data.progress.UserProgress
import com.kotlin.learn.ui.demos.DemoCatalog
import com.kotlin.learn.ui.demos.DemoItem
import com.kotlin.learn.ui.demos.DemoListScreen
import com.kotlin.learn.ui.screens.HomeScreen
import com.kotlin.learn.ui.screens.LessonListScreen
import com.kotlin.learn.ui.screens.LessonScreen
import com.kotlin.learn.ui.screens.NotFoundScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val MODULE = "module/{moduleId}"
    const val LESSON = "lesson/{lessonId}"
    const val DEMO_LAND = "demo-land"
    const val DEMO = "demo/{demoId}"
    fun module(moduleId: String) = "module/$moduleId"
    fun lesson(lessonId: String) = "lesson/$lessonId"
    fun demo(demoId: String) = "demo/$demoId"
}

@Composable
fun AppNav(progressStore: ProgressStore) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { backStackEntry ->
            val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
            HomeScreen(
                progress = progress,
                onModuleClick = { module -> navController.navigate(Routes.module(module.id)) },
                onDemoLandClick = { navController.navigate(Routes.DEMO_LAND) },
            )
        }
        composable(Routes.MODULE) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            val module = Curriculum.modules.firstOrNull { it.id == moduleId }
            val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
            if (module == null) {
                NotFoundScreen()
            } else {
                LessonListScreen(
                    module = module,
                    progress = progress,
                    onLessonClick = { lesson -> navController.navigate(Routes.lesson(lesson.id)) },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Routes.LESSON) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val lesson = Curriculum.findLesson(lessonId.orEmpty())
            if (lesson == null) {
                NotFoundScreen()
            } else {
                LessonScreen(
                    lesson = lesson,
                    onSubmitQuiz = { correct, total ->
                        // 交卷即记完成；写库放在协程里
                        scope.launch { progressStore.recordQuizResult(lessonId!!, correct, total) }
                    },
                )
            }
        }
        composable(Routes.DEMO_LAND) {
            DemoListScreen(
                onDemoClick = { demo -> navController.navigate(Routes.demo(demo.id)) },
                onBack = { navController.popBackStack() },
            )
        }
        // 临时编译桥：CounterDemo/TodoDemo/AnimationDemo 由任务 3/4/5 创建，
        // 届时逐个替换 Placeholder 分支；任务 6 收尾时删除 Placeholder 与本说明。
        composable(Routes.DEMO) { backStackEntry ->
            val demoId = backStackEntry.arguments?.getString("demoId")
            val item = DemoCatalog.find(demoId.orEmpty())
            if (item == null) NotFoundScreen() else DemoListPlaceholder(item)
        }
    }
}

@Composable
private fun DemoListPlaceholder(item: DemoItem) {
    Text("${item.emoji} ${item.title}")
}
