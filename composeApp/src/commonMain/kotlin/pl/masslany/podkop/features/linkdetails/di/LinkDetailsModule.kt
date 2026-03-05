package pl.masslany.podkop.features.linkdetails.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.linkdetails.LinkDetailsViewModel
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

val linkDetailsModule = module {
    viewModel { params ->
        LinkDetailsViewModel(
            id = params.get<Int>(),
            linksRepository = get(),
            authRepository = get(),
            profileRepository = get(),
            resourceItemStateHolder = get<ResourceItemStateHolder>(),
            appNavigator = get(),
            screenshotShareDraftStore = get(),
            resourceActionUpdatesStore = get(),
            topBarActions = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
