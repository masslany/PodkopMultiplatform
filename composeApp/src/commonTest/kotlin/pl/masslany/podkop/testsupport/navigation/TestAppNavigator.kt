package pl.masslany.podkop.testsupport.navigation

import kotlinx.coroutines.CoroutineScope
import pl.masslany.podkop.common.navigation.AppNavigator

expect fun createTestAppNavigator(scope: CoroutineScope): AppNavigator
