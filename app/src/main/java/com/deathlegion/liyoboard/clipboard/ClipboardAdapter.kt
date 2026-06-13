package com.deathlegion.liyoboard.clipboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ClipboardAdapter - RecyclerView adapter for clipboard history
 */
class ClipboardAdapter(
    private val items: List<ClipboardItem>,
    private val onItemClicked: (ClipboardItem) -> Unit,
    private val onPinClicked: ((ClipboardItem) -> Unit)? = null,
    private val onDeleteClicked: ((ClipboardItem) -> Unit)? = null
) : RecyclerView.Adapter<ClipboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClipText: TextView = view.findViewById(R.id.tv_clip_text)
        val tvClipTime: TextView = view.findViewById(R.id.tv_clip_time)
        val btnPin: ImageButton = view.findViewById(R.id.btn_pin)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clipboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Truncate display text
        val displayText = if (item.text.length > 100) {
            item.text.take(100) + "..."
        } else {
            item.text
        }

        holder.tvClipText.text = displayText
        holder.tvClipTime.text = formatTimestamp(item.timestamp)

        // Pin state
        holder.btnPin.setImageResource(
            if (item.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin_outline
        )

        holder.itemView.setOnClickListener { onItemClicked(item) }
        holder.btnPin.setOnClickListener { onPinClicked?.invoke(item) }
        holder.btnDelete.setOnClickListener { onDeleteClicked?.invoke(item) }
    }

    override fun getItemCount(): Int = items.size

    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
