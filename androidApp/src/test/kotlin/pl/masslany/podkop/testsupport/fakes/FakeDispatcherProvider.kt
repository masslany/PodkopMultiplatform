package pl.masslany.podkop.testsupport.fakes

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class FakeDispatcherProvider(dispatcher: CoroutineDispatcher = Dispatchers.Unconfined) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
