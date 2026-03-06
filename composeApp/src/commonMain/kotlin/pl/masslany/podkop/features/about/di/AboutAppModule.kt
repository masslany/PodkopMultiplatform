package pl.masslany.podkop.features.about.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.about.AboutAppViewModel

val aboutAppModule = module {
    viewModel {
        AboutAppViewModel(
            appNavigator = get(),
            buildInfo = get(),
        )
    }
}
