package pl.masslany.podkop.features.links.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import pl.masslany.podkop.features.links.LinkHits
import pl.masslany.podkop.features.links.LinksResourceItemStateHolder
import pl.masslany.podkop.features.links.LinksResourceItemStateHolderImpl
import pl.masslany.podkop.features.links.LinksViewModel
import pl.masslany.podkop.features.resources.ResourceItemStateHolderImpl

val linksModule = module {
    viewModel { params ->
        val isUpcoming = params.get<Boolean>()
        LinksViewModel(
            isUpcoming = isUpcoming,
            authRepository = get(),
            linksRepository = get(),
            hitsRepository = get(),
            notificationsRepository = get(),
            linksResourceItemStateHolder = get { parametersOf(isUpcoming) },
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
            topBarActions = get(),
        )
    }
    factory<LinksResourceItemStateHolder> { params ->
        val linkHits = LinkHits()
        LinksResourceItemStateHolderImpl(
            linkHits = linkHits,
            holder = ResourceItemStateHolderImpl(
                linksRepository = get(),
                entriesRepository = get(),
                favouritesRepository = get(),
                appNavigator = get(),
                dispatcherProvider = get(),
                logger = get(),
                twitterEmbedPreviewRepository = get(),
                streamableEmbedPlayback = get(),
                screenshotShareDraftStore = get(),
                resourceActionUpdatesStore = get(),
                isUpcoming = params.get<Boolean>(),
                onItemRefreshed = linkHits::refresh,
            ),
        )
    }
}
