package pl.masslany.podkop.features.links.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.links.LinksResourceItemStateHolder
import pl.masslany.podkop.features.links.LinksResourceItemStateHolderImpl
import pl.masslany.podkop.features.links.LinksViewModel

val linksModule = module {
    viewModel { params ->
        LinksViewModel(
            isUpcoming = params.get<Boolean>(),
            linksRepository = get(),
            hitsRepository = get(),
            linksResourceItemStateHolder = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
    factory<LinksResourceItemStateHolder> {
        LinksResourceItemStateHolderImpl(
            linksRepository = get(),
            entriesRepository = get(),
            appNavigator = get(),
            dispatcherProvider = get(),
            logger = get(),
        )
    }
}
