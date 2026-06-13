package com.deathlegion.liyoboard.store

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.deathlegion.liyoboard.R
import com.deathlegion.liyoboard.extension.ExtensionManager
import com.deathlegion.liyoboard.extension.ExtensionType

class ExtensionStoreActivity : AppCompatActivity() {
    private lateinit var extensionManager: ExtensionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension_store)
        extensionManager = ExtensionManager.getInstance(this)
        findViewById<Button>(R.id.btn_import)?.setOnClickListener {
            Toast.makeText(this, "Import .liyox extension", Toast.LENGTH_SHORT).show()
        }
    }
}
