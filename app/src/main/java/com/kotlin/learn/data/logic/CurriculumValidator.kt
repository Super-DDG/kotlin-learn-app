package com.kotlin.learn.data.logic

import com.kotlin.learn.data.model.CourseModule

/**
 * 课程数据完整性校验：返回问题描述列表，空列表表示通过。
 * 规则：模块/课时 ID 全局唯一；正文、对比、解析非空；每课 1~2 段代码示例；
 * 每课 3~5 题、每题 2~4 个选项、correctIndex 在范围内。
 */
object CurriculumValidator {
    fun validate(modules: List<CourseModule>): List<String> = buildList {
        val moduleIds = mutableSetOf<String>()
        val lessonIds = mutableSetOf<String>()
        modules.forEach { m ->
            if (!moduleIds.add(m.id)) add("模块 ID 重复：${m.id}")
            if (m.lessons.isEmpty()) add("模块 ${m.id} 没有任何课时")
            m.lessons.forEach { l ->
                if (!lessonIds.add(l.id)) add("课时 ID 重复：${l.id}")
                if (l.title.isBlank()) add("课时 ${l.id} 标题不得为空")
                if (l.concept.isBlank()) add("课时 ${l.id} 概念正文不得为空")
                if (l.cppComparison.isBlank()) add("课时 ${l.id} C++ 对比不得为空")
                if (l.codeExamples.isEmpty()) add("课时 ${l.id} 至少 1 段代码示例")
                if (l.codeExamples.size > 2) add("课时 ${l.id} 代码示例最多 2 段")
                if (l.questions.size < 3) add("课时 ${l.id} 随堂小测至少 3 题")
                if (l.questions.size > 5) add("课时 ${l.id} 随堂小测最多 5 题")
                l.questions.forEachIndexed { qi, q ->
                    if (q.question.isBlank()) add("课时 ${l.id} 第 ${qi + 1} 题题干不得为空")
                    if (q.options.size < 2 || q.options.size > 4) {
                        add("课时 ${l.id} 第 ${qi + 1} 题选项须 2~4 个")
                    }
                    if (q.correctIndex !in q.options.indices) {
                        add("课时 ${l.id} 第 ${qi + 1} 题 correctIndex 越界")
                    }
                    if (q.explanation.isBlank()) add("课时 ${l.id} 第 ${qi + 1} 题解析不得为空")
                }
            }
        }
    }
}
