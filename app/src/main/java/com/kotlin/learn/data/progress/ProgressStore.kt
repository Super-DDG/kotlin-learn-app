package com.kotlin.learn.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** 用户学习进度：已完成课时 + 每课最好成绩（答对数 to 总题数）。 */
data class UserProgress(
    val completedLessonIds: Set<String> = emptySet(),
    val bestScores: Map<String, Pair<Int, Int>> = emptyMap(),
) {
    companion object { val EMPTY = UserProgress() }
}

/** 纯逻辑：进度合并与序列化，便于单元测试（不依赖 Android）。 */
object ProgressLogic {
    fun recordBest(current: UserProgress, lessonId: String, correct: Int, total: Int): UserProgress {
        if (total <= 0) return current
        val old = current.bestScores[lessonId]
        val newBest = if (old == null || correct * old.second > old.first * total) {
            correct to total
        } else old
        return current.copy(
            completedLessonIds = current.completedLessonIds + lessonId,
            bestScores = current.bestScores + (lessonId to newBest),
        )
    }

    /** 编码为键值对，供 DataStore 存储。 */
    fun encode(progress: UserProgress): Map<String, String> = buildMap {
        put("completed", progress.completedLessonIds.joinToString(","))
        progress.bestScores.forEach { (id, score) -> put("score_$id", "${score.first}/${score.second}") }
    }

    /** 从键值对解码。 */
    fun decode(prefs: Map<String, String>): UserProgress {
        val completed = prefs["completed"]?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        val scores = prefs.filterKeys { it.startsWith("score_") }.mapKeys { it.key.removePrefix("score_") }
            .mapValues { (_, v) ->
                val (c, t) = v.split('/').map { it.toInt() }
                c to t
            }
        return UserProgress(completed, scores)
    }
}

private val Context.dataStore by preferencesDataStore(name = "progress")

/** DataStore 封装：UI 层只依赖此类。 */
class ProgressStore(private val context: Context) {
    private val completedKey = stringPreferencesKey("completed")

    val progress: Flow<UserProgress> = context.dataStore.data.map { prefs ->
        val map = buildMap {
            prefs.asMap().forEach { (key, value) -> if (value is String) put(key.name, value) }
        }
        ProgressLogic.decode(map)
    }

    suspend fun recordQuizResult(lessonId: String, correct: Int, total: Int) {
        context.dataStore.edit { prefs ->
            val current = ProgressLogic.decode(
                buildMap {
                    prefs.asMap().forEach { (key, value) -> if (value is String) put(key.name, value) }
                }
            )
            val updated = ProgressLogic.recordBest(current, lessonId, correct, total)
            prefs[completedKey] = updated.completedLessonIds.joinToString(",")
            updated.bestScores.forEach { (id, score) ->
                prefs[stringPreferencesKey("score_$id")] = "${score.first}/${score.second}"
            }
        }
    }

    /** 仅供测试/初始化使用：读取一次当前进度。 */
    suspend fun loadOnce(): UserProgress = progress.first()
}
