package com.htecgroup.coresample.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.htecgroup.androidcore.domain.extension.TAG
import com.htecgroup.coresample.presentation.R
import kotlin.random.Random

class WidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = ".widget.WidgetProvider.refresh"
        const val ACTION_DATA = ".widget.WidgetProvider.data"
    }

    private val customActions = listOf(ACTION_REFRESH)

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: ${intent?.action}")
        if (intent?.action in customActions) onCustomAction(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, context.getRemoteView(widgetId))
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        options: Bundle?
    ) {
        Log.d(TAG, "onAppWidgetOptionsChanged: ")
        context.publishRemoteView(context.getRemoteView(widgetId), widgetId)
    }

    private fun onCustomAction(context: Context, intent: Intent?) {
        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        when (intent?.action) {
            ACTION_REFRESH -> {
                context.publishRemoteView(
                    widgetId = widgetId,
                    remoteView = context.getRemoteView(widgetId).apply {
                        setTextViewText(R.id.txt_content, "Random id: ${Random.nextInt()}")
                    }
                )
            }
        }
    }

    private fun Context.publishRemoteView(remoteView: RemoteViews, widgetId: Int) {
        AppWidgetManager.getInstance(this).updateAppWidget(widgetId, remoteView)
    }

    private fun Context.getRemoteView(widgetId: Int): RemoteViews {
        val btnPendingIntent =
            PendingIntent.getBroadcast(
                this,
                1,
                Intent(this, WidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val bgdPendingIntent =
            PendingIntent.getActivity(
                this,
                2,
                Intent(this, WidgetConfigurationActivity::class.java)
                    .apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        return RemoteViews(this.packageName, R.layout.widget_default).apply {
            setOnClickPendingIntent(R.id.btn_action, btnPendingIntent)
            setOnClickPendingIntent(android.R.id.background, bgdPendingIntent)
        }
    }
}
