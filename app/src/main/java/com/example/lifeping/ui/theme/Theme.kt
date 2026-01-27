package com.example.lifeping.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = BackgroundGray,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.White

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
fun LifePingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            android.util.Log.d("ThemeDebug", "LifePingTheme: Using DarkColorScheme")
            DarkColorScheme
        }
        else -> {
            android.util.Log.d("ThemeDebug", "LifePingTheme: Using LightColorScheme")
            LightColorScheme
        }
    }

    val startTime = System.currentTimeMillis()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            content()
            android.util.Log.d("ThemeDebug", "LifePingTheme: Content composed in ${System.currentTimeMillis() - startTime}ms")
        }
    )
}