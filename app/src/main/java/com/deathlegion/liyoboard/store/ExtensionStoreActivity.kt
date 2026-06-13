package com.deathlegion.liyoboard.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.extension.Extension
import com.deathlegion.liyoboard.extension.ExtensionManager
import com.deathlegion.liyoboard.extension.ExtensionType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

/**
 * ExtensionStoreActivity - Add-on store for themes, fonts, and extensions
 * Local-first: Users import .liyox files from their device storage
 * Share functionality: Users can export and share their custom themes/extensions
 */
class ExtensionStoreActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroupTypes: ChipGroup
    private lateinit var extensionManager: ExtensionManager
    private var currentType: ExtensionType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension_store)

        extensionManager = ExtensionManager.getInstance(this)

        recyclerView = findViewById(R.id.rv_extensions)
        chipGroupTypes = findViewById(R.id.chip_group_types)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        setupTypeChips()
        loadExtensions()

        // Import button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_import).setOnClickListener {
            showImportDialog()
        }
    }

    private fun setupTypeChips() {
        ChipGroup(this)
        ExtensionType.entries.forEach { type ->
            val chip = Chip(this).apply {
                text = type.displayName
                isCheckable = true
                setOnClickListener {
                    currentType = if (isChecked) type else null
                    loadExtensions()
                }
            }
            chipGroupTypes.addView(chip)
        }
    }

    private fun loadExtensions() {
        val extensions = if (currentType != null) {
            extensionManager.getExtensionsByType(currentType!!)
        } else {
            extensionManager.getInstalledExtensions()
        }

        recyclerView.adapter = ExtensionAdapter(extensions) { extension ->
            showExtensionDetail(extension)
        }
    }

    private fun showImportDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Import Extension")
            .setMessage("Select a .liyox file from your device to install a new theme pack, font pack, or other extension.")
            .setPositiveButton("Browse") { _, _ ->
                openFilePicker()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openFilePicker() {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_IMPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMPORT && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val tempFile = File(cacheDir, "temp_import.liyox")
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val extension = extensionManager.installExtension(tempFile)
                    tempFile.delete()

                    if (extension != null) {
                        Toast.makeText(this, "Installed: ${extension.name}", Toast.LENGTH_SHORT).show()
                        loadExtensions()
                    } else {
                        Toast.makeText(this, "Invalid extension file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showExtensionDetail(extension: Extension) {
        MaterialAlertDialogBuilder(this)
            .setTitle(extension.name)
            .setMessage("${extension.description}\n\nBy: ${extension.author}\nVersion: ${extension.version}\nType: ${extension.type.displayName}")
            .setPositiveButton("Uninstall") { _, _ ->
                extensionManager.uninstallExtension(extension.id)
                loadExtensions()
                Toast.makeText(this, "Uninstalled: ${extension.name}", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Export/Share") { _, _ ->
                exportExtension(extension)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun exportExtension(extension: Extension) {
        val extDir = extensionManager.getExtensionDir(extension.id)
        if (extDir != null) {
            val file = extensionManager.createExtensionPack(extension, extDir)
            if (file != null) {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND)
                shareIntent.type = "application/octet-stream"
                shareIntent.putExtra(
                    android.content.Intent.EXTRA_STREAM,
                    androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        file
                    )
                )
                shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(android.content.Intent.createChooser(shareIntent, "Share Extension"))
            }
        }
    }

    inner class ExtensionAdapter(
        private val extensions: List<Extension>,
        private val onExtensionClick: (Extension) -> Unit
    ) : RecyclerView.Adapter<ExtensionAdapter.ExtensionViewHolder>() {

        inner class ExtensionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tv_ext_name)
            val tvAuthor: TextView = view.findViewById(R.id.tv_ext_author)
            val tvType: TextView = view.findViewById(R.id.tv_ext_type)
            val ivPreview: ImageView = view.findViewById(R.id.iv_ext_preview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtensionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_extension, parent, false)
            return ExtensionViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExtensionViewHolder, position: Int) {
            val ext = extensions[position]
            holder.tvName.text = ext.name
            holder.tvAuthor.text = "by ${ext.author}"
            holder.tvType.text = ext.type.displayName
            holder.itemView.setOnClickListener { onExtensionClick(ext) }
        }

        override fun getItemCount(): Int = extensions.size
    }

    companion object {
        private const val REQUEST_IMPORT = 1001
    }
}
