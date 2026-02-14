package pl.masslany.podkop.features.home.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.masslany.podkop.features.home.HomeNavigator
import pl.masslany.podkop.features.home.HomeViewModel

val homeModule = module {
    factory { HomeNavigator(configProvider = get()) }
    viewModelOf(::HomeViewModel)
}
