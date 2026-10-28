package com.aivora.app.ui.perf

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aivora.app.data.local.LocalChat

class ChatListAdapter(
    private val onOpen: (String) -> Unit,
    private val onLongPress: (LocalChat) -> Unit,
) : ListAdapter<LocalChat, ChatListAdapter.Holder>(DIFF) {

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
            setPadding(dp(16), dp(10), dp(16), dp(10))
            isClickable = true
            isFocusable = true
        }
        val avatar = TextView(ctx).apply {
            gravity = Gravity.CENTER
            setTextColor(NativeColors.White)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
        }
        val mid = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also {
                it.marginStart = dp(14)
                it.marginEnd = dp(10)
            }
        }
        val title = TextView(ctx).apply {
            setTextColor(NativeColors.Ink)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        val preview = TextView(ctx).apply {
            setTextColor(NativeColors.TextSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        mid.addView(title)
        mid.addView(preview)
        val end = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }
        val time = TextView(ctx).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
        val badge = TextView(ctx).apply {
            setTextColor(NativeColors.White)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(dp(7), dp(2), dp(7), dp(2))
        }
        end.addView(time)
        end.addView(badge.apply {
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            lp.topMargin = dp(6)
            layoutParams = lp
        })
        row.addView(avatar)
        row.addView(mid)
        row.addView(end)
        return Holder(row, avatar, title, preview, time, badge)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), onOpen, onLongPress)
    }

    class Holder(
        itemView: View,
        private val avatar: TextView,
        private val title: TextView,
        private val preview: TextView,
        private val time: TextView,
        private val badge: TextView,
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            chat: LocalChat,
            onOpen: (String) -> Unit,
            onLongPress: (LocalChat) -> Unit,
        ) {
            val d = itemView.resources.displayMetrics.density
            val letter = chat.title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            avatar.text = letter
            avatar.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(avatarColor(chat.title))
            }
            title.text = buildString {
                append(chat.title)
                if (chat.pinned) append(" 📌")
                if (chat.isMuted) append(" 🔇")
            }
            preview.text = chat.preview
            time.text = chat.time
            time.setTextColor(if (chat.unread > 0) NativeColors.Primary else NativeColors.TextSecondary)
            if (chat.unread > 0) {
                badge.visibility = View.VISIBLE
                badge.text = if (chat.unread > 99) "99+" else chat.unread.toString()
                badge.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 10f * d
                    setColor(NativeColors.Primary)
                }
            } else {
                badge.visibility = View.GONE
            }
            itemView.setOnClickListener { onOpen(chat.id) }
            itemView.setOnLongClickListener {
                onLongPress(chat)
                true
            }
        }
    }

    companion object {
        private fun avatarColor(name: String): Int {
            val hues = intArrayOf(
                0xFF00BE3C.toInt(),
                0xFF2C4A8C.toInt(),
                0xFF09C26F.toInt(),
                0xFFAA9EE8.toInt(),
                0xFF078649.toInt(),
                0xFFF44649.toInt(),
            )
            return hues[kotlin.math.abs(name.hashCode()) % hues.size]
        }

        private val DIFF = object : DiffUtil.ItemCallback<LocalChat>() {
            override fun areItemsTheSame(a: LocalChat, b: LocalChat) = a.id == b.id
            override fun areContentsTheSame(a: LocalChat, b: LocalChat) =
                a.title == b.title &&
                    a.preview == b.preview &&
                    a.time == b.time &&
                    a.unread == b.unread &&
                    a.pinned == b.pinned &&
                    a.mutedUntil == b.mutedUntil &&
                    a.favorite == b.favorite &&
                    a.archived == b.archived
        }
    }
}
