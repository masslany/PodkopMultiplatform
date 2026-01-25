package pl.masslany.podkop

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pl.masslany.podkop.business.startup.api.StartupManager
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class MainApplication : Application() {

    private val mainScope = MainScope()

    private val startupManager by inject<StartupManager>()
    private val dispatcherProvider by inject<DispatcherProvider>()

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(mainModule)
        }

        mainScope.launch(dispatcherProvider.io) {
            startupManager.init(
                key = BuildConfig.WYKOP_KEY,
                secret = BuildConfig.WYKOP_SECRET,
            )
        }
    }
}