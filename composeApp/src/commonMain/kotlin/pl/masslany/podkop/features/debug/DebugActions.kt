package pl.masslany.podkop.features.debug

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface DebugActions : TopBarActions {
    fun onEntryIdChanged(value: String)

    fun onOpenEntryClicked()

    fun onLinkIdChanged(value: String)

    fun onOpenLinkClicked()

    fun onSendPrivateMessagesNotificationClicked()

    fun onSendSnackbarClicked()
}
