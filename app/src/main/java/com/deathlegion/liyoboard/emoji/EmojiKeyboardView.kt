package com.deathlegion.liyoboard.emoji

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.deathlegion.liyoboard.theme.LiyoTheme

class EmojiKeyboardView(
    context: Context,
    private val emojiManager: EmojiManager,
    private val theme: LiyoTheme?
) : LinearLayout(context) {

    private var onEmojiSelected: ((String) -> Unit)? = null

    init {
        orientation = VERTICAL
        setupViews()
    }

    private fun setupViews() {
        val bgColor = theme?.emojiBgColor ?: Color.parseColor("#1E1E2E")
        setBackgroundColor(bgColor)
        val emojis = emojiManager.getEmojis(EmojiCategory.SMILEYS).take(50)
        val gridLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        for (emoji in emojis) {
            val tv = TextView(context).apply {
                text = emoji
                textSize = 24f
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    onEmojiSelected?.invoke(emoji)
                    emojiManager.addToRecent(emoji)
                }
            }
            gridLayout.addView(tv)
        }
        addView(gridLayout)
    }

    fun setOnEmojiSelectedListener(listener: (String) -> Unit) {
        onEmojiSelected = listener
    }
}
