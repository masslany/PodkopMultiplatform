package pl.masslany.podkop.features.linkdetails.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.linkdetails.LinkDetailsViewModel

val linkDetailsModule = module {
    viewModel { params ->
        LinkDetailsViewModel(
            id = params.get<Int>(),
            topBarActions = get(),
        )
    }
}
