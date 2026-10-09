package pl.masslany.podkop.test

import org.koin.dsl.module
import pl.masslany.podkop.common.network.infrastructure.main.NetworkConfig

fun integrationTestModule(baseUrl: String) = module {
    single {
        NetworkConfig(baseUrl = baseUrl)
    }
}
