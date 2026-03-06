package pl.masslany.podkop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pl.masslany.podkop.business.di.businessModule
import pl.masslany.podkop.common.deeplink.di.deepLinkModule
import pl.masslany.podkop.common.navigation.di.navigationModule
import pl.masslany.podkop.common.settings.AppSettings
import pl.masslany.podkop.common.settings.AppSettingsImpl
import pl.masslany.podkop.common.snackbar.SnackbarManager
import pl.masslany.podkop.common.snackbar.SnackbarManagerImpl
import pl.masslany.podkop.features.about.di.aboutAppModule
import pl.masslany.podkop.features.composer.di.composerModule
import pl.masslany.podkop.features.debug.di.debugModule
import pl.masslany.podkop.features.entries.di.entriesModule
import pl.masslany.podkop.features.entrydetails.di.entryDetailsModule
import pl.masslany.podkop.features.favorites.di.favoritesModule
import pl.masslany.podkop.features.hits.di.hitsModule
import pl.masslany.podkop.features.home.di.homeModule
import pl.masslany.podkop.features.imageviewer.di.imageViewerModule
import pl.masslany.podkop.features.linkdetails.di.linkDetailsModule
import pl.masslany.podkop.features.links.di.linksModule
import pl.masslany.podkop.features.more.di.moreModule
import pl.masslany.podkop.features.profile.di.profileModule
import pl.masslany.podkop.features.resourceactions.di.resourceActionsModule
import pl.masslany.podkop.features.resources.di.resourcesModule
import pl.masslany.podkop.features.search.di.searchModule
import pl.masslany.podkop.features.settings.di.settingsModule
import pl.masslany.podkop.features.tag.di.tagModule
import pl.masslany.podkop.features.topbar.di.topBarModule

val composeAppModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single<SnackbarManager> { SnackbarManagerImpl() }
    single<AppSettings> { AppSettingsImpl(keyValueStorage = get()) }
    viewModelOf(::AppViewModel)

    includes(
        navigationModule,
        deepLinkModule,
        aboutAppModule,
        homeModule,
        hitsModule,
        favoritesModule,
        linksModule,
        entriesModule,
        composerModule,
        resourcesModule,
        debugModule,
        entryDetailsModule,
        linkDetailsModule,
        imageViewerModule,
        profileModule,
        moreModule,
        resourceActionsModule,
        searchModule,
        settingsModule,
        tagModule,
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
