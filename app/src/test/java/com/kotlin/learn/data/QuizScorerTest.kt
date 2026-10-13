package com.kotlin.learn.data

import com.kotlin.learn.data.logic.QuizScorer
import com.kotlin.learn.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScorerTest {
    private val questions = listOf(
        QuizQuestion("q1", listOf("a", "b"), 0, "e1"),
        QuizQuestion("q2", listOf("a", "b", "c"), 2, "e2"),
        QuizQuestion("q3", listOf("a", "b"), 1, "e3"),
    )

    @Test
    fun `全对时 allCorrect 为 true`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0, 1 to 2, 2 to 1))
        assertEquals(3, r.correctCount)
        assertEquals(3, r.total)
        assertTrue(r.allCorrect)
    }

    @Test
    fun `部分答对计正确数`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0, 1 to 0, 2 to 1))
        assertEquals(2, r.correctCount)
        assertFalse(r.allCorrect)
    }

    @Test
    fun `未作答视为答错`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0))
        assertEquals(1, r.correctCount)
        assertEquals(3, r.total)
    }

    @Test
    fun `全部未答得零分`() {
        val r = QuizScorer.score(questions, emptyMap())
        assertEquals(0, r.correctCount)
        assertFalse(r.allCorrect)
    }
}
