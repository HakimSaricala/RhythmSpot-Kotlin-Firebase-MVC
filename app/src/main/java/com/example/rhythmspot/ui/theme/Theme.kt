package com.example.rhythmspot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ryhthmspot.ui.theme.bg
import com.example.ryhthmspot.ui.theme.components
import com.example.ryhthmspot.ui.theme.foreground
import com.example.ryhthmspot.ui.theme.textSecondary
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = components,
    secondary = foreground,
    tertiary = textSecondary,
    background = bg,
    surface = bg,
)

private val LightColorScheme = lightColorScheme(
    primary = components,
    secondary = foreground,
    tertiary = textSecondary,
    background = bg,
    surface = Color(0xFF31363F),

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
@Composable
fun RhythmSpotTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(color = Color.White)
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content

    )
}