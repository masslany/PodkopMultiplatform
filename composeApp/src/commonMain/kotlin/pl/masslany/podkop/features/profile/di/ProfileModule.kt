package pl.masslany.podkop.features.profile.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.profile.ProfileViewModel

val profileModule = module {
    viewModel { params ->
        ProfileViewModel(
            username = params.getOrNull<String>(),
            configStorage = get(),
            authRepository = get(),
            appNavigator = get(),
            topBarActions = get(),
        )
    }
}
