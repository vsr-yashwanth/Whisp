package com.example.offlinechat.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    secondary = TitaniumLight,
    background = ObsidianBlack,
    surface = SurfaceDark,
    surfaceVariant = SurfaceElevated,
    onPrimary = ObsidianBlack,
    onSecondary = ObsidianBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorderSubtle,
    error = ErrorMuted
)

@Composable
fun OfflineChatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = AppDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
