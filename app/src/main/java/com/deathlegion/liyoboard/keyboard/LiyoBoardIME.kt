package com.deathlegion.liyoboard.keyboard

import android.content.Context
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.clipboard.ClipboardHistoryManager
import com.deathlegion.liyoboard.emoji.EmojiManager
import com.deathlegion.liyoboard.fonts.FontManager
import com.deathlegion.liyoboard.theme.ThemeManager
import com.deathlegion.liyoboard.theme.LiyoTheme
import com.deathlegion.liyoboard.utils.LanguageSwitcher

class LiyoBoardIME : InputMethodService() {

    private var isCapsLock = false
    private var isShifted = false
    private var currentMode = KeyboardMode.ALPHABET
    private var currentSubtype = "en_US"
    private var currentTheme: LiyoTheme? = null
    private lateinit var themeManager: ThemeManager
    private lateinit var fontManager: FontManager
    private lateinit var clipboardManager: ClipboardHistoryManager
    private lateinit var emojiManager: EmojiManager
    private lateinit var languageSwitcher: LanguageSwitcher
    private var keyboardContainer: LinearLayout? = null
    private var keyboardView: LinearLayout? = null
    private var keyboardFont: Typeface? = null

    enum class KeyboardMode { ALPHABET, SYMBOLS, EMOJI, CLIPBOARD, NUMBER_PAD }

    override fun onCreate() {
        super.onCreate()
        themeManager = ThemeManager.getInstance(this)
        fontManager = FontManager.getInstance(this)
        clipboardManager = ClipboardHistoryManager.getInstance(this)
        emojiManager = EmojiManager.getInstance(this)
        languageSwitcher = LanguageSwitcher(this)
    }

    override fun onCreateInputView(): View? {
        val inflater = LayoutInflater.from(this)
        currentTheme = themeManager.getActiveTheme()
        keyboardContainer = inflater.inflate(R.layout.keyboard_container, null) as LinearLayout
        keyboardView = keyboardContainer?.findViewById(R.id.keyboard_layout)
        applyTheme()
        buildKeyboard()
        setupToolbar()
        return keyboardContainer
    }

    private fun buildKeyboard() {
        keyboardView?.removeAllViews()
        when (currentMode) {
            KeyboardMode.ALPHABET -> buildAlphabetKeyboard()
            KeyboardMode.SYMBOLS -> buildSymbolsKeyboard()
            else -> buildAlphabetKeyboard()
        }
    }

    private fun buildAlphabetKeyboard() {
        val rows = when (currentSubtype) {
            "si_LK" -> getSinhalaLayout()
            "ta_LK", "ta_IN" -> getTamilLayout()
            else -> getQwertyLayout()
        }
        for (row in rows) {
            val rowLayout = createRow(row)
            keyboardView?.addView(rowLayout)
        }
    }

    private fun buildSymbolsKeyboard() {
        val rows = getSymbolsLayout()
        for (row in rows) {
            val rowLayout = createRow(row)
            keyboardView?.addView(rowLayout)
        }
    }

    private data class KeyDef(
        val label: String, val code: Int = 0,
        val isSpecial: Boolean = false, val width: Float = 1f,
        val shiftLabel: String? = null
    )
    private data class RowDef(val keys: List<KeyDef>)

    private fun createRow(row: RowDef): LinearLayout {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        for (key in row.keys) {
            val keyView = createKeyView(key)
            rowLayout.addView(keyView)
        }
        return rowLayout
    }

    private fun createKeyView(key: KeyDef): View {
        val theme = currentTheme ?: ThemeManager.getDefaultTheme()
        return if (key.isSpecial) {
            Button(this).apply {
                text = key.label
                textSize = 14f
                setBackgroundColor(theme.specialKeyBgColor)
                setTextColor(theme.specialKeyTextColor)
                setOnClickListener { handleSpecialKey(key) }
                layoutParams = LinearLayout.LayoutParams(0, resources.getDimensionPixelSize(R.dimen.key_height), key.width)
            }
        } else {
            Button(this).apply {
                text = if (isShifted || isCapsLock) key.shiftLabel ?: key.label.uppercase() else key.label
                typeface = keyboardFont ?: Typeface.DEFAULT
                textSize = if (key.label.length > 2) 14f else 18f
                setBackgroundColor(theme.keyBgColor)
                setTextColor(theme.keyTextColor)
                setOnClickListener { handleKeyPress(key) }
                layoutParams = LinearLayout.LayoutParams(0, resources.getDimensionPixelSize(R.dimen.key_height), key.width)
            }
        }
    }

