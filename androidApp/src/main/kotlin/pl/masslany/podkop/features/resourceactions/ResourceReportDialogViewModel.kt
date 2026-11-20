package pl.masslany.podkop.features.resourceactions

import androidx.lifecycle.ViewModel
import pl.masslany.podkop.R
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.snackbar.SnackbarEvent
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarMessage

interface ResourceReportDialogActions {
    fun onOpenContentClicked()
    fun onLinkCopied()
    fun onContactClicked()
    fun onDismissClicked()
}

class ResourceReportDialogViewModel(
    private val contentUrl: String,
    private val appNavigator: AppNavigator,
    private val snackbarManager: SnackbarManager,
) : ViewModel(),
    ResourceReportDialogActions {

    override fun onOpenContentClicked() {
        appNavigator.openExternalLink(contentUrl)
    }

    override fun onLinkCopied() {
        snackbarManager.tryEmit(
            SnackbarEvent(
                message = SnackbarMessage.Resource(R.string.snackbar_link_copied),
            ),
        )
    }

    override fun onContactClicked() {
        appNavigator.openExternalLink(CONTACT_URL)
    }

    override fun onDismissClicked() {
        appNavigator.back()
    }

    internal companion object {
        const val CONTACT_URL = "https://wykop.pl/kontakt"
    }
}
