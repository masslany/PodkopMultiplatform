package pl.masslany.podkop.common.logging.infrastructure.di

import org.koin.dsl.module
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.logging.infrastructure.main.PlatformLogger

val loggingModule = module {
    single<AppLogger> {
        PlatformLogger()
    }
}
