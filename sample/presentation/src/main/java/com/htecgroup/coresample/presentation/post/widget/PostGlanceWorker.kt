package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.htecgroup.androidcore.domain.extension.TAG
import com.htecgroup.coresample.domain.post.usecase.GetRandomPostFromNetwork
import com.htecgroup.coresample.presentation.post.PostView
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
    private val getRandomPost: GetRandomPostFromNetwork
) : CoroutineWorker(context, params) {

    companion object {
        private val Int.asPeriodicWorkName get() = "periodic_work_$this"

        fun runOnce(context: Context) {
            val work = OneTimeWorkRequestBuilder<PostGlanceWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(CONNECTED).build())
                .build()

            WorkManager.getInstance(context)
                .beginUniqueWork("single_work", REPLACE, work)
                .enqueue()
        }

        fun enqueuePeriodic(context: Context, widgetId: Int) {
            val work = PeriodicWorkRequestBuilder<PostGlanceWorker>(15, MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(CONNECTED).build())
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(widgetId.asPeriodicWorkName, CANCEL_AND_REENQUEUE, work)
        }

        fun cancelPeriodic(context: Context, widgetId: Int) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(widgetId.asPeriodicWorkName)
        }
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val postResult = getRandomPost()

            Log.d(TAG, "doWork: $postResult")
            if (postResult.isSuccess) {
                updateWidget(context, postResult.getOrNull()?.toPostView())
                Result.success()
            } else {
                updateWidget(context, null)
                Result.failure()
            }
        }

    private suspend fun updateWidget(context: Context, post: PostView?) {
        GlanceAppWidgetManager(context).getGlanceIds(PostWidget::class.java).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                if (post == null) {
                    prefs[stringPreferencesKey(PostWidget.KEY_POST_TITLE)] = "Error!"
                } else {
                    prefs[stringPreferencesKey(PostWidget.KEY_POST_TITLE)] = post.title
                    prefs[stringPreferencesKey(PostWidget.KEY_POST_DESC)] = post.description
                    prefs[stringPreferencesKey(PostWidget.KEY_POST_AUTHOR)] =
                        post.user?.name.orEmpty()
                    prefs[stringPreferencesKey(PostWidget.KEY_TIME)] =
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                }
            }
        }
        PostWidget().updateAll(context)
    }
}
