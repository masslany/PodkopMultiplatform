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
            authRepository = get(),
            linksRepository = get(),
            hitsRepository = get(),
            notificationsRepository = get(),
            linksResourceItemStateHolder = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
    factory<LinksResourceItemStateHolder> {
        LinksResourceItemStateHolderImpl(
            linksRepository = get(),
            entriesRepository = get(),
            favouritesRepository = get(),
            appNavigator = get(),
            dispatcherProvider = get(),
            logger = get(),
            twitterEmbedPreviewRepository = get(),
            screenshotShareDraftStore = get(),
            resourceActionUpdatesStore = get(),
        )
    }
}
