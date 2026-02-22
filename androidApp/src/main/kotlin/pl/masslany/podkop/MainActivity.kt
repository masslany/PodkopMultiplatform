package pl.masslany.podkop

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.masslany.podkop.business.startup.models.AppState
import pl.masslany.podkop.common.deeplink.AppDeepLinkHandler
import pl.masslany.podkop.common.navigation.AppNavigator

class MainActivity : ComponentActivity() {

    private val appNavigator: AppNavigator by inject()
    private val appDeepLinkHandler: AppDeepLinkHandler by inject()
    private val activityHolder: AndroidActivityHolder by inject()

    private val viewModel by viewModel<MainActivityViewModel>()

    private var backCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value is AppState.Initializing
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        activityHolder.activity = this

        registerOnBackCallback()
        handleDeepLinkIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun registerOnBackCallback() {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = OnBackInvokedCallback {
                if (!appNavigator.back()) {
                    finish()
                }
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
            backCallback = callback
        } else {
            onBackPressedDispatcher.addCallback(this) {
                if (!appNavigator.back()) {
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        activityHolder.activity = null
        super.onDestroy()
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        appDeepLinkHandler.onIncomingUrl(intent?.dataString)
    }
}
