package pl.masslany.podkop.business.di

import org.koin.dsl.module
import pl.masslany.podkop.business.auth.data.di.authDataModule
import pl.masslany.podkop.business.entries.data.di.entriesDataModule
import pl.masslany.podkop.business.entries.data.network.di.entriesNetworkModule
import pl.masslany.podkop.business.hits.data.di.hitsDataModule
import pl.masslany.podkop.business.hits.data.network.di.hitsNetworkModule
import pl.masslany.podkop.business.links.data.di.linksDataModule
import pl.masslany.podkop.business.links.data.network.di.linksNetworkModule
import pl.masslany.podkop.business.startup.infrastructure.di.startupModule
import pl.masslany.podkop.commonModule

val businessModule = module {
    includes(
        commonModule,
        linksDataModule,
        linksNetworkModule,
        authDataModule,
        hitsDataModule,
        hitsNetworkModule,
        entriesDataModule,
        entriesNetworkModule,
        startupModule,
    )
}
