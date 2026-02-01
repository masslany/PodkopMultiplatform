package pl.masslany.podkop.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

internal val lightColorScheme = lightColorScheme(
    primary = lightPrimary,
    onPrimary = lightOnPrimary,
    primaryContainer = lightPrimaryContainer,
    onPrimaryContainer = lightOnPrimaryContainer,
    secondary = lightSecondary,
    onSecondary = lightOnSecondary,
    secondaryContainer = lightSecondaryContainer,
    onSecondaryContainer = lightOnSecondaryContainer,
    tertiary = lightTertiary,
    onTertiary = lightOnTertiary,
    tertiaryContainer = lightTertiaryContainer,
    onTertiaryContainer = lightOnTertiaryContainer,
    error = lightError,
    onError = lightOnError,
    errorContainer = lightErrorContainer,
    onErrorContainer = lightOnErrorContainer,
    outline = lightOutline,
    background = lightBackground,
    onBackground = lightOnBackground,
    surface = lightSurface,
    onSurface = lightOnSurface,
    surfaceVariant = lightSurfaceVariant,
    onSurfaceVariant = lightOnSurfaceVariant,
    inverseSurface = lightInverseSurface,
    inverseOnSurface = lightInverseOnSurface,
    inversePrimary = lightInversePrimary,
    surfaceTint = lightSurfaceTint,
    outlineVariant = lightOutlineVariant,
    scrim = lightScrim,
)

internal val darkColorScheme = darkColorScheme(
    primary = darkPrimary,
    onPrimary = darkOnPrimary,
    primaryContainer = darkPrimaryContainer,
    onPrimaryContainer = darkOnPrimaryContainer,
    secondary = darkSecondary,
    onSecondary = darkOnSecondary,
    secondaryContainer = darkSecondaryContainer,
    onSecondaryContainer = darkOnSecondaryContainer,
    tertiary = darkTertiary,
    onTertiary = darkOnTertiary,
    tertiaryContainer = darkTertiaryContainer,
    onTertiaryContainer = darkOnTertiaryContainer,
    error = darkError,
    onError = darkOnError,
    errorContainer = darkErrorContainer,
    onErrorContainer = darkOnErrorContainer,
    outline = darkOutline,
    background = darkBackground,
    onBackground = darkOnBackground,
    surface = darkSurface,
    onSurface = darkOnSurface,
    surfaceVariant = darkSurfaceVariant,
    onSurfaceVariant = darkOnSurfaceVariant,
    inverseSurface = darkInverseSurface,
    inverseOnSurface = darkInverseOnSurface,
    inversePrimary = darkInversePrimary,
    surfaceTint = darkSurfaceTint,
    outlineVariant = darkOutlineVariant,
    scrim = darkScrim,
)

@Composable
fun PodkopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = provideColorScheme(darkTheme, dynamicColor)

    val colorsPalette = if (darkTheme) {
        DarkColorsPalette
    } else {
        LightColorsPalette
    }

    CompositionLocalProvider(
        LocalColorsPalette provides colorsPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
