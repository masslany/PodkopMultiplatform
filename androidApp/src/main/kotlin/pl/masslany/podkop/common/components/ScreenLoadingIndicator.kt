package pl.masslany.podkop.common.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Material 3 Expressive shape-morphing indicator for screen-level waits; inline spinners stay circular. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScreenLoadingIndicator(modifier: Modifier = Modifier) {
    LoadingIndicator(modifier = modifier)
}
