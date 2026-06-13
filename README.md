# LiyoBoard - Privacy-First Keyboard

**By Death Legion Team**

An open-source, privacy-first keyboard app for Android 8.0+ built with Kotlin.

## Key Principle: Zero Network Access

LiyoBoard does **NOT** request the `INTERNET` permission. Your typing data never leaves your device. Period.

## Features

### Multi-Language Support
- English (US) - Full QWERTY keyboard
- Sinhala (සිංහල) - Native Sinhala script keyboard
- Tamil (தமிழ்) - Native Tamil script keyboard (Sri Lanka & India)
- Seamless language switching with the globe key

### 500+ Custom Fonts
- 30 categories: Sans Serif, Serif, Monospace, Handwriting, Display, Decorative, Cursive, Pixel, Retro, Modern, Minimal, Bold, Light, Condensed, Extended, Outline, Shadow, Graffiti, Gothic, Calligraphy, Comic, Tech, Futuristic, Vintage, Art Deco, Brush, Stencil, Neon, Sinhala, Tamil
- Full font preview with custom text and size control
- Import your own .ttf/.otf files
- Apply fonts directly to the keyboard

### Advanced Theming Engine
- 15 built-in themes: Catppuccin Mocha, Dracula, Nord, Tokyo Night, Solarized Dark, Rosé Pine, Gruvbox Dark, Material Light, iOS Style, AMOLED Black, Neon Cyberpunk, Ocean Wave, Neumorphic, Cherry Blossom, Emerald
- Full theme editor with color picker for every element
- 8 key styles: Rounded, Sharp, Circle, Pill, Minimal, Material, iOS, Neumorphic
- Gradient backgrounds, image backgrounds
- Key press animations: Scale, Ripple, Glow
- Custom corner radius, borders, shadows
- Export/share themes as JSON

### Built-in Clipboard Manager
- Automatic clipboard history tracking
- Pin important items
- Search through history
- Auto-cleanup of old items
- One-tap paste from keyboard

### Emoji Keyboard
- 12 categories: Recent, Smileys, People, Animals, Food, Travel, Activities, Objects, Symbols, Flags, Sinhala, Tamil
- Recent emoji tracking
- Sinhala and Tamil script characters included
- Quick emoji access from keyboard toolbar

### Extension System & Add-on Store
- Import .liyox extension packages
- Theme packs, font packs, sound packs, layout packs, sticker packs, plugins
- Export and share your custom themes
- Local-only - no automatic downloads

### Keyboard Features
- Haptic feedback with adjustable duration
- Sound feedback
- Key long-press for accented characters
- Auto-capitalization
- Double-space for period
- Adjustable keyboard height
- Quick-access toolbar (clipboard, emoji, theme, settings)

## Privacy

| Feature | Status |
|---------|--------|
| Internet Permission | **NOT REQUESTED** |
| Data Collection | **NONE** |
| Analytics | **NONE** |
| Crash Reports | **NONE** |
| Server Communication | **IMPOSSIBLE** |

## Technical

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Architecture**: MVVM pattern
- **Storage**: SharedPreferences + local files
- **Database**: Room (planned)
- **Build System**: Gradle Kotlin DSL

## Project Structure

```
app/src/main/java/com/deathlegion/liyoboard/
├── keyboard/          # IME service, key handling, layouts
├── clipboard/         # Clipboard history manager & UI
├── emoji/             # Emoji keyboard & data
├── theme/             # Theme engine, editor, preview
├── fonts/             # 500+ font manager & browser
├── extension/         # Extension system
├── store/             # Add-on store
├── settings/          # Main, onboarding, advanced settings
└── utils/             # Language switcher, locale helper
```

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Build and run

## Beta Status

This is a beta release (v1.0.0-beta1). Known limitations:
- Spell check is not yet implemented
- Word suggestions are placeholder
- Some fonts require .ttf files to be added to assets/fonts/

## License

Open Source - Death Legion Team
