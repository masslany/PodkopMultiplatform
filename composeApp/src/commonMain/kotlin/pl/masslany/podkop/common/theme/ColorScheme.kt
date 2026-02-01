package pl.masslany.podkop.common.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun provideColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme