package pl.masslany.podkop.features.hits.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.hits.HitsViewModel

val hitsModule = module {
    viewModel {
        HitsViewModel(
            hitsRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
