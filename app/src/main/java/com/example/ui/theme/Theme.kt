package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BloodRed,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DeepBlack,
    surface = SurvivalGray,
    onPrimary = DeepBlack,
    onSecondary = DeepBlack,
    onBackground = BloodRed,
    onSurface = BloodRed
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
      colorScheme = DarkColorScheme,
      typography = Typography,
      content = content
  )
}
