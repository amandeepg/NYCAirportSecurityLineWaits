package ca.amandeep.nycairportsecuritylinewaits.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme()

@Composable
fun NYCAirportSecurityLineWaitsTheme(
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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        /* getting the current window by tapping into the Activity */
        val currentWindow = (view.context as? Activity)?.window
            ?: throw Exception("Not in an activity - unable to get Window reference")

        SideEffect {
            /* the default code did the same cast here - might as well use our new variable! */
            currentWindow.statusBarColor = colorScheme.primary.toArgb()
            /* accessing the insets controller to change appearance of the status bar, with 100% less deprecation warnings */
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars =
                darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        androidx.compose.material.MaterialTheme(
            colors = androidx.compose.material.MaterialTheme.colors.copy(
                primary = colorScheme.primary,
                primaryVariant = colorScheme.primary,
                secondary = colorScheme.secondary,
                secondaryVariant = colorScheme.secondary,
                background = colorScheme.background,
                surface = colorScheme.surface,
                error = colorScheme.error,
                onPrimary = colorScheme.onPrimary,
                onSecondary = colorScheme.onSecondary,
                onBackground = colorScheme.onBackground,
                onSurface = colorScheme.onSurface,
                onError = colorScheme.onError,
                isLight = !darkTheme
            )
        ) {
            content()
        }
    }
}

@Composable
fun Card3(
    modifier: Modifier = Modifier,
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = elevation,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp),
    ) {
        ProvideTextStyle(TextStyle(color = MaterialTheme.colorScheme.onSurface)) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                content()
            }
        }
    }
}