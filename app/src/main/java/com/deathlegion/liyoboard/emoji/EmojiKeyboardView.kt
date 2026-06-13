package com.deathlegion.liyoboard.emoji

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.theme.LiyoTheme
import com.google.android.material.tabs.TabLayout

/**
 * EmojiKeyboardView - Full emoji keyboard with categories and recent
 */
class EmojiKeyboardView(
    context: Context,
    private val emojiManager: EmojiManager,
    private val theme: LiyoTheme?
) : LinearLayout(context) {

    private var onEmojiSelected: ((String) -> Unit)? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emojiAdapter: EmojiAdapter
    private var currentCategory = EmojiCategory.RECENT

    init {
        orientation = VERTICAL
        setupViews()
        loadCategory(EmojiCategory.RECENT)
    }

    private fun setupViews() {
        val bgColor = theme?.emojiBgColor ?: Color.parseColor("#1E1E2E")

        // Category tabs
        tabLayout = TabLayout(context).apply {
            setBackgroundColor(theme?.emojiCategoryBgColor ?: Color.parseColor("#2D2D44"))
            tabMode = TabLayout.MODE_SCROLLABLE
            tabGravity = TabGravity.START
            setSelectedTabIndicatorColor(theme?.accentColor ?: Color.parseColor("#7C3AED"))
            setTabTextColors(
                theme?.specialKeyTextColor ?: Color.parseColor("#A0A0C0"),
                theme?.accentColor ?: Color.parseColor("#7C3AED")
            )
        }

        // Add category tabs
        for (category in EmojiCategory.entries) {
            val tab = tabLayout.newTab()
            tab.text = category.icon
            tab.contentDescription = category.displayName
            tabLayout.addTab(tab)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = EmojiCategory.entries[tab?.position ?: 0]
                currentCategory = category
                loadCategory(category)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        addView(tabLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // Emoji grid
        recyclerView = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 8)
            setBackgroundColor(bgColor)
            setPadding(8, 8, 8, 8)
        }

        addView(recyclerView, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        // Back to keyboard button
        val backButton = TextView(context).apply {
            text = "⌨️ ABC"
            textSize = 16f
            setTextColor(theme?.accentColor ?: Color.WHITE)
            setPadding(24, 16, 24, 16)
            gravity = Gravity.CENTER
            setBackgroundColor(theme?.emojiCategoryBgColor ?: Color.parseColor("#2D2D44"))
            setOnClickListener {
                // Signal to switch back to alphabet keyboard
                onEmojiSelected?.invoke("")  // Empty string = switch back signal
            }
        }

        addView(backButton, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        setBackgroundColor(bgColor)
    }

    private fun loadCategory(category: EmojiCategory) {
        val emojis = emojiManager.getEmojis(category)
        emojiAdapter = EmojiAdapter(emojis) { emoji ->
            if (emoji.isNotEmpty()) {
                onEmojiSelected?.invoke(emoji)
                emojiManager.addToRecent(emoji)
            }
        }
        recyclerView.adapter = emojiAdapter
    }

    fun setOnEmojiSelectedListener(listener: (String) -> Unit) {
        onEmojiSelected = listener
    }

    /**
     * EmojiAdapter - Grid adapter for emoji display
     */
    inner class EmojiAdapter(
        private val emojis: List<String>,
        private val onEmojiClick: (String) -> Unit
    ) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

        inner class EmojiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEmoji: TextView = view.findViewById(R.id.tv_emoji)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emoji, parent, false)
            return EmojiViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
            val emoji = emojis[position]
            holder.tvEmoji.text = emoji
            holder.tvEmoji.textSize = 24f
            holder.itemView.setOnClickListener {
                onEmojiClick(emoji)
            }
            holder.itemView.setOnLongClickListener {
                // Could show emoji info or add to favorites
                true
            }
        }

        override fun getItemCount(): Int = emojis.size
    }
}
