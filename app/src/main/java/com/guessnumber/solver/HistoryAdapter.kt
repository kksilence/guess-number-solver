package com.guessnumber.solver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 历史记录列表适配器
 */
class HistoryAdapter(
    private var items: List<GuessHistory>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    // 中文数字索引
    private val indexChars = listOf('①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨', '⑩')

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val indexText: TextView = view.findViewById(R.id.indexText)
        val guessText: TextView = view.findViewById(R.id.guessText)
        val feedbackText: TextView = view.findViewById(R.id.feedbackText)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 设置索引（超过10个用数字）
        holder.indexText.text = if (position < indexChars.size) {
            indexChars[position].toString()
        } else {
            "${position + 1}"
        }

        holder.guessText.text = item.guess
        holder.feedbackText.text = item.feedback

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<GuessHistory>) {
        items = newItems
        notifyDataSetChanged()
    }
}
