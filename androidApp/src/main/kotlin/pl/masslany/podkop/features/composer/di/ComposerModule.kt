package pl.masslany.podkop.features.composer.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.composer.ComposerBottomSheetViewModel

val composerModule = module {
    viewModel { params ->
        ComposerBottomSheetViewModel(
            screen = params.get(),
            entriesRepository = get(),
            linksRepository = get(),
            mediaRepository = get(),
            appNavigator = get(),
            savedStateHandle = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
