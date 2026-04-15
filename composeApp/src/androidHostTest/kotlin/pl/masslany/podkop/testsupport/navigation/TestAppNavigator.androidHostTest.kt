package pl.masslany.podkop.testsupport.navigation

import android.app.Application
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.ExternalBrowser
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.common.navigation.NavigationConfigProvider
import pl.masslany.podkop.common.navigation.TopLevelDestination

actual fun createTestAppNavigator(scope: CoroutineScope): AppNavigator = AppNavigator(
    configProvider = TestNavigationConfigProvider(),
    scope = scope,
    externalBrowser = ExternalBrowser(
        activityProvider = { null },
        application = Application(),
        logger = NoOpAppLogger,
    ),
).apply {
    initialize()
}

private class TestNavigationConfigProvider : NavigationConfigProvider {
    override suspend fun resolveStartDestination(): NavTarget = HomeScreen

    override val topLevelDestinations: Flow<kotlinx.collections.immutable.ImmutableList<TopLevelDestination>> =
        MutableStateFlow(persistentListOf())
}

private data object NoOpAppLogger : AppLogger {
    override fun debug(message: String) = Unit

    override fun info(message: String) = Unit

    override fun warn(message: String, throwable: Throwable?) = Unit

    override fun error(message: String, throwable: Throwable?) = Unit
}
