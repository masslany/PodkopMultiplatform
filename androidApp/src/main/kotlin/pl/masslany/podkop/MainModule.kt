package pl.masslany.podkop

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::MainActivityViewModel)
}