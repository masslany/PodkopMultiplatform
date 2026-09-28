package pl.masslany.podkop.features.resources.di

import org.koin.dsl.module
import pl.masslany.podkop.features.resources.BaseResourceItemStateHolder
import pl.masslany.podkop.features.resources.ResourceItemStateHolder
import pl.masslany.podkop.features.resources.StreamableEmbedPlayback

val resourcesModule = module {
    factory {
        StreamableEmbedPlayback(
            appSettings = get(),
            streamableVideoRepository = get(),
            logger = get(),
        )
    }
    factory<ResourceItemStateHolder> {
        BaseResourceItemStateHolder(
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
        )
    }
}
