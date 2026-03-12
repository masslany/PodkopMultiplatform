package pl.masslany.podkop.features.debug.preview

import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.debug.DebugActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpDebugActions : DebugActions, TopBarActions by NoOpTopBarActions {
    override fun onEntryIdChanged(value: String) = Unit
    override fun onOpenEntryClicked() = Unit
    override fun onLinkIdChanged(value: String) = Unit
    override fun onOpenLinkClicked() = Unit
    override fun onSendPrivateMessagesNotificationClicked() = Unit
    override fun onSendSnackbarClicked() = Unit
}
