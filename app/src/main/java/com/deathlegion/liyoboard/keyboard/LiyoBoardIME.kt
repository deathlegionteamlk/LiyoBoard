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
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.clipboard.ClipboardHistoryManager
import com.deathlegion.liyoboard.emoji.EmojiKeyboardView
import com.deathlegion.liyoboard.emoji.EmojiManager
import com.deathlegion.liyoboard.fonts.FontManager
import com.deathlegion.liyoboard.theme.ThemeManager
import com.deathlegion.liyoboard.theme.LiyoTheme
import com.deathlegion.liyoboard.utils.LanguageSwitcher
import com.google.android.flexbox.FlexboxLayout

/**
 * LiyoBoard IME - The main keyboard input method service
 * Supports: Sinhala, English, Tamil
 * Privacy-first: Zero network calls
 */
class LiyoBoardIME : InputMethodService() {

    // Current state
    private var isCapsLock = false
    private var isShifted = false
    private var currentMode = KeyboardMode.ALPHABET
    private var currentSubtype = "en_US"
    private var currentTheme: LiyoTheme? = null

    // Managers
    private lateinit var themeManager: ThemeManager
    private lateinit var fontManager: FontManager
    private lateinit var clipboardManager: ClipboardHistoryManager
    private lateinit var emojiManager: EmojiManager
    private lateinit var languageSwitcher: LanguageSwitcher

    // Views
    private var keyboardContainer: LinearLayout? = null
    private var keyboardView: FlexboxLayout? = null
    private var emojiKeyboardView: EmojiKeyboardView? = null
    private var clipboardView: View? = null
    private var suggestionBar: LinearLayout? = null
    private var toolbarView: LinearLayout? = null

    // Custom font
    private var keyboardFont: Typeface? = null

    enum class KeyboardMode {
        ALPHABET, SYMBOLS, EMOJI, CLIPBOARD, NUMBER_PAD
    }

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
        suggestionBar = keyboardContainer?.findViewById(R.id.suggestion_bar)
        toolbarView = keyboardContainer?.findViewById(R.id.toolbar_bar)

        setupToolbar()
        applyTheme()
        buildKeyboard()

        return keyboardContainer
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentInputConnection?.let {
            // Monitor input for clipboard
        }
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        val inputType = attribute?.inputType ?: return

