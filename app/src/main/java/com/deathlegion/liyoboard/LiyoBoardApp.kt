package com.deathlegion.liyoboard

import android.app.Application
import com.deathlegion.liyoboard.theme.ThemeManager
import com.deathlegion.liyoboard.fonts.FontManager
import com.deathlegion.liyoboard.clipboard.ClipboardHistoryManager
import com.deathlegion.liyoboard.emoji.EmojiManager
import com.deathlegion.liyoboard.extension.ExtensionManager

/**
 * LiyoBoard Application - Main entry point
 * Open-source, privacy-first keyboard by Death Legion Team
 * NO internet permission = NO data leaves the device
 */
class LiyoBoardApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize managers (all local, no server calls)
        ThemeManager.initialize(this)
        FontManager.initialize(this)
        ClipboardHistoryManager.initialize(this)
        EmojiManager.initialize(this)
        ExtensionManager.initialize(this)
    }
}
