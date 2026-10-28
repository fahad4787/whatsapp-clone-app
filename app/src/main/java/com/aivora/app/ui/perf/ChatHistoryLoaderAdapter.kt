package com.aivora.app.ui.perf

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView

/**
 * WhatsApp-style spinner at the visual top while older pages load.
 * Place **before** [ChatMessageAdapter] in a [androidx.recyclerview.widget.ConcatAdapter]
 * for chronological (stackFromEnd) lists.
 */
class ChatHistoryLoaderAdapter : RecyclerView.Adapter<ChatHistoryLoaderAdapter.Holder>() {

    var loading: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) notifyItemInserted(0) else notifyItemRemoved(0)
        }

    override fun getItemCount(): Int = if (loading) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val d = parent.resources.displayMetrics.density
        val pad = (10 * d).toInt()
        val size = (28 * d).toInt()
        val frame = FrameLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setPadding(pad, pad, pad, pad)
        }
        val bar = ProgressBar(parent.context).apply {
            isIndeterminate = true
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER_HORIZONTAL)
        }
        frame.addView(bar)
        return Holder(frame)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = Unit

    class Holder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView)
}
