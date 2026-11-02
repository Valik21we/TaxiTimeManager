package com.taximanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TaxiYellow = Color(0xFFFFC107)
val TaxiYellowDark = Color(0xFFFFA000)
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val InfoBlue = Color(0xFF2196F3)
val DangerRed = Color(0xFFF44336)
val SuccessGreen = Color(0xFF4CAF50)

private val DarkColors = darkColorScheme(
    primary = TaxiYellow, onPrimary = Color.Black,
    secondary = InfoBlue, background = DarkBg,
    surface = DarkSurface, error = DangerRed
)
private val LightColors = lightColorScheme(
    primary = TaxiYellowDark, onPrimary = Color.Black,
    secondary = InfoBlue, error = DangerRed
)

@Composable
fun TaxiManagerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
