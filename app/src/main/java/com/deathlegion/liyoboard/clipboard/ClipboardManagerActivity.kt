package com.deathlegion.liyoboard.clipboard

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R

/**
 * ClipboardManagerActivity - Full clipboard management UI
 */
class ClipboardManagerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBox: EditText
    private lateinit var btnClearAll: ImageButton
    private lateinit var clipboardManager: ClipboardHistoryManager
    private var adapter: ClipboardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard_manager)

        clipboardManager = ClipboardHistoryManager.getInstance(this)

        recyclerView = findViewById(R.id.rv_clipboard)
        searchBox = findViewById(R.id.et_search_clipboard)
        btnClearAll = findViewById(R.id.btn_clear_all)

        recyclerView.layoutManager = LinearLayoutManager(this)
        loadItems()

        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                val items = if (query.isBlank()) {
                    clipboardManager.getHistory()
                } else {
                    clipboardManager.search(query)
                }
                updateAdapter(items)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnClearAll.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Clear Clipboard History")
                .setMessage("This will delete all non-pinned items. Continue?")
                .setPositiveButton("Clear") { _, _ ->
                    clipboardManager.clearHistory()
                    loadItems()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadItems() {
        updateAdapter(clipboardManager.getHistory())
    }

    private fun updateAdapter(items: List<ClipboardItem>) {
        adapter = ClipboardAdapter(
            items = items,
            onItemClicked = { item ->
                // Copy back to clipboard
                val clip = android.content.ClipData.newPlainText("text", item.text)
                (getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                    .setPrimaryClip(clip)
                finish()
            },
            onPinClicked = { item ->
                if (item.isPinned) {
                    clipboardManager.unpinItem(item.id)
                } else {
                    clipboardManager.pinItem(item.id)
                }
                loadItems()
            },
            onDeleteClicked = { item ->
                clipboardManager.deleteItem(item.id)
                loadItems()
            }
        )
        recyclerView.adapter = adapter
    }
}
