package pl.masslany.podkop.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
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
    private var startupStateJob: Job? = null

    fun start(key: String, secret: String) {
        scope.launch(dispatcherProvider.io) {
            startupManager.init(key = key, secret = secret)
        }
    }

    fun startAndObserveStartupState(
        key: String,
        secret: String,
        onStateChanged: (IOSAppStartupState) -> Unit,
    ) {
        observeStartupState(onStateChanged = onStateChanged)
        start(key = key, secret = secret)
    }

    fun observeStartupState(onStateChanged: (IOSAppStartupState) -> Unit) {
        startupStateJob?.cancel()
        startupStateJob = scope.launch(Dispatchers.Main.immediate) {
            startupManager.state.collectLatest { state ->
                onStateChanged(state.toIOSAppStartupState())
            }
        }
    }

    fun stopObservingStartupState() {
        startupStateJob?.cancel()
        startupStateJob = null
    }

    fun viewControllerHolder(): IOSViewControllerHolder = getKoin().get()
}

enum class IOSAppStartupState {
    Initializing,
    Ready,
    Error,
}

private fun AppState.toIOSAppStartupState(): IOSAppStartupState = when (this) {
    AppState.Initializing -> IOSAppStartupState.Initializing
    AppState.Ready -> IOSAppStartupState.Ready
    AppState.Error -> IOSAppStartupState.Error
}
