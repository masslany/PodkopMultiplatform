package pl.masslany.podkop

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import pl.masslany.podkop.business.di.businessModule

fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
) {
    startKoin {
        appDeclaration()
        modules(
            businessModule
        )
    }
}