package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object PostWidgetStateDefinition : GlanceStateDefinition<WidgetState> {

    private const val DATA_STORE_FILENAME = "widget_data"
    const val DEFAULT_WIDGET_ID = "noId"

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<WidgetState> {
        return DataStoreSingletonDelegate(
            serializer = WidgetDataSerializer,
            fileName = DATA_STORE_FILENAME + fileKey,
            corruptionHandler = null,
            produceMigrations = { listOf() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ).getValue(context)
    }

    override fun getLocation(context: Context, fileKey: String): File =
        context.dataStoreFile(DATA_STORE_FILENAME + fileKey)

    @OptIn(ExperimentalSerializationApi::class)
    object WidgetDataSerializer : Serializer<WidgetState> {
        override val defaultValue: WidgetState = WidgetState(widgetId = DEFAULT_WIDGET_ID)

        override suspend fun readFrom(input: InputStream): WidgetState =
            Json.decodeFromStream(input)

        override suspend fun writeTo(t: WidgetState, output: OutputStream) =
            Json.encodeToStream(t, output)
    }
}
