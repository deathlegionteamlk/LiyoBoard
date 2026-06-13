package com.deathlegion.liyoboard.fonts

import android.os.Bundle
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.deathlegion.liyoboard.R

/**
 * FontPreviewActivity - Full font preview with custom text and size control
 */
class FontPreviewActivity : AppCompatActivity() {

    private lateinit var fontManager: FontManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_font_preview)

        fontManager = FontManager.getInstance(this)

        val fontId = intent.getStringExtra("font_id") ?: return
        val fontInfo = fontManager.getAllFonts().find { it.id == fontId } ?: return

        val tvFontName = findViewById<TextView>(R.id.tv_font_name)
        val tvPreview = findViewById<TextView>(R.id.tv_font_preview)
        val etCustomText = findViewById<EditText>(R.id.et_custom_text)
        val seekFontSize = findViewById<SeekBar>(R.id.seek_font_size)
        val tvFontSize = findViewById<TextView>(R.id.tv_font_size_value)

        tvFontName.text = fontInfo.name

        val typeface = fontManager.getFont(fontInfo.name)
        if (typeface != null) {
            tvPreview.typeface = typeface
            etCustomText.typeface = typeface
        }

        // Default preview text
        val previewText = "The quick brown fox jumps over the lazy dog\n" +
                "abcdefghijklmnopqrstuvwxyz\n" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
                "0123456789 !@#\$%^&*()"
        tvPreview.text = previewText

        // Custom text preview
        etCustomText.setText("Type here to preview...")
        etCustomText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvPreview.text = s?.toString() ?: previewText
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Font size control
        seekFontSize.progress = 24
        seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress.coerceIn(8, 72)
                tvPreview.textSize = size.toFloat()
                tvFontSize.text = "${size}sp"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_apply_font).setOnClickListener {
            val prefs = getSharedPreferences("liyoboard_prefs", MODE_PRIVATE)
            prefs.edit().putString("keyboard_font", fontInfo.name).apply()
            finish()
        }
    }
}
