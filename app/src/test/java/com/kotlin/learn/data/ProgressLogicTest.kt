package com.kotlin.learn.data

import com.kotlin.learn.data.progress.ProgressLogic
import com.kotlin.learn.data.progress.UserProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressLogicTest {
    @Test
    fun `首次交卷记完成并记录成绩`() {
        val p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", correct = 3, total = 4)
        assertTrue("kotlin-01" in p.completedLessonIds)
        assertEquals(3 to 4, p.bestScores["kotlin-01"])
    }

    @Test
    fun `重复交卷保留最好成绩`() {
        var p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 2, 4)
        p = ProgressLogic.recordBest(p, "kotlin-01", 4, 4)
        p = ProgressLogic.recordBest(p, "kotlin-01", 1, 4)
        assertEquals(4 to 4, p.bestScores["kotlin-01"])
    }

    @Test
    fun `交卷不影响其他课时记录`() {
        var p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 3, 4)
        p = ProgressLogic.recordBest(p, "kotlin-02", 2, 5)
        assertEquals(setOf("kotlin-01", "kotlin-02"), p.completedLessonIds)
        assertEquals(3 to 4, p.bestScores["kotlin-01"])
        assertEquals(2 to 5, p.bestScores["kotlin-02"])
    }

    @Test
    fun `序列化往返一致`() {
        val p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 3, 4)
        val encoded = ProgressLogic.encode(p)
        val decoded = ProgressLogic.decode(encoded)
        assertEquals(p, decoded)
    }
}
