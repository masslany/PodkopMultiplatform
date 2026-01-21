package pl.masslany.podkop.common.persistence.infrastructure.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.dsl.module
import pl.masslany.podkop.common.persistence.api.KeyValueStorage
import pl.masslany.podkop.common.persistence.infrastructure.main.DatastoreKeyValueStorage

const val DATASTORE_NAME = "pl.masslany.podkop.persistance"

actual val persistenceModule = module {
    single<KeyValueStorage> {
        val datastore = PreferenceDataStoreFactory.create {
            get<Context>().preferencesDataStoreFile(DATASTORE_NAME)
        }
        DatastoreKeyValueStorage(datastore)
    }
}
