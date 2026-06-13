package com.deathlegion.liyoboard.fonts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * FontBrowserActivity - Browse 500+ fonts with preview
 */
class FontBrowserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var fontManager: FontManager
    private var currentCategory: FontCategory? = null
    private var allFonts: List<FontInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_font_browser)

        fontManager = FontManager.getInstance(this)
        allFonts = fontManager.getAllFonts()

        recyclerView = findViewById(R.id.rv_fonts)
        chipGroupCategories = findViewById(R.id.chip_group_categories)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        setupCategoryChips()
        loadFonts()
    }

    private fun setupCategoryChips() {
        FontCategory.entries.forEach { category ->
            val chip = Chip(this).apply {
                text = category.displayName
                isCheckable = true
                setOnClickListener {
                    currentCategory = if (isChecked) category else null
                    loadFonts()
                }
            }
            chipGroupCategories.addView(chip)
        }
    }

    private fun loadFonts() {
        val fonts = if (currentCategory != null) {
            fontManager.getFontsByCategory(currentCategory!!)
        } else {
            allFonts
        }

        recyclerView.adapter = FontAdapter(fonts) { font ->
            val intent = android.content.Intent(this, FontPreviewActivity::class.java)
            intent.putExtra("font_id", font.id)
            startActivity(intent)
        }
    }

    inner class FontAdapter(
        private val fonts: List<FontInfo>,
        private val onFontClick: (FontInfo) -> Unit
    ) : RecyclerView.Adapter<FontAdapter.FontViewHolder>() {

        inner class FontViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvFontName: TextView = view.findViewById(R.id.tv_font_name)
            val tvFontPreview: TextView = view.findViewById(R.id.tv_font_preview)
            val tvFontCategory: TextView = view.findViewById(R.id.tv_font_category)
            val ivFontIcon: ImageView = view.findViewById(R.id.iv_font_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_font, parent, false)
            return FontViewHolder(view)
        }

        override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
            val font = fonts[position]
            holder.tvFontName.text = font.name
            holder.tvFontCategory.text = font.category.displayName

            // Apply font preview
            val typeface = fontManager.getFont(font.name)
            if (typeface != null) {
                holder.tvFontPreview.typeface = typeface
            }
            holder.tvFontPreview.text = font.preview

            holder.itemView.setOnClickListener { onFontClick(font) }
        }

        override fun getItemCount(): Int = fonts.size
    }
}
