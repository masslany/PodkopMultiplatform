package pl.masslany.podkop.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.initKoin

fun initKoinIos() {
    initKoin {

    }
}

class IOSDependencyHelper : KoinComponent {
    val startupManager: StartupManager by inject()
    val dispatcherProvider: DispatcherProvider by inject()

    // We create a scope here to mimic your Android MainScope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun start(key: String, secret: String) {
        scope.launch(dispatcherProvider.io) {
            startupManager.init(key = key, secret = secret)
        }
    }
}