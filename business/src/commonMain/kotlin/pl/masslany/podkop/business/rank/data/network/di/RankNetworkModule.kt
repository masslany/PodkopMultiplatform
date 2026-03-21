package pl.masslany.podkop.business.rank.data.network.di

import org.koin.dsl.module
import pl.masslany.podkop.business.rank.data.api.RankDataSource
import pl.masslany.podkop.business.rank.data.network.api.RankApi
import pl.masslany.podkop.business.rank.data.network.client.RankApiClient
import pl.masslany.podkop.business.rank.data.network.main.RankDataSourceImpl

val rankNetworkModule = module {
    single<RankApi> { RankApiClient(get()) }
    single<RankDataSource> { RankDataSourceImpl(get()) }
}
