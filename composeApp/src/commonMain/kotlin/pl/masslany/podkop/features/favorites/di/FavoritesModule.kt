package pl.masslany.podkop.features.favorites.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.favorites.FavoritesViewModel

val favoritesModule = module {
    viewModel {
        FavoritesViewModel(
            authRepository = get(),
            favouritesRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
