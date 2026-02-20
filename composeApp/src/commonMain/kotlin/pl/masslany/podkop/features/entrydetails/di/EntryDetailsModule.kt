package pl.masslany.podkop.features.entrydetails.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.entrydetails.EntryDetailsViewModel

val entryDetailsModule = module {
    viewModel { params ->
        EntryDetailsViewModel(
            id = params.get<Int>(),
            entriesRepository = get(),
            resourceItemStateHolder = get(),
            logger = get(),
            topBarActions = get(),
        )
    }
}
