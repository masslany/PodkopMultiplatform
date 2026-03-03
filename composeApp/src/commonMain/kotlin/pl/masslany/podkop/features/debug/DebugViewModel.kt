package pl.masslany.podkop.features.debug

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.topbar.TopBarActions

class DebugViewModel(
    private val appNavigator: AppNavigator,
    private val buildInfo: BuildInfo,
    topBarActions: TopBarActions,
) : ViewModel(),
    DebugActions,
    TopBarActions by topBarActions {

    private val _state = MutableStateFlow(DebugScreenState.initial)
    val state = _state.asStateFlow()

    init {
        if (!buildInfo.isDebugBuild) {
            appNavigator.back()
        }
    }

    override fun onEntryIdChanged(value: String) {
        _state.update {
            it.copy(
                entryIdInput = value,
                isEntryIdInvalid = false,
            )
        }
    }

    override fun onOpenEntryClicked() {
        val entryId = state.value.entryIdInput.toIntOrNull()
        if (entryId == null || entryId <= 0) {
            _state.update { it.copy(isEntryIdInvalid = true) }
            return
        }

        appNavigator.navigateTo(EntryDetailsScreen.forEntry(id = entryId))
    }

    override fun onLinkIdChanged(value: String) {
        _state.update {
            it.copy(
                linkIdInput = value,
                isLinkIdInvalid = false,
            )
        }
    }

    override fun onOpenLinkClicked() {
        val linkId = state.value.linkIdInput.toIntOrNull()
        if (linkId == null || linkId <= 0) {
            _state.update { it.copy(isLinkIdInvalid = true) }
            return
        }

        appNavigator.navigateTo(LinkDetailsScreen(id = linkId))
    }
}
