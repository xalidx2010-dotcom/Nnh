package com.example.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.SoundManager
import com.example.game.data.*
import com.example.game.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GameDatabase.getDatabase(application)
    private val auraDao = db.auraDao()
    private val soundManager = SoundManager()

    // Persistent Room Flows
    val moodEntries: StateFlow<List<MoodEntry>> = auraDao.getAllMoodEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habits: StateFlow<List<Habit>> = auraDao.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val focusSessions: StateFlow<List<FocusSession>> = auraDao.getAllFocusSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AuraTab.DASHBOARD)
    val currentTab: StateFlow<AuraTab> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: AuraTab) {
        _currentTab.value = tab
    }

    // Current Aesthetic Theme
    private val _currentTheme = MutableStateFlow(AuraTheme.COSMIC_NIGHT)
    val currentTheme: StateFlow<AuraTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AuraTheme) {
        _currentTheme.value = theme
    }

    // Sound Volume
    private val _soundVolume = MutableStateFlow(0.5f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    fun setSoundVolume(vol: Float) {
        _soundVolume.value = vol
        soundManager.setVolume(vol)
    }

    // Toast Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- MOOD JOURNAL ACTIONS ---
    fun addMoodEntry(moodValue: Float, note: String, tagsList: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsString = tagsList.joinToString(",")
            val entry = MoodEntry(
                moodValue = moodValue,
                note = note,
                tags = tagsString
            )
            auraDao.insertMoodEntry(entry)
            showToast("Gündəlik qeydiniz yadda saxlanıldı! 🌟")
        }
    }

    fun deleteMoodEntry(entry: MoodEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            auraDao.deleteMoodEntry(entry)
            showToast("Qeyd silindi.")
        }
    }

    // --- HABIT ACTIONS ---
    fun addHabit(name: String, category: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = Habit(
                name = name,
                category = category,
                colorHex = colorHex
            )
            auraDao.insertHabit(habit)
            showToast("Yeni vərdiş əlavə olundu: $name 🌱")
        }
    }

    fun toggleHabitCompletion(habit: Habit, dateString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dates = habit.completedDatesJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            val isAlreadyCompleted = dates.contains(dateString)

            val newDates: List<String>
            val newStreak: Int
            val newMax: Int

            if (isAlreadyCompleted) {
                dates.remove(dateString)
                newDates = dates
                newStreak = (habit.streak - 1).coerceAtLeast(0)
                newMax = habit.maxStreak
                showToast("Vərdiş tamamlanması ləğv edildi.")
            } else {
                dates.add(dateString)
                newDates = dates
                
                // Calculate streak based on daily consistency
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                
                // Check if yesterday is also in dates to increment streak
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(calendar.time)
                
                val currentStreakValue = if (dates.contains(yesterdayStr) || habit.streak == 0) {
                    habit.streak + 1
                } else {
                    1 // Break streak, reset to 1
                }
                
                newStreak = currentStreakValue
                newMax = maxOf(habit.maxStreak, currentStreakValue)
                showToast("Afərin! Vərdiş tamamlandı! 🎉")
            }

            val updatedHabit = habit.copy(
                completedDatesJson = newDates.joinToString(","),
                streak = newStreak,
                maxStreak = newMax
            )
            auraDao.updateHabit(updatedHabit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            auraDao.deleteHabit(habit)
            showToast("Vərdiş silindi.")
        }
    }

    // --- FOCUS POMODORO TIMER STATE & ACTIONS ---
    private val _focusTimerMinutes = MutableStateFlow(25)
    val focusTimerMinutes: StateFlow<Int> = _focusTimerMinutes.asStateFlow()

    private val _focusTimeRemainingSeconds = MutableStateFlow(25 * 60)
    val focusTimeRemainingSeconds: StateFlow<Int> = _focusTimeRemainingSeconds.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()

    private val _selectedFocusCategory = MutableStateFlow("Study")
    val selectedFocusCategory: StateFlow<String> = _selectedFocusCategory.asStateFlow()

    private val _selectedFocusSound = MutableStateFlow("None")
    val selectedFocusSound: StateFlow<String> = _selectedFocusSound.asStateFlow()

    private var timerJob: Job? = null

    fun setFocusMinutes(minutes: Int) {
        if (!_isFocusTimerRunning.value) {
            _focusTimerMinutes.value = minutes
            _focusTimeRemainingSeconds.value = minutes * 60
        }
    }

    fun setFocusCategory(category: String) {
        _selectedFocusCategory.value = category
    }

    fun setFocusSound(sound: String) {
        _selectedFocusSound.value = sound
        if (_isFocusTimerRunning.value) {
            soundManager.playFocusTrack(sound)
        }
    }

    fun startFocusTimer() {
        if (_isFocusTimerRunning.value) return
        _isFocusTimerRunning.value = true
        soundManager.playFocusTrack(_selectedFocusSound.value)

        timerJob = viewModelScope.launch {
            while (_focusTimeRemainingSeconds.value > 0) {
                delay(1000)
                _focusTimeRemainingSeconds.value -= 1
            }
            onFocusTimerComplete()
        }
    }

    fun pauseFocusTimer() {
        _isFocusTimerRunning.value = false
        timerJob?.cancel()
        soundManager.stopFocusTrack()
        showToast("Taymer dayandırıldı.")
    }

    fun resetFocusTimer() {
        _isFocusTimerRunning.value = false
        timerJob?.cancel()
        _focusTimeRemainingSeconds.value = _focusTimerMinutes.value * 60
        soundManager.stopFocusTrack()
        showToast("Taymer sıfırlandı.")
    }

    private fun onFocusTimerComplete() {
        _isFocusTimerRunning.value = false
        timerJob?.cancel()
        soundManager.stopFocusTrack()
        
        val minutesCompleted = _focusTimerMinutes.value
        viewModelScope.launch(Dispatchers.IO) {
            val session = FocusSession(
                durationMinutes = minutesCompleted,
                category = _selectedFocusCategory.value
            )
            auraDao.insertFocusSession(session)
            showToast("Təbriklər! ${minutesCompleted} dəqiqəlik diqqət seansı tamamlandı! 🏆")
        }
        
        // Reset remaining time
        _focusTimeRemainingSeconds.value = _focusTimerMinutes.value * 60
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.stopFocusTrack()
    }
}
