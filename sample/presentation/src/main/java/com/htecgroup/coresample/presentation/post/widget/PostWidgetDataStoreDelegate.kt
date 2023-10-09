package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.CoroutineScope
import javax.annotation.concurrent.GuardedBy

class DataStoreSingletonDelegate<T> internal constructor(
    private val fileName: String,
    private val serializer: Serializer<T>,
    private val corruptionHandler: ReplaceFileCorruptionHandler<T>?,
    private val produceMigrations: (Context) -> List<DataMigration<T>>,
    private val scope: CoroutineScope
) {

    private val lock = Any()

    @GuardedBy("lock")
    @Volatile
    private var instance: DataStore<T>? = null

    fun getValue(thisRef: Context): DataStore<T> {
        return instance ?: synchronized(lock) {
            if (instance == null) {
                val applicationContext = thisRef.applicationContext
                instance = DataStoreFactory.create(
                    serializer = serializer,
                    produceFile = { applicationContext.dataStoreFile(fileName) },
                    corruptionHandler = corruptionHandler,
                    migrations = produceMigrations(applicationContext),
                    scope = scope
                )
            }
            instance!!
        }
    }
}