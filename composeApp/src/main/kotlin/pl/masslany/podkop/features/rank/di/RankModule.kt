package pl.masslany.podkop.features.rank.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.rank.RankViewModel

val rankModule = module {
    viewModel {
        RankViewModel(
            rankRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
}
