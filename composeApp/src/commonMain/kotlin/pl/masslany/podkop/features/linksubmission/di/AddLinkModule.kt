package pl.masslany.podkop.features.linksubmission.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.linksubmission.addlink.AddLinkStartViewModel
import pl.masslany.podkop.features.linksubmission.linkdraft.LinkDraftViewModel

val addLinkModule = module {
    viewModel {
        AddLinkStartViewModel(
            linksRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }

    viewModel { params ->
        LinkDraftViewModel(
            screen = params.get(),
            linksRepository = get(),
            mediaRepository = get(),
            tagsRepository = get(),
            appNavigator = get(),
            savedStateHandle = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
