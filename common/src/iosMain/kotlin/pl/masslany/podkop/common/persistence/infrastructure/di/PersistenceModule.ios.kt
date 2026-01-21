package pl.masslany.podkop.common.persistence.infrastructure.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.dsl.module
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import pl.masslany.podkop.common.persistence.infrastructure.main.DatastoreKeyValueStorage
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

const val DATASTORE_NAME = "pl.masslany.podkop.persistance"

@OptIn(ExperimentalForeignApi::class)
actual val persistenceModule = module {
    single<KeyValueStorage> {
        val datastore = PreferenceDataStoreFactory.createWithPath {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            (requireNotNull(documentDirectory).path + "/$DATASTORE_NAME.preferences_pb").toPath()
        }
        DatastoreKeyValueStorage(datastore)
    }
}
