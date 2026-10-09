package pl.masslany.podkop.test

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.test.platform.app.InstrumentationRegistry
import org.koin.core.module.Module
import pl.masslany.podkop.MainApplication
import pl.masslany.podkop.test.support.MockApiServer

class TestMainApplication : MainApplication() {
    lateinit var mockApiServer: MockApiServer
        private set

    override fun onCreate() {
        // The app calls the API as soon as it starts, so the mock server has to be up before that.
        mockApiServer = MockApiServer(assets = InstrumentationRegistry.getInstrumentation().context.assets)
        mockApiServer.start()
        super.onCreate()
        registerActivityLifecycleCallbacks(TestActivityWindowFlags)
    }

    override fun additionalKoinModules(): List<Module> =
        listOf(integrationTestModule(baseUrl = mockApiServer.baseUrl))
}

private object TestActivityWindowFlags : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        @Suppress("DEPRECATION")
        activity.window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
