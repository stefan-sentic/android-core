package com.htecgroup.coresample.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.htecgroup.androidcore.domain.extension.TAG
import com.htecgroup.coresample.presentation.R
import com.htecgroup.coresample.presentation.post.PostView
import com.htecgroup.coresample.presentation.post.random.PostWidgetWorker

class WidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = ".widget.WidgetProvider.refresh"
        const val ACTION_DATA = ".widget.WidgetProvider.data"
        const val EXTRA_POST = ".widget.WidgetProvider.extra.post"
    }

    private val initializedWidgets = mutableMapOf<Int, Boolean>()
    private val customActions = listOf(ACTION_REFRESH, ACTION_DATA)
    private var post: PostView? = null

    init {
        Log.d(TAG, "WidgetProvider init")
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")

        if (intent.action in customActions) {
            onCustomAction(context, intent)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, context.getRemoteView(widgetId))
            if (initializedWidgets[widgetId] != true) {
                initializedWidgets[widgetId] = true
                getDataForWidget(context, widgetId)
            }
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

    private fun onCustomAction(context: Context, intent: Intent) {
        Log.d(TAG, "onCustomAction: ${intent.action}")
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        when (intent.action) {
            ACTION_REFRESH -> {
                getDataForWidget(context, widgetId)
            }

            ACTION_DATA -> {
                post = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_POST, PostView::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_POST)
                }

                if (post == null) return //todo: handle error state

                context.publishRemoteView(
                    widgetId = widgetId,
                    remoteView = context.getRemoteView(widgetId).apply {
                        setTextViewText(R.id.txt_title, post?.title)
                        setTextViewText(R.id.txt_description, post?.description)
                    }
                )
            }
        }
    }

    private fun getDataForWidget(context: Context, widgetId: Int) {
        val work = OneTimeWorkRequestBuilder<PostWidgetWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
            )
            .setInputData(
                Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId).build()
            )
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork(widgetId.toString(), REPLACE, work)
            .enqueue()
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
            setTextViewText(R.id.txt_title, post?.title)
            setTextViewText(R.id.txt_description, post?.description)
            setOnClickPendingIntent(R.id.btn_action, btnPendingIntent)
            setOnClickPendingIntent(android.R.id.background, bgdPendingIntent)
        }
    }
}
