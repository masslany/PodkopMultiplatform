package pl.masslany.podkop.features.resources.di

import org.koin.dsl.module
import pl.masslany.podkop.features.resources.BaseResourceItemStateItemStateHolder
import pl.masslany.podkop.features.resources.ResourceItemStateHolder

val resourcesModule = module {
    factory<ResourceItemStateHolder> {
        BaseResourceItemStateItemStateHolder(
            linksRepository = get(),
            appNavigator = get(),
        )
    }
}