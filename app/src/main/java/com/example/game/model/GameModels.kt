package com.example.game.model

enum class AuraTab(val displayName: String) {
    DASHBOARD("Dashboard"),
    MOOD_JOURNAL("Mood"),
    HABITS("Habits"),
    FOCUS("Focus"),
    INSIGHTS("Insights"),
    SETTINGS("Settings")
}

enum class AuraTheme {
    COSMIC_NIGHT,   // Deep slate, dark purple glows, ambient space aesthetics
    EMERALD_FOREST, // Pine green, moss green, calm forest vibes
    ROSE_QUARTZ,    // Soft velvet reds, pale pink warm glows
    OCEAN_BREEZE    // Deep teal, calm cyan waves
}

data class GoalAchievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val iconName: String
)

