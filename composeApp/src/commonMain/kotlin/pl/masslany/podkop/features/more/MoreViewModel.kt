package pl.masslany.podkop.features.more

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MoreViewModel :
    ViewModel(),
    MoreActions {
    private val _state = MutableStateFlow(MoreScreenState.initial)
    val state = _state.asStateFlow()
}
