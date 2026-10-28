package com.aivora.app.ui.perf

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Single footer row under the home chat list. */
class LockedChatsFooterAdapter(
    private val onClick: () -> Unit,
) : RecyclerView.Adapter<LockedChatsFooterAdapter.Holder>() {

    var visible: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val ctx = parent.context
        val d = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setPadding(dp(16), dp(12), dp(16), dp(12))
            isClickable = true
            isFocusable = true
        }
        val avatar = TextView(ctx).apply {
            gravity = Gravity.CENTER
            text = "🔒"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x1F00BE3C)
            }
        }
        val mid = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also {
                it.marginStart = dp(14)
            }
        }
        val title = TextView(ctx).apply {
            text = "Locked chats"
            setTextColor(NativeColors.Ink)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
        }
        val sub = TextView(ctx).apply {
            text = "Authenticate to unlock"
            setTextColor(NativeColors.TextSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }
        mid.addView(title)
        mid.addView(sub)
        row.addView(avatar)
        row.addView(mid)
        return Holder(row)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.setOnClickListener { onClick() }
    }

    class Holder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView)
}
