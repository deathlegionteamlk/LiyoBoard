package com.deathlegion.liyoboard.extension

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Extension - Represents a keyboard extension (theme pack, font pack, etc.)
 */
data class Extension(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val version: Int = 1,
    val type: ExtensionType,
    val filePath: String? = null,
    val previewPath: String? = null,
    val isInstalled: Boolean = false,
    val installDate: Long? = null,
    val size: Long = 0,
    val downloads: Int = 0,
    val rating: Float = 0f
)

enum class ExtensionType(val displayName: String) {
    THEME_PACK("Theme Pack"),
    FONT_PACK("Font Pack"),
    SOUND_PACK("Sound Pack"),
    LAYOUT_PACK("Layout Pack"),
    STICKER_PACK("Sticker Pack"),
    PLUGIN("Plugin")
}

/**
 * ExtensionManager - Manages keyboard extensions
 * All extensions are local files - no automatic server downloads
 * Users manually import .liyox extension files
 */
class ExtensionManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("liyoboard_extensions", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val extensionsDir = File(context.filesDir, "extensions")
    private var installedExtensions = mutableListOf<Extension>()

    companion object {
        @Volatile
        private var instance: ExtensionManager? = null

        fun initialize(context: Context) {
            instance = ExtensionManager(context.applicationContext)
            instance?.loadExtensions()
        }

        fun getInstance(context: Context): ExtensionManager {
            return instance ?: ExtensionManager(context.applicationContext).also {
                instance = it
                it.loadExtensions()
            }
        }

        // File extension for LiyoBoard extension packages
        const val EXTENSION_FILE_EXT = ".liyox"
    }

    init {
        extensionsDir.mkdirs()
    }

    private fun loadExtensions() {
        val json = prefs.getString("installed_extensions", null)
        json?.let {
            val type = object : TypeToken<List<Extension>>() {}.type
            val extensions: List<Extension> = gson.fromJson(it, type)
            installedExtensions.clear()
            installedExtensions.addAll(extensions)
        }
    }

    private fun saveExtensions() {
        prefs.edit().putString("installed_extensions", gson.toJson(installedExtensions)).apply()
    }

    /**
     * Get all installed extensions
     */
    fun getInstalledExtensions(): List<Extension> = installedExtensions.toList()

    /**
     * Get extensions by type
     */
    fun getExtensionsByType(type: ExtensionType): List<Extension> =
        installedExtensions.filter { it.type == type }

    /**
     * Install an extension from a .liyox file
     * .liyox is a zip file containing:
     * - manifest.json (extension metadata)
     * - assets/ (theme files, font files, etc.)
     */
    fun installExtension(file: File): Extension? {
        return try {
            // Read manifest from zip
            val zipStream = java.util.zip.ZipInputStream(file.inputStream())
            var manifest: String? = null
            var zipEntry = zipStream.nextEntry

            while (zipEntry != null) {
                if (zipEntry.name == "manifest.json") {
                    manifest = zipStream.bufferedReader().readText()
                    break
                }
                zipEntry = zipStream.nextEntry
            }
            zipStream.close()

            if (manifest == null) return null

            val extension = gson.fromJson(manifest, Extension::class.java)
                .copy(isInstalled = true, installDate = System.currentTimeMillis())

            // Extract files
            val extDir = File(extensionsDir, extension.id)
            extDir.mkdirs()

            zipStream = java.util.zip.ZipInputStream(file.inputStream())
            zipEntry = zipStream.nextEntry
            while (zipEntry != null) {
                val outFile = File(extDir, zipEntry.name)
                if (zipEntry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { out ->
                        zipStream.copyTo(out)
                    }
                }
                zipEntry = zipStream.nextEntry
            }
            zipStream.close()

            installedExtensions.add(extension)
            saveExtensions()
            extension
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Uninstall an extension
     */
    fun uninstallExtension(extensionId: String) {
        val extension = installedExtensions.find { it.id == extensionId }
        if (extension != null) {
            val extDir = File(extensionsDir, extensionId)
            extDir.deleteRecursively()
            installedExtensions.remove(extension)
            saveExtensions()
        }
    }

    /**
     * Get extension directory
     */
    fun getExtensionDir(extensionId: String): File? {
        val dir = File(extensionsDir, extensionId)
        return if (dir.exists()) dir else null
    }

    /**
     * Create a sample extension package (for sharing)
     */
    fun createExtensionPack(extension: Extension, sourceDir: File): File? {
        return try {
            val outputFile = File(context.cacheDir, "${extension.name}$EXTENSION_FILE_EXT")
            val zipOut = java.util.zip.ZipOutputStream(outputFile.outputStream())

            // Add manifest
            val manifestEntry = java.util.zip.ZipEntry("manifest.json")
            zipOut.putNextEntry(manifestEntry)
            zipOut.write(gson.toJson(extension).toByteArray())
            zipOut.closeEntry()

            // Add files from source directory
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryPath = file.relativeTo(sourceDir).path
                    val entry = java.util.zip.ZipEntry("assets/$entryPath")
                    zipOut.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }

            zipOut.close()
            outputFile
        } catch (e: Exception) {
            null
        }
    }
}
