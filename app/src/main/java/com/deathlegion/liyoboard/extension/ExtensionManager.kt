package com.deathlegion.liyoboard.extension

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

data class Extension(
    val id: String, val name: String, val description: String,
    val author: String, val version: Int = 1, val type: ExtensionType,
    val filePath: String? = null, val previewPath: String? = null,
    val isInstalled: Boolean = false, val installDate: Long? = null,
    val size: Long = 0, val downloads: Int = 0, val rating: Float = 0f
)

enum class ExtensionType(val displayName: String) {
    THEME_PACK("Theme Pack"), FONT_PACK("Font Pack"),
    SOUND_PACK("Sound Pack"), LAYOUT_PACK("Layout Pack"),
    STICKER_PACK("Sticker Pack"), PLUGIN("Plugin")
}

class ExtensionManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("liyoboard_extensions", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val extensionsDir = File(context.filesDir, "extensions")
    private var installedExtensions = mutableListOf<Extension>()
    companion object {
        @Volatile private var instance: ExtensionManager? = null
        fun initialize(context: Context) { instance = ExtensionManager(context.applicationContext) }
        fun getInstance(context: Context): ExtensionManager = instance ?: ExtensionManager(context.applicationContext)
        const val EXTENSION_FILE_EXT = ".liyox"
    }
    init { extensionsDir.mkdirs(); loadExtensions() }
    private fun loadExtensions() {
        val json = prefs.getString("installed_extensions", null)
        json?.let {
            val type = object : TypeToken<List<Extension>>() {}.type
            val exts: List<Extension> = gson.fromJson(it, type)
            installedExtensions = exts.toMutableList()
        }
    }
    private fun saveExtensions() { prefs.edit().putString("installed_extensions", gson.toJson(installedExtensions)).apply() }
    fun getInstalledExtensions(): List<Extension> = installedExtensions.toList()
    fun getExtensionsByType(type: ExtensionType): List<Extension> = installedExtensions.filter { it.type == type }
    fun installExtension(file: File): Extension? { return null /* placeholder */ }
    fun uninstallExtension(extensionId: String) { installedExtensions.removeIf { it.id == extensionId }; saveExtensions() }
}
