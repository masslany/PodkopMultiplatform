package pl.masslany.podkop.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.navigation.ExternalBrowser
import pl.masslany.podkop.common.platform.ImageDownloader
import pl.masslany.podkop.initKoin

fun initKoinIos() {
    initKoin {
        modules(iOSModule)
    }
}

val iOSModule = module {
    single { IOSViewControllerHolder() }
    single {
        ExternalBrowser(
            viewControllerProvider = get<IOSViewControllerHolder>().provider,
        )
    }
    single { ImageDownloader() }
}

class IOSDependencyHelper : KoinComponent {
    val startupManager: StartupManager by inject()
    val dispatcherProvider: DispatcherProvider by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun start(key: String, secret: String) {
        scope.launch(dispatcherProvider.io) {
            startupManager.init(key = key, secret = secret)
        }
    }

    fun viewControllerHolder(): IOSViewControllerHolder = getKoin().get()
}
