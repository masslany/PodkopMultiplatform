package pl.masslany.podkop.features.debug.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.debug.DebugViewModel

val debugModule = module {
    viewModel {
        DebugViewModel(
            appNavigator = get(),
            topBarActions = get(),
        )
    }
}
