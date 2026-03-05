package pl.masslany.podkop.common.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.staticCompositionLocalOf

@OptIn(ExperimentalMaterial3Api::class)
val LocalBottomSheetState = staticCompositionLocalOf<SheetState?> { null }
