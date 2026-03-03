package pl.masslany.podkop.business.di

import org.koin.dsl.module
import pl.masslany.podkop.business.auth.data.di.authDataModule
import pl.masslany.podkop.business.embeds.data.di.twitterEmbedPreviewDataModule
import pl.masslany.podkop.business.embeds.data.network.di.twitterEmbedPreviewNetworkModule
import pl.masslany.podkop.business.entries.data.di.entriesDataModule
import pl.masslany.podkop.business.entries.data.network.di.entriesNetworkModule
import pl.masslany.podkop.business.favourites.data.di.favouritesDataModule
import pl.masslany.podkop.business.favourites.data.network.di.favouritesNetworkModule
import pl.masslany.podkop.business.hits.data.di.hitsDataModule
import pl.masslany.podkop.business.hits.data.network.di.hitsNetworkModule
import pl.masslany.podkop.business.links.data.di.linksDataModule
import pl.masslany.podkop.business.links.data.network.di.linksNetworkModule
import pl.masslany.podkop.business.media.data.di.mediaDataModule
import pl.masslany.podkop.business.media.data.network.di.mediaNetworkModule
import pl.masslany.podkop.business.profile.data.di.profileDataModule
import pl.masslany.podkop.business.profile.data.local.di.profileLocalModule
import pl.masslany.podkop.business.profile.data.network.di.profileNetworkModule
import pl.masslany.podkop.business.startup.infrastructure.di.startupModule
import pl.masslany.podkop.business.tags.data.di.tagsDataModule
import pl.masslany.podkop.business.tags.data.network.di.tagsNetworkModule
import pl.masslany.podkop.commonModule

val businessModule = module {
    includes(
        commonModule,
        linksDataModule,
        linksNetworkModule,
        authDataModule,
        twitterEmbedPreviewDataModule,
        twitterEmbedPreviewNetworkModule,
        hitsDataModule,
        hitsNetworkModule,
        entriesDataModule,
        entriesNetworkModule,
        favouritesDataModule,
        favouritesNetworkModule,
        profileDataModule,
        profileNetworkModule,
        profileLocalModule,
        mediaDataModule,
        mediaNetworkModule,
        tagsDataModule,
        tagsNetworkModule,
        startupModule,
    )
}
