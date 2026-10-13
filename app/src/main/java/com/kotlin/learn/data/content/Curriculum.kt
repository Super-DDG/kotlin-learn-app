package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson

/** 全部课程内容入口。 */
object Curriculum {
    val modules: List<CourseModule> = listOf(
        KotlinBasics.moduleOf(KotlinBasics.lessons),
        AndroidCore.moduleOf(AndroidCore.lessons),
        ComposeBasics.moduleOf(ComposeBasics.lessons),
    )

    /** 按课时 ID 查找课程；找不到返回 null（由 UI 显示兜底页）。 */
    fun findLesson(lessonId: String): Lesson? =
        modules.asSequence()
            .flatMap { it.lessons }
            .firstOrNull { it.id == lessonId }

    /** 按课时 ID 查找所属模块。 */
    fun findModuleOf(lessonId: String): CourseModule? =
        modules.firstOrNull { m -> m.lessons.any { it.id == lessonId } }

    val totalLessonCount: Int get() = modules.sumOf { it.lessons.size }
}
