package pl.masslany.podkop.features.resources.di

import org.koin.dsl.module
import pl.masslany.podkop.features.resources.BaseResourceItemStateHolder
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

val resourcesModule = module {
    factory<ResourceItemStateHolder> {
        BaseResourceItemStateHolder(
            linksRepository = get(),
            entriesRepository = get(),
            favouritesRepository = get(),
            appNavigator = get(),
            dispatcherProvider = get(),
            logger = get(),
            twitterEmbedPreviewRepository = get(),
            screenshotShareDraftStore = get(),
        )
    }
}
