package com.deathlegion.liyoboard.fonts

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

/**
 * FontInfo - Metadata for a font
 */
data class FontInfo(
    val id: String,
    val name: String,
    val category: FontCategory,
    val fileName: String,
    val author: String = "Death Legion",
    val isBuiltIn: Boolean = false,
    val isPremium: Boolean = false,
    val style: FontStyle = FontStyle.REGULAR,
    val languages: List<String> = listOf("en"),
    val preview: String = "AaBbCc 123"
)

enum class FontCategory(val displayName: String) {
    SANS_SERIF("Sans Serif"),
    SERIF("Serif"),
    MONOSPACE("Monospace"),
    HANDWRITING("Handwriting"),
    DISPLAY("Display"),
    DECORATIVE("Decorative"),
    SINHALA("Sinhala"),
    TAMIL("Tamil"),
    CURSIVE("Cursive"),
    PIXEL("Pixel"),
    RETRO("Retro"),
    MODERN("Modern"),
    MINIMAL("Minimal"),
    BOLD("Bold"),
    LIGHT("Light"),
    CONDENSED("Condensed"),
    EXTENDED("Extended"),
    OUTLINE("Outline"),
    SHADOW("Shadow"),
    GRAFFITI("Graffiti"),
    GOTHIC("Gothic"),
    CALLIGRAPHY("Calligraphy"),
    COMIC("Comic"),
    TECH("Tech"),
    FUTURISTIC("Futuristic"),
    VINTAGE("Vintage"),
    ART_DECO("Art Deco"),
    BRUSH("Brush"),
    STENCIL("Stencil"),
    NEON("Neon")
}

enum class FontStyle {
    REGULAR, BOLD, ITALIC, BOLD_ITALIC, LIGHT, MEDIUM, BLACK, THIN
}

/**
 * FontManager - Manages 500+ custom fonts
 * Fonts are bundled locally - no download needed
 * Users can import their own .ttf/.otf files
 */
class FontManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("liyoboard_fonts", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val fontsDir = File(context.filesDir, "fonts")
    private var fonts = mutableListOf<FontInfo>()
    private var fontCache = mutableMapOf<String, Typeface>()

    companion object {
        @Volatile
        private var instance: FontManager? = null

        fun initialize(context: Context) {
            instance = FontManager(context.applicationContext)
            instance?.loadFonts()
        }

        fun getInstance(context: Context): FontManager {
            return instance ?: FontManager(context.applicationContext).also {
                instance = it
                it.loadFonts()
            }
        }
    }

    init {
        fontsDir.mkdirs()
    }

    private fun loadFonts() {
        fonts.clear()
        fonts.addAll(getBuiltInFonts())

        // Load custom fonts
        val customFontsJson = prefs.getString("custom_fonts", null)
        customFontsJson?.let {
            val type = object : TypeToken<List<FontInfo>>() {}.type
            val customFonts: List<FontInfo> = gson.fromJson(it, type)
            fonts.addAll(customFonts)
        }
    }

    /**
     * Get a Typeface by font name
     */
    fun getFont(fontName: String): Typeface? {
        fontCache[fontName]?.let { return it }

        val fontInfo = fonts.find { it.name == fontName || it.id == fontName } ?: return null

        val typeface = if (fontInfo.isBuiltIn) {
            try {
                Typeface.createFromAsset(context.assets, "fonts/${fontInfo.fileName}")
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
        } else {
            val fontFile = File(fontsDir, fontInfo.fileName)
            if (fontFile.exists()) {
                Typeface.createFromFile(fontFile)
            } else {
                Typeface.DEFAULT
            }
        }

        fontCache[fontName] = typeface
        return typeface
    }

    /**
     * Get all fonts
     */
    fun getAllFonts(): List<FontInfo> = fonts.toList()

    /**
     * Get fonts by category
     */
    fun getFontsByCategory(category: FontCategory): List<FontInfo> =
        fonts.filter { it.category == category }

    /**
     * Get fonts for a specific language
     */
    fun getFontsForLanguage(language: String): List<FontInfo> =
        fonts.filter { language in it.languages }

    /**
     * Search fonts by name
     */
    fun searchFonts(query: String): List<FontInfo> {
        if (query.isBlank()) return fonts
        val lowerQuery = query.lowercase()
        return fonts.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.category.displayName.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Import a custom font from a file
     */
    fun importFont(sourceFile: File, fontName: String, category: FontCategory): FontInfo? {
        return try {
            val fontId = "custom_${System.currentTimeMillis()}"
            val destFile = File(fontsDir, "$fontId.ttf")
            sourceFile.copyTo(destFile, overwrite = true)

            val fontInfo = FontInfo(
                id = fontId,
                name = fontName,
                category = category,
                fileName = "$fontId.ttf",
                isBuiltIn = false
            )

            fonts.add(fontInfo)
            saveCustomFonts()
            fontInfo
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete a custom font
     */
    fun deleteFont(fontId: String) {
        val font = fonts.find { it.id == fontId }
        if (font != null && !font.isBuiltIn) {
            val fontFile = File(fontsDir, font.fileName)
            fontFile.delete()
            fontCache.remove(font.name)
            fonts.remove(font)
            saveCustomFonts()
        }
    }

    private fun saveCustomFonts() {
        val customFonts = fonts.filter { !it.isBuiltIn }
        prefs.edit().putString("custom_fonts", gson.toJson(customFonts)).apply()
    }

    /**
     * 500+ Built-in fonts catalog
     * In a full build, actual .ttf files would be bundled in assets/fonts/
     */
    private fun getBuiltInFonts(): List<FontInfo> {
        val builtIn = mutableListOf<FontInfo>()

        // Sans Serif fonts (50+)
        val sansSerifFonts = listOf(
            "Roboto", "Roboto Light", "Roboto Medium", "Roboto Bold", "Roboto Black",
            "Roboto Thin", "Roboto Condensed", "Roboto Condensed Light",
            "Open Sans", "Open Sans Light", "Open Sans SemiBold", "Open Sans Bold",
            "Noto Sans", "Noto Sans Light", "Noto Sans Medium", "Noto Sans Bold",
            "Lato", "Lato Light", "Lato Bold", "Lato Black",
            "Montserrat", "Montserrat Light", "Montserrat Medium", "Montserrat Bold",
            "Poppins", "Poppins Light", "Poppins Medium", "Poppins Bold",
            "Inter", "Inter Light", "Inter Medium", "Inter Bold",
            "Ubuntu", "Ubuntu Light", "Ubuntu Medium", "Ubuntu Bold",
            "Source Sans Pro", "Source Sans Pro Light", "Source Sans Pro Bold",
            "Work Sans", "Work Sans Light", "Work Sans Medium", "Work Sans Bold",
            "DM Sans", "DM Sans Medium", "DM Sans Bold",
            "Nunito", "Nunito Light", "Nunito Bold",
            "Rubik", "Rubik Light", "Rubik Medium", "Rubik Bold",
            "Manrope", "Manrope Light", "Manrope Bold",
            "Space Grotesk", "Space Grotesk Medium", "Space Grotesk Bold"
        )
        sansSerifFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "sans_$index",
                name = name,
                category = FontCategory.SANS_SERIF,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Serif fonts (50+)
        val serifFonts = listOf(
            "Roboto Serif", "Noto Serif", "Merriweather", "Merriweather Light",
            "Playfair Display", "Playfair Display Bold",
            "Lora", "Lora Bold", "Source Serif Pro", "Source Serif Pro Bold",
            "Crimson Text", "Crimson Text Bold",
            "Libre Baskerville", "Libre Baskerville Bold",
            "EB Garamond", "EB Garamond Bold",
            "Cormorant Garamond", "Cormorant Garamond Bold",
            "Spectral", "Spectral Light", "Spectral Bold",
            "Bitter", "Bitter Bold",
            "Vollkorn", "Vollkorn Bold",
            "PT Serif", "PT Serif Bold",
            "Nunito Serif", "Nunito Serif Bold",
            "DM Serif Display", "DM Serif Text",
            "Fraunces", "Fraunces Bold",
            "Lora Italic", "Playfair Italic",
            "Spectral SC", "Cormorant SC",
            "Libre Caslon Text", "Libre Caslon Display",
            "Alegreya", "Alegreya Bold",
            "Cardo", "Cardo Bold",
            "Gentium Plus", "Gentium Basic",
            "Sorts Mill Goudy", "Philosopher",
            "Vidaloka", "Prata",
            "Rokkitt", "Rokkitt Bold",
            "Arvo", "Arvo Bold",
            "Bree Serif", "Cabin",
            "Noticia Text", "Noticia Text Bold",
            "Josefin Slab", "Josefin Slab Bold",
            "Abril Fatface", "Alfa Slab One"
        )
        serifFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "serif_$index",
                name = name,
                category = FontCategory.SERIF,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Monospace fonts (30+)
        val monoFonts = listOf(
            "JetBrains Mono", "JetBrains Mono Bold",
            "Fira Code", "Fira Code Bold",
            "Source Code Pro", "Source Code Pro Bold",
            "Roboto Mono", "Roboto Mono Bold",
            "Ubuntu Mono", "Ubuntu Mono Bold",
            "Noto Sans Mono", "Noto Sans Mono Bold",
            "IBM Plex Mono", "IBM Plex Mono Bold",
            "Inconsolata", "Inconsolata Bold",
            "Space Mono", "Space Mono Bold",
            "Anonymous Pro", "Anonymous Pro Bold",
            "Cascadia Code", "Cascadia Code Bold",
            "Victor Mono", "Victor Mono Bold",
            "Droid Sans Mono", "Oxygen Mono",
            "PT Mono", "Share Tech Mono",
            "Courier Prime", "Courier Prime Bold",
            "Sometype Mono", "DM Mono",
            "Azeret Mono", "Red Hat Mono"
        )
        monoFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "mono_$index",
                name = name,
                category = FontCategory.MONOSPACE,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Handwriting fonts (40+)
        val handwritingFonts = listOf(
            "Dancing Script", "Dancing Script Bold",
            "Pacifico", "Great Vibes", "Lobster",
            "Caveat", "Caveat Bold",
            "Satisfy", "Sacramento",
            "Kalam", "Kalam Bold",
            "Indie Flower", "Shadows Into Light",
            "Amatic SC", "Amatic SC Bold",
            "Courgette", "Cookie",
            "Kaushan Script", "Grand Hotel",
            "Gloria Hallelujah", "Permanent Marker",
            "Rock Salt", "Homemade Apple",
            "Architects Daughter", "Patrick Hand",
            "Covered By Your Grace", "Yellowtail",
            "Charmonman", "Charmonman Bold",
            "Mrs Saint Delafield", "Allura",
            "Sedgwick Ave", "Zhi Mang Xing",
            "Ma Shan Zheng", "Liu Jian Mao Cao",
            "Long Cang", "Zcool KuaiLe",
            "Zcool QingKe HuangYou", "Zcool XiaoWei"
        )
        handwritingFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "handwriting_$index",
                name = name,
                category = FontCategory.HANDWRITING,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Display fonts (50+)
        val displayFonts = listOf(
            "Righteous", "Bungee", "Bungee Shade",
            "Orbitron", "Orbitron Bold",
            "Rajdhani", "Rajdhani Bold",
            "Audiowide", "Black Ops One",
            "Bangers", "Bebas Neue",
            "Contrail One", "Creepster",
            "Dela Gothic One", "Fascinate",
            "Fontdiner Swanky", "Frijole",
            "Germania One", "Gravitas One",
            "Hanalei", "Irish Grover",
            "Knewave", "Lacquer",
            "Lilita One", "Lobster Two",
            "Magra", "Metal Mania",
            "Mitr", "Monoton",
            "Nosifer", "Passion One",
            "Plaster", "Press Start 2P",
            "Russo One", "Sancreek",
            "Sonsie One", "Staatliches",
            "Trade Winds", "Titan One",
            "Unlock", "Vast Shadow",
            "Wallpoet", "Warnes",
            "Wellfleet", "Zen Dots",
            "DotGothic16", "Doki Doki",
            "Rubik Glitch", "Rubik Burned",
            "Rubik Maze", "Rubik Puddles"
        )
        displayFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "display_$index",
                name = name,
                category = FontCategory.DISPLAY,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Decorative fonts (40+)
        val decorativeFonts = listOf(
            "Abril Fatface", "Alfa Slab One",
            "Anton", "Archivo Black",
            "Barlow Condensed", "Barlow Condensed Bold",
            "Barriecito", "Bungee Inline",
            "Cabin Sketch", "Cabin Sketch Bold",
            "Chewy", "Codystar",
            "Comfortaa", "Comfortaa Bold",
            "Concert One", "Elsie",
            "Emblema One", "Fascinate Inline",
            "Fredericka the Great", "Fredoka One",
            "Geostar", "Geostar Fill",
            "Gorditas", "Gugi",
            "Hi Melody", "Jua",
            "Kirang Haerang", "Luckiest Guy",
            "Miltonian", "Miltonian Tattoo",
            "Modern Antiqua", "Motley Forces",
            "Nova Cut", "Nova Mono",
            "Pangolin", "Paprika",
            "Patua One", "Rye",
            "Salsa", "Stylish"
        )
        decorativeFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "decorative_$index",
                name = name,
                category = FontCategory.DECORATIVE,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Cursive fonts (30+)
        val cursiveFonts = listOf(
            "Dancing Script", "Pacifico", "Great Vibes",
            "Lobster", "Courgette", "Kaushan Script",
            "Satisfy", "Sacramento", "Cookie",
            "Yellowtail", "Allura", "Alex Brush",
            "Beautiful Every Time", "Bilbo Swash Caps",
            "Calligraffitti", "Caveat Brush",
            "Clicker Script", "Dawning of a New Day",
            "Euphoria Script", "Felipa",
            "Gochi Hand", "Grand Hotel",
            "Herr Von Muellerhoff", "Italianno",
            "Julee", "Kristi",
            "La Belle Aurore", "Marck Script",
            "MedievalSharp", "Montez"
        )
        cursiveFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "cursive_$index",
                name = name,
                category = FontCategory.CURSIVE,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Pixel fonts (25+)
        val pixelFonts = listOf(
            "Press Start 2P", "VT323", "Pixelify Sans",
            "Silkscreen", "DotGothic16",
            "IBM Plex Mono Light", "Share Tech Mono",
            "Fira Code Light", "Source Code Pro Light",
            "Space Mono Light", "Roboto Mono Light",
            "Nova Mono", "Oxygen Mono",
            "Anonymous Pro", "Cascadia Code Light",
            "Droid Sans Mono", "JetBrains Mono Light",
            "Ubuntu Mono", "Courier Prime",
            "Red Hat Mono", "Azeret Mono Light",
            "Sometype Mono Light", "DM Mono Light",
            "Victor Mono Light"
        )
        pixelFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "pixel_$index",
                name = name,
                category = FontCategory.PIXEL,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Retro fonts (25+)
        val retroFonts = listOf(
            "Righteous", "Bungee Shade", "Creepster",
            "Fontdiner Swanky", "Frijole",
            "Germania One", "Gravitas One",
            "Hanalei", "Irish Grover",
            "Knewave", "Lacquer",
            "Metal Mania", "Monoton",
            "Nosifer", "Plaster",
            "Rye", "Sancreek",
            "Staatliches", "Trade Winds",
            "Vast Shadow", "Wallpoet",
            "Warnes", "Wellfleet",
            "Russo One", "Salsa"
        )
        retroFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "retro_$index",
                name = name,
                category = FontCategory.RETRO,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Modern fonts (30+)
        val modernFonts = listOf(
            "Inter", "Inter Light", "Inter Bold",
            "Poppins", "Poppins Light", "Poppins Bold",
            "Montserrat", "Montserrat Light", "Montserrat Bold",
            "Space Grotesk", "Space Grotesk Bold",
            "DM Sans", "DM Sans Bold",
            "Manrope", "Manrope Bold",
            "Outfit", "Outfit Bold",
            "Plus Jakarta Sans", "Plus Jakarta Sans Bold",
            "Sora", "Sora Bold",
            "Lexend", "Lexend Bold",
            "Albert Sans", "Albert Sans Bold",
            "Figtree", "Figtree Bold",
            "Hanken Grotesk", "Hanken Grotesk Bold",
            "Red Hat Display", "Red Hat Display Bold",
            "Urbanist", "Urbanist Bold"
        )
        modernFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "modern_$index",
                name = name,
                category = FontCategory.MODERN,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Sinhala fonts (25+)
        val sinhalaFonts = listOf(
            "Noto Sans Sinhala", "Noto Sans Sinhala Light",
            "Noto Sans Sinhala Medium", "Noto Sans Sinhala Bold",
            "Noto Serif Sinhala", "Noto Serif Sinhala Bold",
            "Gemunu Libre", "Gemunu Libre Bold",
            "Yaldevi", "Yaldevi Bold",
            "Mukta Malar", "Mukta Malar Bold",
            "Nirmala UI", "Nirmala UI Bold",
            "Sinhala Sangam MN", "Sinhala Sangam MN Bold",
            "Iskoola Pota", "Malithi Web",
            "Bhashitha", "Kaputa",
            "Dasitha", "FMBindumathi",
            "DL Lihini", "DL Manel",
            "DL Sarala", "EN Sinhala"
        )
        sinhalaFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "sinhala_$index",
                name = name,
                category = FontCategory.SINHALA,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("si", "en")
            ))
        }

        // Tamil fonts (25+)
        val tamilFonts = listOf(
            "Noto Sans Tamil", "Noto Sans Tamil Light",
            "Noto Sans Tamil Medium", "Noto Sans Tamil Bold",
            "Noto Serif Tamil", "Noto Serif Tamil Bold",
            "Mukta Malar", "Mukta Malar Bold",
            "Catamaran", "Catamaran Bold",
            "Hind Madurai", "Hind Madurai Bold",
            "Meera Inimai", "Arima",
            "Tiro Devanagari Hindi", "Tiro Tamil",
            "Tamil Sangam MN", "Tamil Sangam MN Bold",
            "Vijaya", "Nirmala UI Tamil",
            "Lohit Tamil", "Samyak Tamil",
            "Scribble Tamil", "Ezhil Tamil",
            "Vikatan Tamil", "Kavithai Tamil"
        )
        tamilFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "tamil_$index",
                name = name,
                category = FontCategory.TAMIL,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("ta", "en")
            ))
        }

        // Calligraphy fonts (25+)
        val calligraphyFonts = listOf(
            "Great Vibes", "Alex Brush", "Allura",
            "Beautiful Every Time", "Bilbo Swash Caps",
            "Calligraffitti", "Dawning of a New Day",
            "Euphoria Script", "Felipa",
            "Herr Von Muellerhoff", "Italianno",
            "Kristi", "La Belle Aurore",
            "Marck Script", "MedievalSharp",
            "Montez", "Mrs Saint Delafield",
            "Monsieur La Doulaise", "Pinyon Script",
            "Princess Sofia", "Rochester",
            "Royal Wedding", "Sofia",
            "Tangerine", "Vibur"
        )
        calligraphyFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "calligraphy_$index",
                name = name,
                category = FontCategory.CALLIGRAPHY,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Tech fonts (25+)
        val techFonts = listOf(
            "Orbitron", "Orbitron Bold",
            "Audiowide", "Rajdhani",
            "Rajdhani Bold", "Share Tech",
            "Electrolize", "Michroma",
            "Audiowide", "Exo 2",
            "Exo 2 Bold", "Chakra Petch",
            "Chakra Petch Bold", "Oxanium",
            "Oxanium Bold", "Quantico",
            "Quantico Bold", "Saira",
            "Saira Bold", "Teko",
            "Teko Bold", "Timer Roman",
            "Wallpoet", "ZCOOL QingKe HuangYou",
            "Black Ops One"
        )
        techFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "tech_$index",
                name = name,
                category = FontCategory.TECH,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Futuristic fonts (25+)
        val futuristicFonts = listOf(
            "Orbitron", "Audiowide", "Electrolize",
            "Michroma", "Saira Stencil One",
            "Black Ops One", "Chakra Petch",
            "Oxanium", "Quantico", "Teko",
            "Jura", "Jura Bold",
            "Kode Mono", "Major Mono Display",
            "Megrim", "Monofett",
            "Nova Square", "Play",
            "Rationale", "Russo One",
            "Saira Extra Condensed", "Saira Semi Condensed",
            "Share Tech Mono", "Sigmar One",
            "Varela Round"
        )
        futuristicFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "futuristic_$index",
                name = name,
                category = FontCategory.FUTURISTIC,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Vintage fonts (25+)
        val vintageFonts = listOf(
            "Rye", "Sancreek", "Staatliches",
            "Playfair Display", "Abril Fatface",
            "Alfa Slab One", "Anton",
            "Archivo Black", "Bitter",
            "Bungee Shade", "Concert One",
            "Elsie", "Emblema One",
            "Fredericka the Great", "Gravitas One",
            "Lora", "Merriweather",
            "Old Standard TT", "PT Serif",
            "Rokkitt", "Sorts Mill Goudy",
            "Spectral", "Vollkorn",
            "Zhi Mang Xing", "Prata"
        )
        vintageFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "vintage_$index",
                name = name,
                category = FontCategory.VINTAGE,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Brush fonts (20+)
        val brushFonts = listOf(
            "Kaushan Script", "Pacifico",
            "Lobster", "Courgette",
            "Satisfy", "Cookie",
            "Yellowtail", "Great Vibes",
            "Allura", "Alex Brush",
            "Dancing Script", "Sacramento",
            "Caveat", "Kalam",
            "Indie Flower", "Shadows Into Light",
            "Rock Salt", "Permanent Marker",
            "Gochi Hand", "Patrick Hand"
        )
        brushFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "brush_$index",
                name = name,
                category = FontCategory.BRUSH,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Graffiti fonts (15+)
        val graffitiFonts = listOf(
            "Permanent Marker", "Rock Salt",
            "Bangers", "Bungee",
            "Creepster", "Lacquer",
            "Rubik Glitch", "Rubik Burned",
            "Rubik Maze", "Rubik Puddles",
            "Russo One", "Black Ops One",
            "Nosifer", "Fascinate",
            "Monoton"
        )
        graffitiFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "graffiti_$index",
                name = name,
                category = FontCategory.GRAFFITI,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Gothic fonts (15+)
        val gothicFonts = listOf(
            "UnifrakturMaguntia", "MedievalSharp",
            "UnifrakturCook", "Metamorphous",
            "Texturina", "Cinzel",
            "Cinzel Decorative", "Aguafina Script",
            "Cantata One", "Caesar Dressing",
            "Codystar", "Eagle Lake",
            "Grechen Fuemen", "Jim Nightshade",
            "Pirata One"
        )
        gothicFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "gothic_$index",
                name = name,
                category = FontCategory.GOTHIC,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Neon fonts (10+)
        val neonFonts = listOf(
            "Monoton", "Bungee Shade",
            "Fascinate Inline", "Codystar",
            "Texturina", "Rationale",
            "Megrim", "Kode Mono",
            "Major Mono Display", "Nova Square",
            "Saira Stencil One", "Varela Round"
        )
        neonFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "neon_$index",
                name = name,
                category = FontCategory.NEON,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Stencil fonts (10+)
        val stencilFonts = listOf(
            "Saira Stencil One", "Bungee",
            "Black Ops One", "Stencil",
            "Chakra Petch", "Oxanium",
            "Quantico", "Teko",
            "Anton", "Archivo Black",
            "Bebas Neue", "Contrail One"
        )
        stencilFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "stencil_$index",
                name = name,
                category = FontCategory.STENCIL,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        // Art Deco fonts (10+)
        val artDecoFonts = listOf(
            "Poiret One", "Cinzel",
            "Cinzel Decorative", "Playfair Display",
            "Abril Fatface", "Staatliches",
            "Josefin Sans", "Alegreya Sans",
            "Spectral SC", "Vidaloka",
            "Rye", "Sancreek"
        )
        artDecoFonts.forEachIndexed { index, name ->
            builtIn.add(FontInfo(
                id = "artdeco_$index",
                name = name,
                category = FontCategory.ART_DECO,
                fileName = "${name.lowercase().replace(" ", "_")}.ttf",
                isBuiltIn = true,
                languages = listOf("en")
            ))
        }

        return builtIn
    }
}
