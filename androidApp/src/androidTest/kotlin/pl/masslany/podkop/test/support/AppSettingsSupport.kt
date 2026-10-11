package pl.masslany.podkop.test.support

import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import pl.masslany.podkop.common.settings.AppSettings

/** Turns on threaded entry comments, an opt-in setting, before the app starts. */
fun enableThreadedEntryComments() {
    runBlocking {
        GlobalContext.get().get<AppSettings>().setThreadedEntryComments(true)
    }
}
