package com.htecgroup.coresample.presentation.post.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
        logWidget("onUpdate: ")
        appWidgetIds.forEach { widgetId ->
            PostGlanceWorker.enqueuePeriodic(
                context = context,
                glanceWidgetId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId).toString()
            )
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { widgetId ->
            PostGlanceWorker.cancelPeriodic(
                context = context,
                glanceWidgetId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId).toString()
            )
        }
    }
}
