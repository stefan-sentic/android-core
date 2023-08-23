package com.htecgroup.coresample.presentation.post.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.htecgroup.androidcore.domain.extension.TAG
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget get() = PostWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate: ")
        appWidgetIds.forEach { widgetId ->
            PostGlanceWorker.enqueuePeriodic(context, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { widgetId ->
            PostGlanceWorker.cancelPeriodic(context, widgetId)
        }
    }
}