        // Detect input type and switch keyboard accordingly
        when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE -> {
                currentMode = KeyboardMode.NUMBER_PAD
                buildKeyboard()
            }
            else -> {
                if (currentMode == KeyboardMode.NUMBER_PAD) {
                    currentMode = KeyboardMode.ALPHABET
                    buildKeyboard()
                }
            }
        }

        // Detect current subtype
        currentSubtype = currentInputEditorInfo?.let {
            currentInputEditorInfo.hintText?.toString() ?: "en_US"
        } ?: "en_US"
    }

    // ==========================================
    // KEYBOARD BUILDING
    // ==========================================

    private fun buildKeyboard() {
        keyboardView?.removeAllViews()

        when (currentMode) {
            KeyboardMode.ALPHABET -> buildAlphabetKeyboard()
            KeyboardMode.SYMBOLS -> buildSymbolsKeyboard()
            KeyboardMode.EMOJI -> buildEmojiKeyboard()
            KeyboardMode.CLIPBOARD -> buildClipboardKeyboard()
            KeyboardMode.NUMBER_PAD -> buildNumberPad()
        }
    }

    private fun buildAlphabetKeyboard() {
        val layoutRes = when (currentSubtype) {
            "si_LK" -> R.xml.keyboard_sinhala
            "ta_LK", "ta_IN" -> R.xml.keyboard_tamil
            else -> R.xml.keyboard_qwerty
        }

        val rows = parseKeyboardLayout(layoutRes)
        for (row in rows) {
            val rowLayout = createRow(row)
            keyboardView?.addView(rowLayout)
        }
    }

    private fun buildSymbolsKeyboard() {
        val rows = parseKeyboardLayout(R.xml.keyboard_symbols)
        for (row in rows) {
            val rowLayout = createRow(row)
            keyboardView?.addView(rowLayout)
        }
    }

    private fun buildEmojiKeyboard() {
        keyboardView?.visibility = View.GONE
        if (emojiKeyboardView == null) {
            emojiKeyboardView = EmojiKeyboardView(this, emojiManager, currentTheme)
        }
        emojiKeyboardView?.visibility = View.VISIBLE
        emojiKeyboardView?.setOnEmojiSelectedListener { emoji ->
            currentInputConnection?.commitText(emoji, 1)
            emojiManager.addToRecent(emoji)
            performHapticFeedback()
        }
        // Add emoji view to container
        val containerIndex = keyboardContainer?.indexOfChild(keyboardView)
        if (containerIndex != null && containerIndex >= 0) {
            if (emojiKeyboardView?.parent != null) {
                (emojiKeyboardView?.parent as? LinearLayout)?.removeView(emojiKeyboardView)
            }
            keyboardContainer?.addView(emojiKeyboardView, containerIndex)
        }
    }

    private fun buildClipboardKeyboard() {
        keyboardView?.visibility = View.GONE
        clipboardView = LayoutInflater.from(this).inflate(R.layout.clipboard_panel, keyboardContainer, false)
        val clipRecycler = clipboardView?.findViewById<RecyclerView>(R.id.clipboard_recycler)
        val clips = clipboardManager.getHistory()
        val adapter = com.deathlegion.liyoboard.clipboard.ClipboardAdapter(clips) { clip ->
            currentInputConnection?.commitText(clip.text, 1)
            switchMode(KeyboardMode.ALPHABET)
        }
        clipRecycler?.adapter = adapter
        keyboardContainer?.addView(clipboardView)
    }

    private fun buildNumberPad() {
        val rows = parseKeyboardLayout(R.xml.keyboard_number_pad)
        for (row in rows) {
            val rowLayout = createRow(row)
            keyboardView?.addView(rowLayout)
        }
    }

    // ==========================================
    // KEY PARSING & CREATION
    // ==========================================

    private data class KeyDef(
        val label: String,
        val code: Int = 0,
        val isSpecial: Boolean = false,
        val width: Float = 1f,
        val shiftLabel: String? = null
    )

    private data class RowDef(val keys: List<KeyDef>)

    private fun parseKeyboardLayout(xmlRes: Int): List<RowDef> {
        // In a full implementation, this would parse the XML
        // For now, return programmatic layouts
        return when (xmlRes) {
            R.xml.keyboard_qwerty -> getQwertyLayout()
            R.xml.keyboard_sinhala -> getSinhalaLayout()
            R.xml.keyboard_tamil -> getTamilLayout()
            R.xml.keyboard_symbols -> getSymbolsLayout()
            R.xml.keyboard_number_pad -> getNumberPadLayout()
            else -> getQwertyLayout()
        }
    }

    private fun createRow(row: RowDef): LinearLayout {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.MATCH_PARENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (key in row.keys) {
            val keyView = createKeyView(key)
            rowLayout.addView(keyView)
        }
        return rowLayout
    }

    private fun createKeyView(key: KeyDef): View {
        return if (key.isSpecial) {
            ImageButton(this).apply {
                setImageResource(key.code)
                background = createKeyBackground(isSpecial = true)
                setOnClickListener { handleSpecialKey(key) }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    resources.getDimensionPixelSize(R.dimen.key_height),
                    key.width
                )
            }
        } else {
            Button(this).apply {
                text = key.label
                typeface = keyboardFont ?: Typeface.DEFAULT
                textSize = if (key.label.length > 2) 14f else 18f
                background = createKeyBackground(isSpecial = false)
                setTextColor(currentTheme?.keyTextColor ?: 0xFF000000.toInt())
                setOnClickListener { handleKeyPress(key) }
                setOnLongClickListener {
                    handleLongPress(key)
                    true
                }
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    resources.getDimensionPixelSize(R.dimen.key_height),
                    key.width
                )
            }
        }
    }

    private fun createKeyBackground(isSpecial: Boolean): android.graphics.drawable.Drawable {
        val theme = currentTheme ?: ThemeManager.getDefaultTheme()
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(if (isSpecial) theme.specialKeyBgColor else theme.keyBgColor)
            cornerRadius = theme.keyCornerRadius
            setStroke(
                (theme.keyBorderWidth).toInt().coerceAtLeast(0),
                theme.keyBorderColor
            )
        }
    }

    // ==========================================
    // KEY LAYOUTS
    // ==========================================

    private fun getQwertyLayout(): List<RowDef> = listOf(
        RowDef(listOf(
            KeyDef("q", KeyEvent.KEYCODE_Q, shiftLabel = "Q"),
            KeyDef("w", KeyEvent.KEYCODE_W, shiftLabel = "W"),
            KeyDef("e", KeyEvent.KEYCODE_E, shiftLabel = "E"),
            KeyDef("r", KeyEvent.KEYCODE_R, shiftLabel = "R"),
            KeyDef("t", KeyEvent.KEYCODE_T, shiftLabel = "T"),
            KeyDef("y", KeyEvent.KEYCODE_Y, shiftLabel = "Y"),
            KeyDef("u", KeyEvent.KEYCODE_U, shiftLabel = "U"),
            KeyDef("i", KeyEvent.KEYCODE_I, shiftLabel = "I"),
            KeyDef("o", KeyEvent.KEYCODE_O, shiftLabel = "O"),
            KeyDef("p", KeyEvent.KEYCODE_P, shiftLabel = "P")
        )),
        RowDef(listOf(
            KeyDef("a", KeyEvent.KEYCODE_A, shiftLabel = "A"),
            KeyDef("s", KeyEvent.KEYCODE_S, shiftLabel = "S"),
            KeyDef("d", KeyEvent.KEYCODE_D, shiftLabel = "D"),
            KeyDef("f", KeyEvent.KEYCODE_F, shiftLabel = "F"),
            KeyDef("g", KeyEvent.KEYCODE_G, shiftLabel = "G"),
            KeyDef("h", KeyEvent.KEYCODE_H, shiftLabel = "H"),
            KeyDef("j", KeyEvent.KEYCODE_J, shiftLabel = "J"),
            KeyDef("k", KeyEvent.KEYCODE_K, shiftLabel = "K"),
            KeyDef("l", KeyEvent.KEYCODE_L, shiftLabel = "L")
        )),
        RowDef(listOf(
            KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial = true, width = 1.5f),
            KeyDef("z", KeyEvent.KEYCODE_Z, shiftLabel = "Z"),
            KeyDef("x", KeyEvent.KEYCODE_X, shiftLabel = "X"),
            KeyDef("c", KeyEvent.KEYCODE_C, shiftLabel = "C"),
            KeyDef("v", KeyEvent.KEYCODE_V, shiftLabel = "V"),
            KeyDef("b", KeyEvent.KEYCODE_B, shiftLabel = "B"),
            KeyDef("n", KeyEvent.KEYCODE_N, shiftLabel = "N"),
            KeyDef("m", KeyEvent.KEYCODE_M, shiftLabel = "M"),
            KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial = true, width = 1.5f)
        )),
        RowDef(listOf(
            KeyDef("?123", isSpecial = true, width = 1.2f),
            KeyDef(",", isSpecial = true, width = 0.8f),
            KeyDef("🌐", isSpecial = true, width = 1.0f), // Language switch
            KeyDef(" ", KeyEvent.KEYCODE_SPACE, width = 4.0f),
            KeyDef(".", width = 0.8f),
            KeyDef("😊", isSpecial = true, width = 1.0f), // Emoji
            KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial = true, width = 1.2f)
        ))
    )

    private fun getSinhalaLayout(): List<RowDef> = listOf(
        RowDef(listOf(
            KeyDef("ඳ", width = 1f), KeyDef("ඟ", width = 1f),
            KeyDef("ෂ", width = 1f), KeyDef("ර", width = 1f),
            KeyDef("එ", width = 1f), KeyDef("ට", width = 1f),
            KeyDef("ය", width = 1f), KeyDef("උ", width = 1f),
            KeyDef("ඉ", width = 1f), KeyDef("ඔ", width = 1f),
            KeyDef("ප", width = 1f)
        )),
        RowDef(listOf(
            KeyDef("අ", width = 1f), KeyDef("ස", width = 1f),
            KeyDef("ද", width = 1f), KeyDef("ෆ", width = 1f),
            KeyDef("ග", width = 1f), KeyDef("හ", width = 1f),
            KeyDef("ජ", width = 1f), KeyDef("ක", width = 1f),
            KeyDef("ල", width = 1f)
        )),
        RowDef(listOf(
            KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial = true, width = 1.5f),
            KeyDef("ෙ", width = 1f), KeyDef("ව", width = 1f),
            KeyDef("ච", width = 1f), KeyDef("බ", width = 1f),
            KeyDef("න", width = 1f), KeyDef("ම", width = 1f),
            KeyDef("ට", width = 1f), KeyDef("ඛ", width = 1f),
            KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial = true, width = 1.5f)
        )),
        RowDef(listOf(
            KeyDef("?123", isSpecial = true, width = 1.2f),
            KeyDef(",", isSpecial = true, width = 0.8f),
            KeyDef("🌐", isSpecial = true, width = 1.0f),
            KeyDef(" ", KeyEvent.KEYCODE_SPACE, width = 4.0f),
            KeyDef(".", width = 0.8f),
            KeyDef("😊", isSpecial = true, width = 1.0f),
            KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial = true, width = 1.2f)
        ))
    )

    private fun getTamilLayout(): List<RowDef> = listOf(
        RowDef(listOf(
            KeyDef("ஔ", width = 1f), KeyDef("ஒ", width = 1f),
            KeyDef("ஓ", width = 1f), KeyDef("ர", width = 1f),
            KeyDef("எ", width = 1f), KeyDef("ட", width = 1f),
            KeyDef("ய", width = 1f), KeyDef("உ", width = 1f),
            KeyDef("இ", width = 1f), KeyDef("ஓ", width = 1f),
            KeyDef("ப", width = 1f)
        )),
        RowDef(listOf(
            KeyDef("அ", width = 1f), KeyDef("ச", width = 1f),
            KeyDef("ட", width = 1f), KeyDef("த", width = 1f),
            KeyDef("க", width = 1f), KeyDef("ஹ", width = 1f),
            KeyDef("ஜ", width = 1f), KeyDef("ல", width = 1f),
            KeyDef("ள", width = 1f)
        )),
        RowDef(listOf(
            KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial = true, width = 1.5f),
            KeyDef("வ", width = 1f), KeyDef("ந", width = 1f),
            KeyDef("ம", width = 1f), KeyDef("ண", width = 1f),
            KeyDef("ன", width = 1f), KeyDef("ற", width = 1f),
            KeyDef("ழ", width = 1f), KeyDef("க்ஷ", width = 1f),
            KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial = true, width = 1.5f)
        )),
        RowDef(listOf(
            KeyDef("?123", isSpecial = true, width = 1.2f),
            KeyDef(",", isSpecial = true, width = 0.8f),
            KeyDef("🌐", isSpecial = true, width = 1.0f),
            KeyDef(" ", KeyEvent.KEYCODE_SPACE, width = 4.0f),
            KeyDef(".", width = 0.8f),
            KeyDef("😊", isSpecial = true, width = 1.0f),
            KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial = true, width = 1.2f)
        ))
    )

    private fun getSymbolsLayout(): List<RowDef> = listOf(
        RowDef(listOf(
            KeyDef("1"), KeyDef("2"), KeyDef("3"),
            KeyDef("4"), KeyDef("5"), KeyDef("6"),
            KeyDef("7"), KeyDef("8"), KeyDef("9"),
            KeyDef("0")
        )),
        RowDef(listOf(
            KeyDef("@"), KeyDef("#"), KeyDef("$"),
            KeyDef("_"), KeyDef("&"), KeyDef("-"),
            KeyDef("+"), KeyDef("("), KeyDef(")"),
            KeyDef("/")
        )),
        RowDef(listOf(
            KeyDef("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, isSpecial = true, width = 1.5f),
            KeyDef("*"), KeyDef("\""), KeyDef("'"),
            KeyDef(":"), KeyDef(";"), KeyDef("!"),
            KeyDef("?"), KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial = true, width = 1.5f)
        )),
        RowDef(listOf(
            KeyDef("ABC", isSpecial = true, width = 1.2f),
            KeyDef(",", isSpecial = true, width = 0.8f),
            KeyDef("🌐", isSpecial = true, width = 1.0f),
            KeyDef(" ", KeyEvent.KEYCODE_SPACE, width = 4.0f),
            KeyDef(".", width = 0.8f),
            KeyDef("😊", isSpecial = true, width = 1.0f),
            KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial = true, width = 1.2f)
        ))
    )

    private fun getNumberPadLayout(): List<RowDef> = listOf(
        RowDef(listOf(
            KeyDef("1"), KeyDef("2"), KeyDef("3")
        )),
        RowDef(listOf(
            KeyDef("4"), KeyDef("5"), KeyDef("6")
        )),
        RowDef(listOf(
            KeyDef("7"), KeyDef("8"), KeyDef("9")
        )),
        RowDef(listOf(
            KeyDef("-"), KeyDef("0"), KeyDef(".")
        )),
        RowDef(listOf(
            KeyDef("ABC", isSpecial = true, width = 1.5f),
            KeyDef(" ", KeyEvent.KEYCODE_SPACE, width = 3.0f),
            KeyDef("⌫", KeyEvent.KEYCODE_DEL, isSpecial = true, width = 1.5f)
        ))
    )

    // ==========================================
    // KEY HANDLING
    // ==========================================

    private fun handleKeyPress(key: KeyDef) {
        val ic = currentInputConnection ?: return
        performHapticFeedback()

        when {
            key.code == KeyEvent.KEYCODE_SPACE -> {
                ic.commitText(" ", 1)
            }
            key.code == KeyEvent.KEYCODE_ENTER -> {
                val action = currentInputEditorInfo?.imeOptions ?: 0
                if (action and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0 ||
                    currentInputEditorInfo?.inputType?.and(InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
                    ic.commitText("\n", 1)
                } else {
                    ic.performEditorAction(action and EditorInfo.IME_MASK_ACTION)
                }
            }
            key.code != 0 -> {
                val code = if (isShifted || isCapsLock) key.shiftLabel?.first()?.code ?: key.code else key.code
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
                if (isShifted && !isCapsLock) {
                    isShifted = false
                    updateShiftState()
                }
            }
            else -> {
                val text = if (isShifted || isCapsLock) key.shiftLabel ?: key.label.uppercase() else key.label
                ic.commitText(text, 1)
                if (isShifted && !isCapsLock) {
                    isShifted = false
                    updateShiftState()
                }
            }
        }
    }

    private fun handleSpecialKey(key: KeyDef) {
        performHapticFeedback()
        when (key.label) {
            "⇧" -> {
                if (isCapsLock) {
                    isCapsLock = false
                    isShifted = false
                } else if (isShifted) {
                    isCapsLock = true
                    isShifted = false
                } else {
                    isShifted = true
                }
                updateShiftState()
            }
            "⌫" -> {
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                )
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL)
                )
            }
            "↵" -> handleKeyPress(KeyDef("↵", KeyEvent.KEYCODE_ENTER, isSpecial = true))
            "?123", "=\\<" -> switchMode(KeyboardMode.SYMBOLS)
            "ABC" -> switchMode(KeyboardMode.ALPHABET)
            "😊" -> switchMode(KeyboardMode.EMOJI)
            "📋" -> switchMode(KeyboardMode.CLIPBOARD)
            "🌐" -> switchLanguage()
        }
    }

    private fun handleLongPress(key: KeyDef): Boolean {
        performHapticFeedback()
        when (key.label) {
            "⇧" -> {
                isCapsLock = !isCapsLock
                isShifted = isCapsLock
                updateShiftState()
                return true
            }
            " " -> {
                // Long press space: show language switcher
                showLanguageSelector()
                return true
            }
            else -> {
                // Show accent/popup characters
                showKeyPopup(key)
                return true
            }
        }
    }

    // ==========================================
    // MODE SWITCHING
    // ==========================================

    private fun switchMode(mode: KeyboardMode) {
        // Clean up previous mode views
        emojiKeyboardView?.visibility = View.GONE
        clipboardView?.let {
            keyboardContainer?.removeView(it)
            clipboardView = null
        }
        keyboardView?.visibility = View.VISIBLE

        currentMode = mode
        buildKeyboard()
    }

    // ==========================================
    // LANGUAGE SWITCHING
    // ==========================================

    private fun switchLanguage() {
        val subtypes = listOf("en_US", "si_LK", "ta_LK")
        val currentIndex = subtypes.indexOf(currentSubtype)
        val nextIndex = (currentIndex + 1) % subtypes.size
        currentSubtype = subtypes[nextIndex]

        switchMode(KeyboardMode.ALPHABET)
        showToast(languageSwitcher.getLanguageName(currentSubtype))
    }

    private fun showLanguageSelector() {
        val languages = arrayOf("English", "සිංහල (Sinhala)", "தமிழ் (Tamil)")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_language))
        builder.setItems(languages) { _, which ->
            currentSubtype = when (which) {
                0 -> "en_US"
                1 -> "si_LK"
                2 -> "ta_LK"
                else -> "en_US"
            }
            switchMode(KeyboardMode.ALPHABET)
        }
        builder.show()
    }

    // ==========================================
    // THEME
    // ==========================================

    private fun applyTheme() {
        val theme = currentTheme ?: return
        keyboardContainer?.setBackgroundColor(theme.keyboardBgColor)

        // Apply keyboard font
        theme.keyboardFontName?.let { fontName ->
            keyboardFont = fontManager.getFont(fontName)
        }
    }

    // ==========================================
    // TOOLBAR
    // ==========================================

    private fun setupToolbar() {
        toolbarView?.let { toolbar ->
            toolbar.findViewById<ImageButton>(R.id.btn_clipboard)?.setOnClickListener {
                switchMode(KeyboardMode.CLIPBOARD)
            }
            toolbar.findViewById<ImageButton>(R.id.btn_emoji)?.setOnClickListener {
                switchMode(KeyboardMode.EMOJI)
            }
            toolbar.findViewById<ImageButton>(R.id.btn_theme)?.setOnClickListener {
                // Quick theme switch
                val themes = themeManager.getAllThemes()
                val currentIndex = themes.indexOf(currentTheme)
                val nextIndex = (currentIndex + 1) % themes.size
                currentTheme = themes[nextIndex]
                applyTheme()
                buildKeyboard()
            }
            toolbar.findViewById<ImageButton>(R.id.btn_settings)?.setOnClickListener {
                // Open settings
                val intent = android.content.Intent(this, com.deathlegion.liyoboard.settings.MainActivity::class.java)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    // ==========================================
    // SHIFT STATE
    // ==========================================

    private fun updateShiftState() {
        buildKeyboard()
    }

    // ==========================================
    // HAPTIC FEEDBACK
    // ==========================================

    private fun performHapticFeedback() {
        val prefs = getSharedPreferences("liyoboard_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("haptic_feedback", true)) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    }

    // ==========================================
    // POPUP KEYS
    // ==========================================

    private fun showKeyPopup(key: KeyDef) {
        // Show popup with accented/alternate characters
        val popupChars = getPopupChars(key.label)
        if (popupChars.isEmpty()) return

        val popup = android.widget.PopupWindow(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(8, 8, 8, 8)
        }

        for (char in popupChars) {
            val btn = Button(this).apply {
                text = char.toString()
                textSize = 16f
                setOnClickListener {
                    currentInputConnection?.commitText(char.toString(), 1)
                    popup.dismiss()
                }
            }
            layout.addView(btn)
        }

        popup.contentView = layout
        popup.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        popup.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        // Popup would be anchored to the key view in a full implementation
    }

    private fun getPopupChars(key: String): List<Char> {
        return when (key) {
            "a" -> listOf('á', 'à', 'â', 'ä', 'ã', 'å', 'ā', 'ă', 'ą')
            "e" -> listOf('é', 'è', 'ê', 'ë', 'ė', 'ę', 'ē', 'ě')
            "i" -> listOf('í', 'ì', 'î', 'ï', 'į', 'ī')
            "o" -> listOf('ó', 'ò', 'ô', 'ö', 'õ', 'ø', 'ō', 'ő')
            "u" -> listOf('ú', 'ù', 'û', 'ü', 'ũ', 'ū', 'ů')
            "c" -> listOf('ç', 'ć', 'č', 'ĉ')
            "n" -> listOf('ñ', 'ń', 'ň')
            "s" -> listOf('ś', 'š', 'ş', 'ș')
            "z" -> listOf('ź', 'ž', 'ż')
            else -> emptyList()
        }
    }

    // ==========================================
    // UTILITIES
    // ==========================================

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
