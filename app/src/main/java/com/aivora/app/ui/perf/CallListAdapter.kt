package com.aivora.app.ui.perf

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.ImageViewCompat
import coil3.dispose
import coil3.load
import coil3.request.crossfade
import com.aivora.app.R
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class CallListItem(
    val id: String,
    val name: String,
    val detail: String,
    val missed: Boolean,
    val video: Boolean,
    val time: String,
    val avatarUrl: String? = null,
)

class CallListAdapter(
    private val onClick: (CallListItem) -> Unit,
) : ListAdapter<CallListItem, CallListAdapter.Holder>(DIFF) {

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
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = FrameLayout.LayoutParams(dp(44), dp(44))
        }
        val avatarImage = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(dp(44), dp(44))
            outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }
        val avatarBox = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            addView(avatar)
            addView(avatarImage)
        }
        val mid = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also {
                it.marginStart = dp(12)
                it.marginEnd = dp(8)
            }
        }
        val name = TextView(ctx).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        val detail = TextView(ctx).apply {
            setTextColor(NativeColors.TextSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        mid.addView(name)
        mid.addView(detail)
        val end = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            minimumWidth = dp(64)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        val time = TextView(ctx).apply {
            setTextColor(NativeColors.TextSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).also { it.gravity = Gravity.END }
        }
        val icon = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        end.addView(time)
        end.addView(icon.apply {
            val lp = LinearLayout.LayoutParams(dp(22), dp(22))
            lp.gravity = Gravity.END
            lp.topMargin = dp(4)
            layoutParams = lp
        })
        row.addView(avatarBox)
        row.addView(mid)
        row.addView(end)
        return Holder(row, avatar, avatarImage, name, detail, time, icon)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class Holder(
        itemView: View,
        private val avatar: TextView,
        private val avatarImage: ImageView,
        private val name: TextView,
        private val detail: TextView,
        private val time: TextView,
        private val icon: ImageView,
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: CallListItem, onClick: (CallListItem) -> Unit) {
            val letter = item.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            avatar.text = letter
            avatar.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(NativeColors.Primary)
            }
            val url = item.avatarUrl?.takeIf { it.isNotBlank() }
            avatarImage.setImageDrawable(null)
            if (url != null) {
                avatarImage.load(url) {
                    crossfade(false)
                    size((44 * itemView.resources.displayMetrics.density).toInt())
                }
            } else {
                avatarImage.dispose()
            }
            name.text = item.name
            name.setTextColor(if (item.missed) NativeColors.Danger else NativeColors.Ink)
            detail.text = item.detail
            time.text = item.time
            icon.setImageResource(if (item.video) R.drawable.ic_aivora_video else R.drawable.ic_aivora_call)
            ImageViewCompat.setImageTintList(
                icon,
                android.content.res.ColorStateList.valueOf(if (item.missed) NativeColors.Danger else NativeColors.Primary),
            )
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CallListItem>() {
            override fun areItemsTheSame(a: CallListItem, b: CallListItem) = a.id == b.id
            override fun areContentsTheSame(a: CallListItem, b: CallListItem) = a == b
        }
    }
}
