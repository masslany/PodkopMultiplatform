package pl.masslany.podkop.features.search.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.search.SearchViewModel

val searchModule = module {
    viewModel {
        SearchViewModel(
            tagsRepository = get(),
            profileRepository = get(),
            authRepository = get(),
            appNavigator = get(),
            logger = get(),
            savedStateHandle = get(),
            topBarActions = get(),
        )
    }
}
