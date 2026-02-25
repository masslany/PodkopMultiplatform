package pl.masslany.podkop.features.resourceactions.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetViewModel
import pl.masslany.podkop.features.resourceactions.ResourceActionsParams
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetViewModel
import pl.masslany.podkop.features.resourceactions.ResourceVotesParams

val resourceActionsModule = module {
    viewModel { params ->
        ResourceActionsBottomSheetViewModel(
            params = params.get<ResourceActionsParams>(),
            appNavigator = get(),
            snackbarManager = get(),
        )
    }

    viewModel { params ->
        ResourceVotesBottomSheetViewModel(
            params = params.get<ResourceVotesParams>(),
            entriesRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
