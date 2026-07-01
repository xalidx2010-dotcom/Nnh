package com.example.game.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val moodValue: Float, // 1.0 to 10.0
    val note: String,
    val tags: String // comma-separated strings: "Work,Health,Mind,Social"
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Mind", "Fitness", "Work", "Routine", "Other"
    val colorHex: String,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val completedDatesJson: String = "[]", // Stores string array of dates "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val category: String // "Study", "Work", "Meditation", "Relax"
)

