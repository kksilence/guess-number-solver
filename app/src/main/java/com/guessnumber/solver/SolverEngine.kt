package com.guessnumber.solver

import android.content.Context
import org.json.JSONObject
import java.util.Collections

/**
 * 求解引擎 - 移植自 quick_solver.py
 */
class SolverEngine(private val context: Context) {

    // 所有候选密码（预生成）
    private val allCandidates: List<String> by lazy { generateAllCandidates() }

    // 预计算数据
    private var precomputeData: JSONObject? = null

    init {
        loadPrecomputeData()
    }

    /**
     * 加载预计算数据
     */
    private fun loadPrecomputeData() {
        try {
            val json = context.assets.open("precomputed_depth2.json")
                .bufferedReader()
                .use { it.readText() }
            precomputeData = JSONObject(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 生成所有候选密码
     * 移植自 generate_all_candidates()
     */
    private fun generateAllCandidates(): List<String> {
        val candidates = mutableListOf<String>()
        val digits = "012345"

        // 生成所有4位组合
        for (d1 in digits) {
            for (d2 in digits) {
                for (d3 in digits) {
                    for (d4 in digits) {
                        val candidate = "$d1$d2$d3$d4"
                        if (isValidCandidate(candidate)) {
                            candidates.add(candidate)
                        }
                    }
                }
            }
        }

        return candidates
    }

    /**
     * 检查候选是否有效（每个数字最多出现2次）
     */
    private fun isValidCandidate(candidate: String): Boolean {
        val counts = IntArray(6)
        for (c in candidate) {
            val index = c.digitToInt()
            counts[index]++
            if (counts[index] > 2) return false
        }
        return true
    }

    /**
     * 计算反馈
     * 移植自 compute_feedback()
     */
    fun computeFeedback(guess: String, secret: String): String {
        if (guess.length != secret.length) {
            throw IllegalArgumentException("猜测和密码长度必须一致")
        }

        var fullCorrect = 0
        val guessRemaining = mutableListOf<Char>()
        val secretRemaining = mutableListOf<Char>()

        // 计算位置正确的情况
        for (i in guess.indices) {
            if (guess[i] == secret[i]) {
                fullCorrect++
            } else {
                guessRemaining.add(guess[i])
                secretRemaining.add(secret[i])
            }
        }

        // 计算数字正确位置错误的情况
        val guessCount = guessRemaining.groupingBy { it }.eachCount()
        val secretCount = secretRemaining.groupingBy { it }.eachCount()

        var partialCorrect = 0
        for ((digit, count) in guessCount) {
            partialCorrect += minOf(count, secretCount[digit] ?: 0)
        }

        val wrongCount = 4 - fullCorrect - partialCorrect

        return "1".repeat(fullCorrect) + "2".repeat(partialCorrect) + "0".repeat(wrongCount)
    }

    /**
     * 获取符合历史的候选密码
     * 移植自 get_possible_candidates()
     */
    private fun getPossibleCandidates(history: List<GuessHistory>): List<String> {
        var candidates = allCandidates

        for (record in history) {
            val newCandidates = mutableListOf<String>()
            for (candidate in candidates) {
                val computedFb = computeFeedback(record.guess, candidate)
                if (computedFb == record.feedback) {
                    newCandidates.add(candidate)
                }
            }
            candidates = newCandidates
        }

        return candidates
    }

    /**
     * 根据猜测将候选分组
     * 移植自 partition_candidates()
     */
    private fun partitionCandidates(guess: String, candidates: List<String>): Map<String, List<String>> {
        val groups = mutableMapOf<String, MutableList<String>>()

        for (candidate in candidates) {
            val fb = computeFeedback(guess, candidate)
            groups.getOrPut(fb) { mutableListOf() }.add(candidate)
        }

        return groups
    }

    /**
     * 简单Minimax算法选择最佳猜测
     * 移植自 choose_best_guess_simple()
     */
    private fun chooseBestGuessSimple(candidates: List<String>): String? {
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates[0]

        var bestGuess: String? = null
        var minMaxGroup = Int.MAX_VALUE

        for (guess in candidates) {
            val groups = partitionCandidates(guess, candidates)
            val maxGroup = groups.values.maxOfOrNull { it.size } ?: 0

            if (maxGroup < minMaxGroup) {
                minMaxGroup = maxGroup
                bestGuess = guess
            }
        }

        return bestGuess
    }

    /**
     * 根据历史获取下一步猜测
     * 返回：Pair(下一步猜测, 剩余候选列表)
     */
    fun getNextGuess(history: List<GuessHistory>): Pair<String?, List<String>> {
        val candidates = getPossibleCandidates(history)

        if (candidates.isEmpty()) {
            return Pair(null, emptyList())
        }

        if (candidates.size == 1) {
            return Pair(candidates[0], candidates)
        }

        val attempts = history.size
        var nextGuess: String? = null

        // 尝试使用预计算数据
        precomputeData?.let { data ->
            when (attempts) {
                0 -> {
                    nextGuess = "0123"
                }
                1 -> {
                    val firstFb = history[0].feedback
                    nextGuess = data.optJSONObject("layer1")
                        ?.optJSONObject(firstFb)
                        ?.optString("next_guess")
                }
                2 -> {
                    val firstFb = history[0].feedback
                    val secondFb = history[1].feedback
                    val pathKey = "0123_$firstFb"
                    nextGuess = data.optJSONObject("layer2")
                        ?.optJSONObject(pathKey)
                        ?.optJSONObject(secondFb)
                        ?.optString("next_guess")
                }
            }
        }

        // 如果没有预计算结果或超过2步，使用实时计算
        if (nextGuess == null) {
            nextGuess = chooseBestGuessSimple(candidates)
        }

        return Pair(nextGuess, candidates)
    }

    /**
     * 验证输入是否有效
     */
    fun validateInput(guess: String, feedback: String): Boolean {
        if (guess.length != 4 || feedback.length != 4) return false

        // 验证猜测：只能是0-5
        for (c in guess) {
            if (c !in '0'..'5') return false
        }

        // 验证反馈：只能是0-2
        for (c in feedback) {
            if (c !in '0'..'2') return false
        }

        // 验证猜测本身符合规则
        if (!isValidCandidate(guess)) return false

        return true
    }
}
