package pl.masslany.podkop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pl.masslany.podkop.business.di.businessModule
import pl.masslany.podkop.common.navigation.di.navigationModule
import pl.masslany.podkop.features.bottombar.di.bottomBarModule
import pl.masslany.podkop.features.links.di.linksModule

val composeAppModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    includes(
        navigationModule,
        bottomBarModule,
        linksModule,
    )
}

fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
) {
    startKoin {
        appDeclaration()
        modules(
            businessModule,
            composeAppModule,
        )
    }
}