package pl.masslany.podkop.features.entries.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.entries.EntriesViewModel

val entriesModule = module {
    viewModel {
        EntriesViewModel(
            entriesRepository = get(),
            resourceItemStateHolder = get(),
            topBarActions = get(),
        )
    }
}
