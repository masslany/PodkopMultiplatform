package pl.masslany.podkop.features.linkdetails

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.masslany.podkop.features.topbar.TopBarActions

class LinkDetailsViewModel(
    id: Int,
    topBarActions: TopBarActions,
) : ViewModel(),
    LinkDetailsActions,
    TopBarActions by topBarActions {

    private val _state = MutableStateFlow(
        LinkDetailsScreenState.initial.copy(
            id = id,
        ),
    )
    val state = _state.asStateFlow()
}
