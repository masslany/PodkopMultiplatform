package pl.masslany.podkop

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
import pl.masslany.podkop.common.navigation.AppNavigator

class MainActivity : ComponentActivity() {

    private val appNavigator: AppNavigator by inject()
    private val viewModel by viewModel<MainActivityViewModel>()

    private var backCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value is AppState.Initializing
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        registerOnBackCallback()

        setContent {
            App()
        }
    }

    private fun registerOnBackCallback() {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = OnBackInvokedCallback {
                // Delegate to AppNavigator
                if (!appNavigator.back()) {
                    finish() // allow system to close the activity
                }
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )
            backCallback = callback
        } else {
            // Fallback for < 33
            onBackPressedDispatcher.addCallback(this) {
                if (!appNavigator.back()) {
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backCallback?.let {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it)
            }
        }
    }
}