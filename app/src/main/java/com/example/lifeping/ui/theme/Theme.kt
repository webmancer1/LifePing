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
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = TertiaryAmber,
    onTertiary = OnTertiaryAmber, 
    background = DarkBackground,
    onBackground = NeutralWhite,
    surface = DarkSurface,
    onSurface = NeutralWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralTextSecondary,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryIndigo,
    primaryContainer = PrimaryContainerIndigo,
    onPrimaryContainer = OnPrimaryContainerIndigo,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryAmber,
    onTertiary = OnTertiaryAmber,
    tertiaryContainer = TertiaryContainerAmber,
    onTertiaryContainer = OnTertiaryContainerAmber,
    background = NeutralBackground,
    onBackground = NeutralTextPrimary,
    surface = NeutralSurface,
    onSurface = NeutralTextPrimary,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralTextSecondary,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

@Composable
fun LifePingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
        shapes = Shapes,
        content = {
            content()
            android.util.Log.d("ThemeDebug", "LifePingTheme: Content composed in ${System.currentTimeMillis() - startTime}ms")
        }
    )
}