package com.kotlin.learn.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import com.kotlin.learn.ui.demos.AnimationDemo
import com.kotlin.learn.ui.demos.ControlsDemo
import com.kotlin.learn.ui.demos.CounterDemo
import com.kotlin.learn.ui.demos.DialogDemo
import com.kotlin.learn.ui.demos.DemoCatalog
import com.kotlin.learn.ui.demos.DemoListScreen
import com.kotlin.learn.ui.demos.LoginFormDemo
import com.kotlin.learn.ui.demos.ScaffoldDemo
import com.kotlin.learn.ui.demos.TodoDemo
import com.kotlin.learn.ui.screens.HomeScreen
import com.kotlin.learn.ui.screens.LessonListScreen
import com.kotlin.learn.ui.screens.LessonScreen
import com.kotlin.learn.ui.screens.NotFoundScreen
import com.kotlin.learn.ui.screens.PlaygroundScreen
import com.kotlin.learn.ui.screens.QuizScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val MODULE = "module/{moduleId}"
    const val LESSON = "lesson/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val PLAYGROUND = "playground/{lessonId}/{exampleIndex}"
    const val DEMO_LAND = "demo-land"
    const val DEMO = "demo/{demoId}"
    fun module(moduleId: String) = "module/$moduleId"
    fun lesson(lessonId: String) = "lesson/$lessonId"
    fun quiz(lessonId: String) = "quiz/$lessonId"
    fun playground(lessonId: String, exampleIndex: Int) = "playground/$lessonId/$exampleIndex"
    fun demo(demoId: String) = "demo/$demoId"
}

@Composable
fun AppNav(progressStore: ProgressStore) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 } },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) },
    ) {
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
                    onQuizClick = { navController.navigate(Routes.quiz(lesson.id)) },
                    onPlaygroundClick = { index -> navController.navigate(Routes.playground(lesson.id, index)) },
                    onDemoClick = { demoId -> navController.navigate(Routes.demo(demoId)) },
                )
            }
        }
        composable(Routes.QUIZ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val lesson = Curriculum.findLesson(lessonId.orEmpty())
            if (lesson == null) {
                NotFoundScreen()
            } else {
                QuizScreen(
                    lesson = lesson,
                    onBack = { navController.popBackStack() },
                    onSubmitQuiz = { correct, total ->
                        scope.launch { progressStore.recordQuizResult(lessonId!!, correct, total) }
                    },
                )
            }
        }
        composable(Routes.PLAYGROUND) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val index = backStackEntry.arguments?.getString("exampleIndex")?.toIntOrNull()
            val example = Curriculum.findLesson(lessonId.orEmpty())?.codeExamples?.getOrNull(index ?: -1)
            if (example == null) {
                NotFoundScreen()
            } else {
                PlaygroundScreen(
                    code = example.code,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Routes.DEMO_LAND) {
            DemoListScreen(
                onDemoClick = { demo -> navController.navigate(Routes.demo(demo.id)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.DEMO) { backStackEntry ->
            val demoId = backStackEntry.arguments?.getString("demoId")
            val item = DemoCatalog.find(demoId.orEmpty())
            val onLessonClick: (String) -> Unit = { navController.navigate(Routes.lesson(it)) }
            val onBack: () -> Unit = { navController.popBackStack() }
            when (item?.id) {
                "counter" -> CounterDemo(item!!, onBack, onLessonClick)
                "todo" -> TodoDemo(item!!, onBack, onLessonClick)
                "animation" -> AnimationDemo(item!!, onBack, onLessonClick)
                "dialogs" -> DialogDemo(item!!, onBack, onLessonClick)
                "scaffold" -> ScaffoldDemo(item!!, onBack, onLessonClick)
                "login-form" -> LoginFormDemo(item!!, onBack, onLessonClick)
                "controls" -> ControlsDemo(item!!, onBack, onLessonClick)
                else -> NotFoundScreen()
            }
        }
    }
}
