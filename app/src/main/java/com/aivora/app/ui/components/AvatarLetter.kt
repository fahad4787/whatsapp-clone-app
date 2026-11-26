package com.aivora.app.ui.components

import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aivora.app.R

/**
 * The letter shown in an avatar without a photo: the first letter of the name.
 * Null when the name has no letters (a bare phone number like "+92 300…"), so callers
 * show a person icon instead of "+" or a digit.
 */
fun avatarLetter(name: String?): String? =
    name?.firstOrNull { it.isLetter() }?.uppercaseChar()?.toString()

/** Shows [avatarLetter] in a list-row avatar, or a person icon when there is no letter. */
fun TextView.setAvatarLabel(name: String?) {
    val letter = avatarLetter(name)
    if (letter != null) {
        text = letter
        foreground = null
        return
    }
    text = ""
    // Drawn as a centred foreground: a compound drawable would sit above the empty text line.
    val icon = ContextCompat.getDrawable(context, R.drawable.ic_avatar_person)?.mutate() ?: return
    val px = (textSize * 1.35f).toInt()
    foreground = LayerDrawable(arrayOf(icon)).apply { setLayerSize(0, px, px) }
    foregroundGravity = Gravity.CENTER
}
