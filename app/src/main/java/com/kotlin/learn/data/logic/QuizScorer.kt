package com.kotlin.learn.data.logic

import com.kotlin.learn.data.model.QuizQuestion

data class QuizResult(val correctCount: Int, val total: Int) {
    val allCorrect: Boolean get() = total > 0 && correctCount == total
}

object QuizScorer {
    /**
     * 判分。answers：题目下标 -> 用户选择的选项下标；未作答的题按答错计。
     */
    fun score(questions: List<QuizQuestion>, answers: Map<Int, Int>): QuizResult {
        var correct = 0
        questions.forEachIndexed { index, q ->
            if (answers[index] == q.correctIndex) correct++
        }
        return QuizResult(correct, questions.size)
    }
}
