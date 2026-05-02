package pl.masslany.podkop.test.common

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.stopKoin
import pl.masslany.podkop.initKoin
import pl.masslany.podkop.mainModule
import pl.masslany.podkop.test.IntegrationTestAuth
import pl.masslany.podkop.test.IntegrationTestNetwork
import pl.masslany.podkop.test.integrationTestModule
import pl.masslany.podkop.test.support.MockApiServer

class IntegrationTestRule(
    private val configureMockApi: (MockApiServer) -> Unit,
    private val configureEnvironment: () -> Unit,
) : ExternalResource() {
    lateinit var mockApiServer: MockApiServer
        private set

    override fun before() {
        stopKoin()
        IntegrationTestAuth.reset()
        IntegrationTestNetwork.reset()
        configureEnvironment()

        mockApiServer = MockApiServer().also { server ->
            configureMockApi(server)
            IntegrationTestNetwork.baseUrl = server.start()
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        initKoin(
            appDeclaration = {
                androidContext(context.applicationContext)
                androidLogger()
                modules(mainModule)
            },
            additionalModules = listOf(integrationTestModule),
        )
    }

    override fun after() {
        if (::mockApiServer.isInitialized) {
            mockApiServer.shutdown()
        }
        IntegrationTestAuth.reset()
        IntegrationTestNetwork.reset()
        stopKoin()
    }
}
