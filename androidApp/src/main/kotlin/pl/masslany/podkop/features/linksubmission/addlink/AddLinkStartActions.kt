package pl.masslany.podkop.features.linksubmission.addlink

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
internal interface AddLinkStartActions : TopBarActions {
    fun onUrlChanged(value: String)
    fun onContinueClicked()
    fun onSavedDraftContinueClicked(key: String)
    fun onSavedDraftDeleteClicked(key: String)
}
