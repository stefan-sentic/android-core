package com.htecgroup.coresample.presentation.post.random

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.htecgroup.coresample.domain.post.usecase.GetRandomPost
import com.htecgroup.coresample.presentation.post.PostView
import com.htecgroup.coresample.presentation.post.toPostView
import com.htecgroup.coresample.presentation.widget.WidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PostWidgetWorker @AssistedInject constructor(
    @Assisted params: WorkerParameters,
    @Assisted private val context: Context,
    private val getRandomPost: GetRandomPost
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val postResult = getRandomPost()

        return if (postResult.isSuccess) {
            updateWidget(postResult.getOrNull()?.toPostView())
            Result.success()
        } else {
            Result.failure()
        }
    }

    private fun updateWidget(post: PostView?) {
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))
        val intent = Intent(context, WidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
