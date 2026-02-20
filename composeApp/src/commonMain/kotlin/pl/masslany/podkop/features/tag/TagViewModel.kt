package pl.masslany.podkop.features.tag

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.features.topbar.TopBarActions

class TagViewModel(tag: String, topBarActions: TopBarActions) :
    ViewModel(),
    TagActions,
    TopBarActions by topBarActions {

    private val _state = MutableStateFlow(TagScreenState.initial)
    val state = _state.asStateFlow()

    init {
        _state.update { previousState ->
            previousState.copy(tag = tag)
        }
    }
}
