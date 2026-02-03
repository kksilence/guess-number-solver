package com.guessnumber.solver

/**
 * 猜测历史记录数据类
 */
data class GuessHistory(
    val guess: String,      // 4位猜测
    val feedback: String    // 4位反馈
)
