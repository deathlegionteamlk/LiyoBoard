# LiyoBoard ProGuard Rules

# Keep keyboard service
-keepclassmembers class com.deathlegion.liyoboard.keyboard.LiyoBoardIME {
    *;
}

# Keep theme models
-keep class com.deathlegion.liyoboard.theme.** { *; }

# Keep extension models
-keep class com.deathlegion.liyoboard.extension.** { *; }

# Keep font models
-keep class com.deathlegion.liyoboard.fonts.** { *; }

# Keep clipboard models
-keep class com.deathlegion.liyoboard.clipboard.** { *; }

# Keep emoji models
-keep class com.deathlegion.liyoboard.emoji.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
