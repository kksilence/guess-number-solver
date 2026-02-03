package com.guessnumber.solver

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 主活动
 */
class MainActivity : AppCompatActivity() {

    private lateinit var solverEngine: SolverEngine
    private val historyList = mutableListOf<GuessHistory>()
    private lateinit var historyAdapter: HistoryAdapter

    // UI组件
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var guessInput: EditText
    private lateinit var feedbackInput: EditText
    private lateinit var addButton: Button
    private lateinit var clearButton: Button
    private lateinit var solveButton: Button
    private lateinit var nextGuessText: TextView
    private lateinit var remainingCountText: TextView
    private lateinit var remainingListText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化求解引擎
        solverEngine = SolverEngine(this)

        // 初始化UI
        initViews()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        guessInput = findViewById(R.id.guessInput)
        feedbackInput = findViewById(R.id.feedbackInput)
        addButton = findViewById(R.id.addButton)
        clearButton = findViewById(R.id.clearButton)
        solveButton = findViewById(R.id.solveButton)
        nextGuessText = findViewById(R.id.nextGuessText)
        remainingCountText = findViewById(R.id.remainingCountText)
        remainingListText = findViewById(R.id.remainingListText)

        // 初始显示
        resetResultDisplay()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList) { position ->
            historyList.removeAt(position)
            historyAdapter.updateItems(historyList)
            resetResultDisplay()
        }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        // 添加按钮
        addButton.setOnClickListener {
            addGuess()
        }

        // 清空按钮
        clearButton.setOnClickListener {
            clearAll()
        }

        // 计算按钮
        solveButton.setOnClickListener {
            solveNextGuess()
        }

        // 反馈输入框按回车自动提交
        feedbackInput.setOnEditorActionListener { _, _, _ ->
            addGuess()
            true
        }
    }

    /**
     * 添加猜测记录
     */
    private fun addGuess() {
        val guess = guessInput.text.toString().trim()
        val feedback = feedbackInput.text.toString().trim()

        // 验证输入
        if (!solverEngine.validateInput(guess, feedback)) {
            Toast.makeText(
                this,
                "输入无效：猜测需4位(0-5)，反馈需4位(0-2)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 添加到历史
        historyList.add(GuessHistory(guess, feedback))
        historyAdapter.updateItems(historyList)

        // 清空输入框
        guessInput.text?.clear()
        feedbackInput.text?.clear()
        guessInput.requestFocus()

        // 重置结果显示
        resetResultDisplay()

        Toast.makeText(this, "已添加", Toast.LENGTH_SHORT).show()
    }

    /**
     * 清空所有历史
     */
    private fun clearAll() {
        if (historyList.isEmpty()) return

        historyList.clear()
        historyAdapter.updateItems(historyList)
        resetResultDisplay()

        Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
    }

    /**
     * 计算下一步猜测
     */
    private fun solveNextGuess() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, "请先添加猜测历史", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示计算中
        nextGuessText.text = "计算中..."
        remainingCountText.text = ""
        remainingListText.text = ""

        // 在后台线程计算
        lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                solverEngine.getNextGuess(historyList)
            }

            // 显示结果
            displayResult(result.first, result.second)
        }
    }

    /**
     * 显示计算结果
     */
    private fun displayResult(nextGuess: String?, candidates: List<String>) {
        when {
            candidates.isEmpty() -> {
                nextGuessText.text = "❌ 错误"
                remainingCountText.text = "没有找到符合的密码"
                remainingListText.text = "请检查输入是否正确"
            }
            candidates.size == 1 -> {
                nextGuessText.text = "✅ 已找到答案！"
                remainingCountText.text = "密码是: ${candidates[0]}"
                remainingListText.text = ""
            }
            else -> {
                nextGuessText.text = "下一步建议: $nextGuess"
                remainingCountText.text = "剩余可能密码: ${candidates.size} 个"

                // 剩余密码15个以内时显示列表
                if (candidates.size <= 15) {
                    remainingListText.text = "列表: ${candidates.joinToString(", ")}"
                } else {
                    remainingListText.text = "密码数量较多，暂不显示列表"
                }
            }
        }
    }

    /**
     * 重置结果显示
     */
    private fun resetResultDisplay() {
        nextGuessText.text = "等待计算..."
        remainingCountText.text = ""
        remainingListText.text = ""
    }
}
