package pl.masslany.podkop.features.observed.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.observed.ObservedViewModel

val observedModule = module {
    viewModel {
        ObservedViewModel(
            observedRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
