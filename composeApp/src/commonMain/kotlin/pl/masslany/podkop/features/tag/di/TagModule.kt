package pl.masslany.podkop.features.tag.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.tag.TagViewModel

val tagModule = module {
    viewModel { params ->
        TagViewModel(
            tag = params.get<String>(),
            topBarActions = get(),
        )
    }
}
