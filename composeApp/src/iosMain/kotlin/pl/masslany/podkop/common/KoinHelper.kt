package pl.masslany.podkop.common

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider
import pl.masslany.podkop.common.deeplink.AppDeepLinkHandler
import pl.masslany.podkop.common.navigation.ExternalBrowser
import pl.masslany.podkop.common.platform.BuildInfo
import pl.masslany.podkop.common.platform.ImageDownloader
import pl.masslany.podkop.common.platform.ScreenshotExporter
import pl.masslany.podkop.initKoin
import platform.Foundation.NSBundle

fun initKoinIos() {
    initKoin {
        modules(iOSModule)
    }
}

val iOSModule = module {
    single { IOSViewControllerHolder() }
    single {
        BuildInfo(
            isDebugBuild = isIosDebugBinary(),
            appVersionName = iosAppVersionName(),
        )
    }
    single {
        ExternalBrowser(
            viewControllerProvider = get<IOSViewControllerHolder>().provider,
            logger = get(),
        )
    }
    single { ImageDownloader() }
    single {
        ScreenshotExporter(
            viewControllerProvider = get<IOSViewControllerHolder>().provider,
        )
    }
}

class IOSDependencyHelper : KoinComponent {
    val startupManager: StartupManager by inject()
    val dispatcherProvider: DispatcherProvider by inject()
    val appDeepLinkHandler: AppDeepLinkHandler by inject()

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

    fun handleDeepLink(url: String) {
        appDeepLinkHandler.onIncomingUrl(url)
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

@OptIn(ExperimentalNativeApi::class)
private fun isIosDebugBinary(): Boolean = Platform.isDebugBinary

private fun iosAppVersionName(): String {
    val bundle = NSBundle.mainBundle
    val shortVersion = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
    val buildVersion = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String

    return when {
        !shortVersion.isNullOrBlank() && !buildVersion.isNullOrBlank() && shortVersion != buildVersion ->
            "$shortVersion ($buildVersion)"

        !shortVersion.isNullOrBlank() -> shortVersion

        !buildVersion.isNullOrBlank() -> buildVersion

        else -> "unknown"
    }
}
