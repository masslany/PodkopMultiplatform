package pl.masslany.podkop.features.bottombar.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import pl.masslany.podkop.features.bottombar.BottomBarViewModel

val bottomBarModule = module {
    viewModelOf(::BottomBarViewModel)
}