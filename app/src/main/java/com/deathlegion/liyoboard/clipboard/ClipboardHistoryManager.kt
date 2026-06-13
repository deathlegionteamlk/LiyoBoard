package com.deathlegion.liyoboard.clipboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * ClipboardItem - Represents a clipboard history entry
 */
data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val source: String = "user" // "user" or "system"
)

/**
 * ClipboardHistoryManager - Manages clipboard history
 * All data stored locally - NO network access
 * Features: history, pin, search, auto-cleanup
 */
class ClipboardHistoryManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("liyoboard_clipboard", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var history = mutableListOf<ClipboardItem>()
    private val maxHistorySize = 100
    private val maxItemLength = 5000

    companion object {
        @Volatile
        private var instance: ClipboardHistoryManager? = null

        fun initialize(context: Context) {
            instance = ClipboardHistoryManager(context.applicationContext)
            instance?.loadHistory()
        }

        fun getInstance(context: Context): ClipboardHistoryManager {
            return instance ?: ClipboardHistoryManager(context.applicationContext).also {
                instance = it
                it.loadHistory()
            }
        }
    }

    private fun loadHistory() {
        val json = prefs.getString("clipboard_history", null)
        json?.let {
            val type = object : TypeToken<List<ClipboardItem>>() {}.type
            val items: List<ClipboardItem> = gson.fromJson(it, type)
            history.clear()
            history.addAll(items)
        }
    }

    private fun saveHistory() {
        prefs.edit().putString("clipboard_history", gson.toJson(history)).apply()
    }

    /**
     * Add a new item to clipboard history
     */
    fun addItem(text: String) {
        if (text.isBlank() || text.length > maxItemLength) return

        // Don't add duplicates
        if (history.any { it.text == text }) {
            // Move to top instead
            history.removeIf { it.text == text }
        }

        val item = ClipboardItem(text = text.trim())
        history.add(0, item)

        // Trim to max size (keeping pinned items)
        val pinned = history.filter { it.isPinned }
        val unpinned = history.filter { !it.isPinned }

        if (history.size > maxHistorySize) {
            history.clear()
            history.addAll(pinned)
            history.addAll(unpinned.take(maxHistorySize - pinned.size))
        }

        saveHistory()
    }

    /**
     * Get all clipboard history items
     */
    fun getHistory(): List<ClipboardItem> = history.toList()

    /**
     * Get recent clipboard items
     */
    fun getRecent(count: Int = 10): List<ClipboardItem> = history.take(count)

    /**
     * Pin an item so it doesn't get auto-deleted
     */
    fun pinItem(itemId: String) {
        val index = history.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            history[index] = history[index].copy(isPinned = true)
            saveHistory()
        }
    }

    /**
     * Unpin an item
     */
    fun unpinItem(itemId: String) {
        val index = history.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            history[index] = history[index].copy(isPinned = false)
            saveHistory()
        }
    }

    /**
     * Delete a specific item
     */
    fun deleteItem(itemId: String) {
        history.removeIf { it.id == itemId }
        saveHistory()
    }

    /**
     * Clear all non-pinned items
     */
    fun clearHistory() {
        history.removeIf { !it.isPinned }
        saveHistory()
    }

    /**
     * Clear all items including pinned
     */
    fun clearAll() {
        history.clear()
        saveHistory()
    }

    /**
     * Search clipboard history
     */
    fun search(query: String): List<ClipboardItem> {
        if (query.isBlank()) return history
        val lowerQuery = query.lowercase()
        return history.filter { it.text.lowercase().contains(lowerQuery) }
    }

    /**
     * Get clipboard item by ID
     */
    fun getItem(itemId: String): ClipboardItem? = history.find { it.id == itemId }

    /**
     * Auto-cleanup: Remove items older than specified days
     */
    fun cleanupOldItems(daysToKeep: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        history.removeIf { it.timestamp < cutoffTime && !it.isPinned }
        saveHistory()
    }
}
