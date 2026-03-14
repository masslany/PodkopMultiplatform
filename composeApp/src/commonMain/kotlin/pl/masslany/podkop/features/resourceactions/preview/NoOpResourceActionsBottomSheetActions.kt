package pl.masslany.podkop.features.resourceactions.preview

import pl.masslany.podkop.features.resourceactions.ResourceActionId
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetActions

object NoOpResourceActionsBottomSheetActions : ResourceActionsBottomSheetActions {
    override fun onActionClicked(actionId: ResourceActionId) = Unit
}
