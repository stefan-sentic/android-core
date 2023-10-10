package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.htecgroup.coresample.domain.post.usecase.GetCommentsForPost
import com.htecgroup.coresample.domain.post.usecase.GetRandomPostFromNetwork
import com.htecgroup.coresample.presentation.post.toCommentView
import com.htecgroup.coresample.presentation.post.toPostView
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit.MINUTES

@HiltWorker
class PostGlanceWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val getRandomPost: GetRandomPostFromNetwork,
    private val getCommentsForPost: GetCommentsForPost
) : CoroutineWorker(context, params) {

    companion object {
        private val String.asPeriodicWorkName get() = "periodic_work_$this"
        private val String.asSingleWorkName get() = "single_work_$this"

        fun runOnce(context: Context, glanceWidgetId: String) {
            val work = OneTimeWorkRequestBuilder<PostGlanceWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(CONNECTED).build())
                .setInputData(
                    Data.Builder().putString(PostWidget.KEY_WIDGET_ID, glanceWidgetId).build()
                )
                .build()

            WorkManager.getInstance(context)
                .beginUniqueWork(glanceWidgetId.asSingleWorkName, REPLACE, work)
                .enqueue()
        }

        fun enqueuePeriodic(context: Context, glanceWidgetId: String) {
            val work = PeriodicWorkRequestBuilder<PostGlanceWorker>(15, MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(CONNECTED).build())
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    glanceWidgetId.asPeriodicWorkName,
                    CANCEL_AND_REENQUEUE,
                    work
                )
        }

        fun cancelPeriodic(context: Context, glanceWidgetId: String) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(glanceWidgetId.asPeriodicWorkName)
        }
    }

    private val WorkerParameters.widgetId: String
        get() = this.inputData.getString(PostWidget.KEY_WIDGET_ID).orEmpty()

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            logWidget("work started ${params.widgetId}")
            updateWidget(context, WidgetState(widgetId = params.widgetId, loading = true))

            val postResult = getRandomPost()
            logWidget("work fetched the data ${params.widgetId}")

            if (postResult.isSuccess) {
                val post = postResult.getOrNull()?.toPostView()
                val postComments =
                    post?.let { getCommentsForPost(it.id) }
                        ?.getOrNull()
                        ?.map { it.toCommentView() }
                        ?: emptyList()
                updateWidget(
                    context,
                    WidgetState(
                        widgetId = params.widgetId,
                        loading = false,
                        post = post,
                        comments = postComments,
                        time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    ),
                )
                Result.success()
            } else {
                updateWidget(context, WidgetState(widgetId = params.widgetId, loading = false))
                Result.failure()
            }
        }

    private suspend fun updateWidget(context: Context, data: WidgetState) {
        logWidget("updateWidget: $data")
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(PostWidget::class.java).forEach { glanceId ->
            logWidget("updateWidget, current glanceID: $glanceId")
            val widgetState = getAppWidgetState(context, PostWidgetStateDefinition, glanceId)
            logWidget("updateWidget, old glanceID: ${widgetState.widgetId}")
            if (widgetState.widgetId.isEmpty() ||
                widgetState.widgetId == PostWidgetStateDefinition.DEFAULT_WIDGET_ID ||
                widgetState.widgetId == data.widgetId
            ) {
                logWidget("updateWidget, doing update")
                updateAppWidgetState(context, PostWidgetStateDefinition, glanceId) { data }
                PostWidget().update(context, glanceId)
            }
        }
    }
}

internal fun logWidget(message: String) {
    Log.d("sentic", message)
}
