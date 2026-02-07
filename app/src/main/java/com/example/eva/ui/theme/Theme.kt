package com.eva.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/*
================================================================================
EVA THEME - JETPACK COMPOSE THEME
================================================================================

PURPOSE:
    This file defines the visual theme for the app using Jetpack Compose.
    It specifies colors, typography, and shapes used throughout the UI.

MATERIAL DESIGN 3:
    We use Material Design 3 (Material You) which provides:
    - Dynamic color support
    - Modern component designs
    - Accessibility features

EVA'S DESIGN:
    - Dark theme by default
    - Blue primary color (#3B82F6)
    - Purple secondary color (#8B5CF6)
    - Dark gray backgrounds

================================================================================
*/

/*
================================================================================
COLOR DEFINITIONS
================================================================================
*/

// Primary colors - Blue
private val EvaPrimary = Color(0xFF3B82F6)
private val EvaPrimaryDark = Color(0xFF2563EB)
private val EvaPrimaryLight = Color(0xFF60A5FA)

// Secondary colors - Purple
private val EvaSecondary = Color(0xFF8B5CF6)
private val EvaSecondaryDark = Color(0xFF7C3AED)

// Background colors - Dark grays
private val EvaBackground = Color(0xFF111827)
private val EvaSurface = Color(0xFF1F2937)
private val EvaSurfaceVariant = Color(0xFF374151)

// Text colors
private val EvaOnPrimary = Color.White
private val EvaOnBackground = Color.White
private val EvaOnSurface = Color.White
private val EvaOnSurfaceVariant = Color(0xFF9CA3AF)

// Error color
private val EvaError = Color(0xFFEF4444)

/*
================================================================================
DARK COLOR SCHEME
================================================================================
*/

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = EvaPrimary,
    onPrimary = EvaOnPrimary,
    primaryContainer = EvaPrimaryDark,
    onPrimaryContainer = EvaOnPrimary,

    // Secondary
    secondary = EvaSecondary,
    onSecondary = EvaOnPrimary,
    secondaryContainer = EvaSecondaryDark,
    onSecondaryContainer = EvaOnPrimary,

    // Background
    background = EvaBackground,
    onBackground = EvaOnBackground,

    // Surface
    surface = EvaSurface,
    onSurface = EvaOnSurface,
    surfaceVariant = EvaSurfaceVariant,
    onSurfaceVariant = EvaOnSurfaceVariant,

    // Error
    error = EvaError,
    onError = EvaOnPrimary
)

/*
================================================================================
THEME COMPOSABLE
================================================================================
*/

/**
 * Eva app theme wrapper.
 *
 * Wraps content in MaterialTheme with Eva's custom colors.
 *
 * USAGE:
 *     EvaTheme {
 *         // Your composables here
 *         Text("Hello", color = MaterialTheme.colorScheme.primary)
 *     }
 *
 * PARAMETERS:
 *     darkTheme - Whether to use dark theme (default: always dark)
 *     content - The composable content to wrap
 */
@Composable
fun EvaTheme(
    darkTheme: Boolean = true, // Eva is always dark theme
    content: @Composable () -> Unit
) {
    // Always use dark color scheme for Eva
    val colorScheme = DarkColorScheme

    // Get the current view for system bar configuration
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Get the activity's window
            val window = (view.context as Activity).window

            // Set status bar color to match background
            window.statusBarColor = colorScheme.background.toArgb()

            // Set navigation bar color
            window.navigationBarColor = colorScheme.background.toArgb()

            // Use light status bar icons (because background is dark)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // Apply Material Theme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}