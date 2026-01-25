package pl.masslany.podkop.features.links.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.links.LinksViewModel

val linksModule = module {
    viewModel { params ->
        LinksViewModel(
            isUpcoming = params.get<Boolean>(),
            linksRepository = get(),
            resourceItemStateHolder = get(),
        )
    }
}