    private fun handleKeyPress(key: KeyDef) {
        performHapticFeedback()
        val ic = currentInputConnection ?: return
        when {
            key.code == KeyEvent.KEYCODE_SPACE -> ic.commitText(" ", 1)
            key.code == KeyEvent.KEYCODE_ENTER -> {
                ic.performEditorAction(currentInputEditorInfo?.imeOptions ?: 0)
            }
            key.code != 0 -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, key.code))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, key.code))
            }
            else -> {
                val text = if (isShifted || isCapsLock) key.shiftLabel ?: key.label.uppercase() else key.label
                ic.commitText(text, 1)
            }
        }
        if (isShifted && !isCapsLock) { isShifted = false; buildKeyboard() }
    }

    private fun handleSpecialKey(key: KeyDef) {
        performHapticFeedback()
        when (key.label) {
            "⇧" -> {
                if (isCapsLock) { isCapsLock = false; isShifted = false }
                else if (isShifted) { isCapsLock = true; isShifted = false }
                else { isShifted = true }
                buildKeyboard()
            }
            "⌫" -> {
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }
            "?123" -> { currentMode = KeyboardMode.SYMBOLS; buildKeyboard() }
            "ABC" -> { currentMode = KeyboardMode.ALPHABET; buildKeyboard() }
            "🌐" -> switchLanguage()
            "😊" -> Toast.makeText(this, "Emoji keyboard coming soon!", Toast.LENGTH_SHORT).show()
            "📋" -> Toast.makeText(this, "Clipboard panel coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchLanguage() {
        val subtypes = listOf("en_US", "si_LK", "ta_LK")
        val idx = subtypes.indexOf(currentSubtype)
        currentSubtype = subtypes[(idx + 1) % subtypes.size]
        currentMode = KeyboardMode.ALPHABET
        buildKeyboard()
        Toast.makeText(this, languageSwitcher.getLanguageName(currentSubtype), Toast.LENGTH_SHORT).show()
    }

    private fun setupToolbar() {
        keyboardContainer?.let { container ->
            container.findViewById<ImageButton>(R.id.btn_clipboard)?.setOnClickListener {
                Toast.makeText(this, "Clipboard", Toast.LENGTH_SHORT).show()
            }
            container.findViewById<ImageButton>(R.id.btn_emoji)?.setOnClickListener {
                Toast.makeText(this, "Emoji", Toast.LENGTH_SHORT).show()
            }
            container.findViewById<ImageButton>(R.id.btn_theme)?.setOnClickListener {
                val themes = themeManager.getAllThemes()
                val current = themes.indexOf(currentTheme)
                currentTheme = themes[(current + 1) % themes.size]
                applyTheme()
                buildKeyboard()
            }
            container.findViewById<ImageButton>(R.id.btn_settings)?.setOnClickListener {
                val intent = android.content.Intent(this, com.deathlegion.liyoboard.settings.MainActivity::class.java)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun applyTheme() {
        val theme = currentTheme ?: return
        keyboardContainer?.setBackgroundColor(theme.keyboardBgColor)
        theme.keyboardFontName?.let { keyboardFont = fontManager.getFont(it) }
    }

    private fun performHapticFeedback() {
        val prefs = getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("haptic_feedback", true)) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    // ===== KEYBOARD LAYOUTS =====
    private fun getQwertyLayout() = listOf(
        RowDef(listOf(KeyDef("q", KeyEvent.KEYCODE_Q, shiftLabel="Q"), KeyDef("w", KeyEvent.KEYCODE_W, shiftLabel="W"), KeyDef("e", KeyEvent.KEYCODE_E, shiftLabel="E"), KeyDef("r", KeyEvent.KEYCODE_R, shiftLabel="R"), KeyDef("t", KeyEvent.KEYCODE_T, shiftLabel="T"), KeyDef("y", KeyEvent.KEYCODE_Y, shiftLabel="Y"), KeyDef("u", KeyEvent.KEYCODE_U, shiftLabel="U"), KeyDef("i", KeyEvent.KEYCODE_I, shiftLabel="I"), KeyDef("o", KeyEvent.KEYCODE_O, shiftLabel="O"), KeyDef("p", KeyEvent.KEYCODE_P, shiftLabel="P"))),
        RowDef(listOf(KeyDef("a", KeyEvent.KEYCODE_A, shiftLabel="A"), KeyDef("s", KeyEvent.KEYCODE_S, shiftLabel="S"), KeyDef("d", KeyEvent.KEYCODE_D, shiftLabel="D"), KeyDef("f", KeyEvent.KEYCODE_F, shiftLabel="F"), KeyDef("g", KeyEvent.KEYCODE_G, shiftLabel="G"), KeyDef("h", KeyEvent.KEYCODE_H, shiftLabel="H"), KeyDef("j", KeyEvent.KEYCODE_J, shiftLabel="J"), KeyDef("k", KeyEvent.KEYCODE_K, shiftLabel="K"), KeyDef("l", KeyEvent.KEYCODE_L, shiftLabel="L"))),
        RowDef(listOf(KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial=true, width=1.5f), KeyDef("z", KeyEvent.KEYCODE_Z, shiftLabel="Z"), KeyDef("x", KeyEvent.KEYCODE_X, shiftLabel="X"), KeyDef("c", KeyEvent.KEYCODE_C, shiftLabel="C"), KeyDef("v", KeyEvent.KEYCODE_V, shiftLabel="V"), KeyDef("b", KeyEvent.KEYCODE_B, shiftLabel="B"), KeyDef("n", KeyEvent.KEYCODE_N, shiftLabel="N"), KeyDef("m", KeyEvent.KEYCODE_M, shiftLabel="M"), KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial=true, width=1.5f))),
        RowDef(listOf(KeyDef("?123", isSpecial=true, width=1.2f), KeyDef(",", isSpecial=true, width=0.8f), KeyDef("🌐", isSpecial=true, width=1f), KeyDef(" ", KeyEvent.KEYCODE_SPACE, width=4f), KeyDef(".", width=0.8f), KeyDef("😊", isSpecial=true, width=1f), KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial=true, width=1.2f)))
    )

    private fun getSinhalaLayout() = listOf(
        RowDef(listOf(KeyDef("අ"), KeyDef("ආ"), KeyDef("ඇ"), KeyDef("ඈ"), KeyDef("ඉ"), KeyDef("ඊ"), KeyDef("උ"), KeyDef("ඌ"), KeyDef("ඍ"), KeyDef("එ"))),
        RowDef(listOf(KeyDef("ක"), KeyDef("ඛ"), KeyDef("ග"), KeyDef("ඝ"), KeyDef("ඞ"), KeyDef("ච"), KeyDef("ඡ"), KeyDef("ජ"), KeyDef("ඤ"), KeyDef("ට"))),
        RowDef(listOf(KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial=true, width=1.5f), KeyDef("ත"), KeyDef("ද"), KeyDef("න"), KeyDef("ප"), KeyDef("බ"), KeyDef("ම"), KeyDef("ය"), KeyDef("ර"), KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial=true, width=1.5f))),
        RowDef(listOf(KeyDef("?123", isSpecial=true, width=1.2f), KeyDef(",", isSpecial=true, width=0.8f), KeyDef("🌐", isSpecial=true, width=1f), KeyDef(" ", KeyEvent.KEYCODE_SPACE, width=4f), KeyDef(".", width=0.8f), KeyDef("😊", isSpecial=true, width=1f), KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial=true, width=1.2f)))
    )

    private fun getTamilLayout() = listOf(
        RowDef(listOf(KeyDef("அ"), KeyDef("ஆ"), KeyDef("இ"), KeyDef("ஈ"), KeyDef("உ"), KeyDef("ஊ"), KeyDef("எ"), KeyDef("ஏ"), KeyDef("ஐ"), KeyDef("ஒ"))),
        RowDef(listOf(KeyDef("க"), KeyDef("ங"), KeyDef("ச"), KeyDef("ஞ"), KeyDef("ட"), KeyDef("ண"), KeyDef("த"), KeyDef("ந"), KeyDef("ப"))),
        RowDef(listOf(KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial=true, width=1.5f), KeyDef("ம"), KeyDef("ய"), KeyDef("ர"), KeyDef("ல"), KeyDef("வ"), KeyDef("ழ"), KeyDef("ள"), KeyDef("ற"), KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial=true, width=1.5f))),
        RowDef(listOf(KeyDef("?123", isSpecial=true, width=1.2f), KeyDef(",", isSpecial=true, width=0.8f), KeyDef("🌐", isSpecial=true, width=1f), KeyDef(" ", KeyEvent.KEYCODE_SPACE, width=4f), KeyDef(".", width=0.8f), KeyDef("😊", isSpecial=true, width=1f), KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial=true, width=1.2f)))
    )

    private fun getSymbolsLayout() = listOf(
        RowDef(listOf(KeyDef("1"), KeyDef("2"), KeyDef("3"), KeyDef("4"), KeyDef("5"), KeyDef("6"), KeyDef("7"), KeyDef("8"), KeyDef("9"), KeyDef("0"))),
        RowDef(listOf(KeyDef("@"), KeyDef("#"), KeyDef("$"), KeyDef("_"), KeyDef("&"), KeyDef("-"), KeyDef("+"), KeyDef("("), KeyDef(")"), KeyDef("/"))),
        RowDef(listOf(KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial=true, width=1.5f), KeyDef("*"), KeyDef("\""), KeyDef("'"), KeyDef(":"), KeyDef(";"), KeyDef("!"), KeyDef("?"), KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial=true, width=1.5f))),
        RowDef(listOf(KeyDef("ABC", isSpecial=true, width=1.2f), KeyDef(",", isSpecial=true, width=0.8f), KeyDef("🌐", isSpecial=true, width=1f), KeyDef(" ", KeyEvent.KEYCODE_SPACE, width=4f), KeyDef(".", width=0.8f), KeyDef("😊", isSpecial=true, width=1f), KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial=true, width=1.2f)))
    )
}
