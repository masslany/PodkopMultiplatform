package pl.masslany.podkop.features.about

import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.features.about.generated.GeneratedOpenSourceLibraries

class AboutAppViewModel(private val appNavigator: AppNavigator, buildInfo: BuildInfo) :
    ViewModel(),
    AboutAppActions {

    private val _state = MutableStateFlow(AboutAppScreenState.initial)
    val state = _state.asStateFlow()

    init {
        _state.update {
            AboutAppScreenState(
                appVersion = buildInfo.appVersionName,
                libraries = GeneratedOpenSourceLibraries.toPersistentList(),
            )
        }
    }

    override fun onCloseClicked() {
        appNavigator.back()
    }

    override fun onOpenLinkClicked(url: String) {
        if (url.isBlank()) return
        appNavigator.openExternalLink(url)
    }
}
