package pl.masslany.podkop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pl.masslany.podkop.business.di.businessModule
import pl.masslany.podkop.common.navigation.di.navigationModule
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarManagerImpl
import pl.masslany.podkop.features.entries.di.entriesModule
import pl.masslany.podkop.features.entrydetails.di.entryDetailsModule
import pl.masslany.podkop.features.home.di.homeModule
import pl.masslany.podkop.features.imageviewer.di.imageViewerModule
import pl.masslany.podkop.features.links.di.linksModule
import pl.masslany.podkop.features.resources.di.resourcesModule
import pl.masslany.podkop.features.topbar.di.topBarModule

val composeAppModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single<SnackbarManager> { SnackbarManagerImpl() }

    includes(
        navigationModule,
        homeModule,
        linksModule,
        entriesModule,
        resourcesModule,
        entryDetailsModule,
        imageViewerModule,
        topBarModule,
    )
}

fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
) {
    startKoin {
        appDeclaration()
        modules(
            businessModule,
            composeAppModule,
        )
    }
}
