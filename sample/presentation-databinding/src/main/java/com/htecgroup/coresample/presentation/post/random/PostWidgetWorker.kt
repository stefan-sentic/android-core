package com.htecgroup.coresample.presentation.post.random

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.htecgroup.androidcore.domain.extension.TAG
import com.htecgroup.coresample.domain.post.usecase.GetRandomPost
import com.htecgroup.coresample.presentation.post.PostView
import com.htecgroup.coresample.presentation.post.toPostView
import com.htecgroup.coresample.presentation.widget.WidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class PostWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val getRandomPost: GetRandomPost
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val postResult = getRandomPost()

            Log.d(TAG, "doWork: $postResult")
            if (postResult.isSuccess) {
                updateWidget(postResult.getOrNull()?.toPostView(), widgetId = params.widgetId)
                Result.success()
            } else {
                updateWidget(null, widgetId = params.widgetId)
                Result.failure()
            }
        }

    private fun updateWidget(post: PostView?, widgetId: Int) =
        context.sendBroadcast(
            Intent(context, WidgetProvider::class.java).apply {
                action = WidgetProvider.ACTION_DATA
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra(WidgetProvider.EXTRA_POST, post)
            })
}

private val WorkerParameters.widgetId: Int
    get() = this.inputData.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

