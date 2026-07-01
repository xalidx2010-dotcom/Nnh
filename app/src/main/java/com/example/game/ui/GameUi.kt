package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.data.FocusSession
import com.example.game.data.Habit
import com.example.game.data.MoodEntry
import com.example.game.model.AuraTab
import com.example.game.model.AuraTheme
import com.example.game.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

// Strongly-typed theme values to replace Any-typed lists
data class AuraThemeStyle(
    val bgBrush: Brush,
    val accentColor: Color,
    val secondaryAccent: Color,
    val surfaceBg: Color
)

@Composable
fun AuraChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeColor else inactiveColor)
            .border(
                BorderStroke(1.dp, if (selected) Color.Transparent else Color.Gray.copy(alpha = 0.2f)),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.LightGray,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameUi(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val themeStyle = remember(currentTheme) {
        when (currentTheme) {
            AuraTheme.COSMIC_NIGHT -> AuraThemeStyle(
                bgBrush = Brush.verticalGradient(listOf(Color(0xFF090714), Color(0xFF130E26), Color(0xFF06040A))),
                accentColor = Color(0xFF9D4EDD), // Violet
                secondaryAccent = Color(0xFFE040FB), // Glowing Pink
                surfaceBg = Color(0xFF1A162B)
            )
            AuraTheme.EMERALD_FOREST -> AuraThemeStyle(
                bgBrush = Brush.verticalGradient(listOf(Color(0xFF04100A), Color(0xFF0B2115), Color(0xFF020604))),
                accentColor = Color(0xFF2EC4B6), // Minty Jade
                secondaryAccent = Color(0xFF4E9F3D), // Forest Leaf Green
                surfaceBg = Color(0xFF11251A)
            )
            AuraTheme.ROSE_QUARTZ -> AuraThemeStyle(
                bgBrush = Brush.verticalGradient(listOf(Color(0xFF180E13), Color(0xFF2B1621), Color(0xFF0D0609))),
                accentColor = Color(0xFFF72585), // Crimson Rose
                secondaryAccent = Color(0xFFFF70A6), // Pale Velvet Pink
                surfaceBg = Color(0xFF261820)
            )
            AuraTheme.OCEAN_BREEZE -> AuraThemeStyle(
                bgBrush = Brush.verticalGradient(listOf(Color(0xFF05101A), Color(0xFF0C243C), Color(0xFF03070C))),
                accentColor = Color(0xFF00B4D8), // Cyan Blue
                secondaryAccent = Color(0xFF48CAE4), // Pale Water Wave
                surfaceBg = Color(0xFF122336)
            )
        }
    }

    val bgBrush = themeStyle.bgBrush
    val accentColor = themeStyle.accentColor
    val secondaryAccent = themeStyle.secondaryAccent
    val surfaceBg = themeStyle.surfaceBg

    Scaffold(
        modifier = modifier.testTag("auralife_scaffold"),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            AuraBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.setCurrentTab(it) },
                accentColor = accentColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "tab_navigation"
            ) { tab ->
                when (tab) {
                    AuraTab.DASHBOARD -> DashboardScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                    AuraTab.MOOD_JOURNAL -> MoodJournalScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                    AuraTab.HABITS -> HabitsScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                    AuraTab.FOCUS -> FocusTimerScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                    AuraTab.INSIGHTS -> InsightsScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                    AuraTab.SETTINGS -> SettingsScreen(viewModel, accentColor, secondaryAccent, surfaceBg)
                }
            }

            // Toast overlay
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .animateContentSize()
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuraBottomNavigation(
    currentTab: AuraTab,
    onTabSelected: (AuraTab) -> Unit,
    accentColor: Color
) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.4f),
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(16.dp)
    ) {
        AuraTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            val icon = when (tab) {
                AuraTab.DASHBOARD -> Icons.Default.Home
                AuraTab.MOOD_JOURNAL -> Icons.Default.Face
                AuraTab.HABITS -> Icons.Default.Check
                AuraTab.FOCUS -> Icons.Default.PlayArrow
                AuraTab.INSIGHTS -> Icons.Default.Star
                AuraTab.SETTINGS -> Icons.Default.Settings
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.displayName,
                        tint = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.8f)
                    )
                },
                label = {
                    Text(
                        text = tab.displayName,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = accentColor.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val moods by viewModel.moodEntries.collectAsStateWithLifecycle()
    val habitsList by viewModel.habits.collectAsStateWithLifecycle()
    val sessions by viewModel.focusSessions.collectAsStateWithLifecycle()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    val totalFocusMinutes = remember(sessions) { sessions.sumOf { it.durationMinutes } }
    val completedHabitsToday = remember(habitsList, sessions) {
        habitsList.count { it.completedDatesJson.split(",").contains(todayStr) }
    }
    val latestMood = remember(moods) { moods.firstOrNull() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AuraLife",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Gününüz dinc və xoş keçsin ✨",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Focus",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Diqqət vaxtı", color = Color.Gray, fontSize = 12.sp)
                        Text("$totalFocusMinutes dəq", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = surfaceBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, secondaryAccent.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Habits",
                            tint = secondaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Bugünkü vərdiş", color = Color.Gray, fontSize = 12.sp)
                        Text("$completedHabitsToday / ${habitsList.size}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getMoodEmoji(latestMood?.moodValue ?: 5f),
                            fontSize = 28.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Son Əhval-Ruhiyyə",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = latestMood?.note?.ifEmpty { "Qeyd yoxdur" } ?: "Hələ qeyd yoxdur",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.4f),
                                secondaryAccent.copy(alpha = 0.25f)
                            )
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Özünə Vaxt Ayır",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AuraLife sizə fərdi inkişafınız, diqqət toplamağınız və daxili dincliyiniz üçün hər gün fərdi köməkçi olacaq.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Tez Girişlər",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.setCurrentTab(AuraTab.FOCUS) },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Diqqət Qur", color = Color.White, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.setCurrentTab(AuraTab.MOOD_JOURNAL) },
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Əhvalı Yaz", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MoodJournalScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val moods by viewModel.moodEntries.collectAsStateWithLifecycle()

    var moodSliderValue by remember { mutableStateOf(5.0f) }
    var moodNoteText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    val tagsAvailable = listOf("İş", "Sağlıq", "Dincəlmək", "Ailə", "Hobbi", "İdman")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Əhval-Ruhiyyə Gündəliyi", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("İndiki hisslərinizi və düşüncələrinizi qeyd edin", color = Color.LightGray, fontSize = 13.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getMoodEmoji(moodSliderValue),
                            fontSize = 42.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getMoodDescription(moodSliderValue),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Xal: ${moodSliderValue.toInt()}/10",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = moodSliderValue,
                        onValueChange = { moodSliderValue = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("mood_slider")
                    )
                }
            }
        }

        item {
            Column {
                Text("Kateqoriya / Teqlər", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagsAvailable.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        AuraChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = tag,
                            activeColor = accentColor,
                            inactiveColor = surfaceBg
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = moodNoteText,
                        onValueChange = { moodNoteText = it },
                        placeholder = { Text("Fikirləriniz və qeydləriniz...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("mood_note_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = accentColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (moodNoteText.trim().isNotEmpty() || selectedTags.isNotEmpty()) {
                                viewModel.addMoodEntry(
                                    moodSliderValue,
                                    moodNoteText,
                                    selectedTags.toList()
                                )
                                moodSliderValue = 5.0f
                                moodNoteText = ""
                                selectedTags.clear()
                            } else {
                                viewModel.showToast("Zəhmət olmasa bir qeyd yazın!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_mood_button")
                    ) {
                        Text("Yadda saxla 💜", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Keçmiş Əhval Qeydləri",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (moods.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Hələ ki qeydiniz yoxdur. Birincisini indi yazın!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(moods) { entry ->
                MoodHistoryRow(entry, accentColor, surfaceBg) {
                    viewModel.deleteMoodEntry(entry)
                }
            }
        }
    }
}

@Composable
fun MoodHistoryRow(
    entry: MoodEntry,
    accentColor: Color,
    surfaceBg: Color,
    onDelete: () -> Unit
) {
    val dateText = remember(entry.timestamp) {
        val date = Date(entry.timestamp)
        val format = SimpleDateFormat("dd MMMM, HH:mm", Locale("az"))
        format.format(date)
    }

    val tags = remember(entry.tags) {
        entry.tags.split(",").filter { it.isNotEmpty() }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(getMoodEmoji(entry.moodValue), fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dateText, color = Color.Gray, fontSize = 11.sp)
                    Text("Xal: ${entry.moodValue.toInt()}/10", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (entry.note.isNotEmpty()) {
                    Text(entry.note, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tag, color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun HabitsScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val habitsList by viewModel.habits.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var habitNameInput by remember { mutableStateOf("") }
    var habitCategory by remember { mutableStateOf("Mind") }
    
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gündəlik Vərdişlər", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Kiçik vərdişlər, böyük uğurlar gətirir", color = Color.LightGray, fontSize = 13.sp)
                }
                
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_habit_trigger")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yeni", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (habitsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vərdişləriniz hələ yoxdur.", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showAddDialog = true }) {
                            Text("İlk vərdişini yarat 🌱", color = accentColor, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            items(habitsList) { habit ->
                HabitItemRow(habit, todayStr, accentColor, surfaceBg,
                    onToggle = { viewModel.toggleHabitCompletion(habit, todayStr) },
                    onDelete = { viewModel.deleteHabit(habit) }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = surfaceBg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)), RoundedCornerShape(20.dp)),
            title = {
                Text("Yeni Vərdiş", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = habitNameInput,
                        onValueChange = { habitNameInput = it },
                        label = { Text("Vərdişin adı", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_name_input")
                    )

                    Column {
                        Text("Kateqoriya", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Mind", "Fitness", "Work", "Other").forEach { cat ->
                                val isSelected = habitCategory == cat
                                AuraChip(
                                    selected = isSelected,
                                    onClick = { habitCategory = cat },
                                    label = cat,
                                    activeColor = accentColor,
                                    inactiveColor = Color.Black.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitNameInput.trim().isNotEmpty()) {
                            viewModel.addHabit(
                                habitNameInput,
                                habitCategory,
                                String.format("#%06X", (0xFFFFFF and accentColor.toArgb()))
                            )
                            habitNameInput = ""
                            showAddDialog = false
                        } else {
                            viewModel.showToast("Zəhmət olmasa bir ad yazın!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("submit_habit_button")
                ) {
                    Text("Yarat", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Ləğv et", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun HabitItemRow(
    habit: Habit,
    todayStr: String,
    accentColor: Color,
    surfaceBg: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompletedToday = remember(habit.completedDatesJson) {
        habit.completedDatesJson.split(",").contains(todayStr)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isCompletedToday) accentColor.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompletedToday) accentColor else Color.White.copy(alpha = 0.08f)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = if (isCompletedToday) Color.White else Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    color = if (isCompletedToday) Color.Gray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(habit.category, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥 ${habit.streak} gün", color = Color.LightGray, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆 Rekord: ${habit.maxStreak}", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun FocusTimerScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val totalMinutes by viewModel.focusTimerMinutes.collectAsStateWithLifecycle()
    val secondsRemaining by viewModel.focusTimeRemainingSeconds.collectAsStateWithLifecycle()
    val isRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
    val selectedSound by viewModel.selectedFocusSound.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedFocusCategory.collectAsStateWithLifecycle()

    val displayMin = secondsRemaining / 60
    val displaySec = secondsRemaining % 60
    val timeString = String.format("%02d:%02d", displayMin, displaySec)

    val progress = remember(secondsRemaining, totalMinutes) {
        val totalSec = totalMinutes * 60
        if (totalSec > 0) (secondsRemaining.toFloat() / totalSec) else 1f
    }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text("Dərin Fokus", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Musiqi və Taymer vasitəsilə tam diqqət toplayın", color = Color.LightGray, fontSize = 13.sp)
            }
        }

        item {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .drawBehind {
                        if (isRunning) {
                            drawCircle(
                                color = accentColor.copy(alpha = pulseAlpha),
                                radius = size.minDimension / 2f * pulseScale
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        style = Stroke(width = 12.dp.toPx())
                    )
                    
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isRunning) "DİQQƏT SEANSI" else "HAZIR",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fokus Müddəti: $totalMinutes dəq",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 15, 25, 45, 60).forEach { mins ->
                        val isSelected = totalMinutes == mins
                        ElevatedButton(
                            onClick = { if (!isRunning) viewModel.setFocusMinutes(mins) },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (isSelected) accentColor else surfaceBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            enabled = !isRunning
                        ) {
                            Text(
                                text = "${mins}d",
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Text("Fəaliyyət", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Study", "Work", "Meditation", "Relax").forEach { cat ->
                        val isSelected = selectedCategory == cat
                        AuraChip(
                            selected = isSelected,
                            onClick = { if (!isRunning) viewModel.setFocusCategory(cat) },
                            label = cat,
                            activeColor = accentColor,
                            inactiveColor = surfaceBg
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            ) {
                Text("Fon Səsləri (Prosedurol Sintez)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("None", "Rain", "Zen Waves", "White Noise", "Forest Wind").forEach { sound ->
                        val isSelected = selectedSound == sound
                        AuraChip(
                            selected = isSelected,
                            onClick = { viewModel.setFocusSound(sound) },
                            label = sound,
                            activeColor = secondaryAccent,
                            inactiveColor = surfaceBg
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetFocusTimer() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sıfırla", fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        if (isRunning) viewModel.pauseFocusTimer() else viewModel.startFocusTimer()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color.Red else accentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("play_pause_focus")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Warning else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Durdur" else "Başlat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InsightsScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val moods by viewModel.moodEntries.collectAsStateWithLifecycle()
    val habitsList by viewModel.habits.collectAsStateWithLifecycle()
    val sessions by viewModel.focusSessions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Vizual Analitika", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Daxili inkişafınızın fərdi qrafiki", color = Color.LightGray, fontSize = 13.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Həftəlik Əhval Dəyişimi",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (moods.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Qrafik yaratmaq üçün qeyd yoxdur.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            val reversedMoods = moods.take(7).reversed()
                            val pointsCount = reversedMoods.size
                            
                            if (pointsCount > 1) {
                                val width = size.width
                                val height = size.height
                                val path = Path()
                                val fillPath = Path()

                                val stepX = width / (pointsCount - 1)
                                
                                reversedMoods.forEachIndexed { idx, entry ->
                                    val valY = height - (height * 0.8f * (entry.moodValue - 1f) / 9f + height * 0.1f)
                                    val valX = idx * stepX

                                    if (idx == 0) {
                                        path.moveTo(valX, valY)
                                        fillPath.moveTo(valX, height)
                                        fillPath.lineTo(valX, valY)
                                    } else {
                                        val prevX = (idx - 1) * stepX
                                        val prevEntry = reversedMoods[idx - 1]
                                        val prevY = height - (height * 0.8f * (prevEntry.moodValue - 1f) / 9f + height * 0.1f)
                                        
                                        path.cubicTo(
                                            (prevX + valX) / 2f, prevY,
                                            (prevX + valX) / 2f, valY,
                                            valX, valY
                                        )
                                        fillPath.cubicTo(
                                            (prevX + valX) / 2f, prevY,
                                            (prevX + valX) / 2f, valY,
                                            valX, valY
                                        )
                                    }

                                    if (idx == pointsCount - 1) {
                                        fillPath.lineTo(valX, height)
                                        fillPath.close()
                                    }

                                    drawCircle(
                                        color = accentColor,
                                        radius = 4.dp.toPx(),
                                        center = Offset(valX, valY)
                                    )
                                }

                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                                    )
                                )

                                drawPath(
                                    path = path,
                                    color = accentColor,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            } else {
                                val singleY = size.height / 2f
                                drawCircle(
                                    color = accentColor,
                                    radius = 6.dp.toPx(),
                                    center = Offset(size.width / 2f, singleY)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, secondaryAccent.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vərdişlərin Davamlılığı",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (habitsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Vərdişləriniz hələ yoxdur.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        habitsList.take(4).forEach { habit ->
                            val completionsCount = remember(habit.completedDatesJson) {
                                habit.completedDatesJson.split(",").filter { it.isNotEmpty() }.size
                            }
                            
                            val completionRate = if (completionsCount > 0) {
                                (completionsCount.toFloat() / 30f).coerceIn(0.1f, 1.0f)
                            } else 0.02f

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(habit.name, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("$completionsCount tamamlanma", color = Color.Gray, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { completionRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = secondaryAccent,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fəaliyyətə Görə Fokus",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (sessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Diqqət seanslarınız yoxdur.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        val categoryMap = remember(sessions) {
                            val map = mutableMapOf("Study" to 0, "Work" to 0, "Meditation" to 0, "Relax" to 0)
                            sessions.forEach {
                                val current = map[it.category] ?: 0
                                map[it.category] = current + it.durationMinutes
                            }
                            map
                        }

                        val maxMinutes = categoryMap.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                        categoryMap.forEach { (cat, mins) ->
                            val percent = mins.toFloat() / maxMinutes.toFloat()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(80.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(percent.coerceAtLeast(0.05f))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(accentColor, secondaryAccent)
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("$mins dəq", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    accentColor: Color,
    secondaryAccent: Color,
    surfaceBg: Color
) {
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val volume by viewModel.soundVolume.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Tənzimləmələr", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("AuraLife fərdi dizayn və səs seçimləri", color = Color.LightGray, fontSize = 13.sp)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vizual Mövzular (Aura Palette)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuraTheme.values().forEach { theme ->
                            val isSelected = currentTheme == theme
                            val (c1, c2) = when (theme) {
                                AuraTheme.COSMIC_NIGHT -> listOf(Color(0xFF9D4EDD), Color(0xFF130E26))
                                AuraTheme.EMERALD_FOREST -> listOf(Color(0xFF2EC4B6), Color(0xFF0B2115))
                                AuraTheme.ROSE_QUARTZ -> listOf(Color(0xFFF72585), Color(0xFF261820))
                                AuraTheme.OCEAN_BREEZE -> listOf(Color(0xFF00B4D8), Color(0xFF0C243C))
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(c2)
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) c1 else Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setTheme(theme) }
                                    .padding(8.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(c1)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fon Səslərinin Səsi",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = accentColor)
                        Slider(
                            value = volume,
                            onValueChange = { viewModel.setSoundVolume(it) },
                            valueRange = 0f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text("${(volume * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceBg),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AuraLife Haqqında",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Versiya: 1.0.0 (Premium Elite)",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Prosedurol Ambient Sintez texnologiyası vasitəsilə oflayn və limitsiz səs dincəlməsi təklif edir.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

fun getMoodEmoji(value: Float): String {
    return when {
        value <= 2f -> "😭"
        value <= 4f -> "😔"
        value <= 6f -> "😐"
        value <= 8f -> "🙂"
        else -> "🥰"
    }
}

fun getMoodDescription(value: Float): String {
    return when {
        value <= 2f -> "Çox pis / Kədərli"
        value <= 4f -> "Yorğun / Kefsiz"
        value <= 6f -> "Normal / Neytral"
        value <= 8f -> "Yaxşı / Sakit"
        else -> "Mükəmməl / Xoşbəxt"
    }
}
