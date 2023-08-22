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
import android.view.View
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.htecgroup.androidcore.domain.extension.TAG
import com.htecgroup.coresample.presentation.R
import com.htecgroup.coresample.presentation.post.PostView
import com.htecgroup.coresample.presentation.post.PostsActivity
import com.htecgroup.coresample.presentation.post.random.PostWidgetWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit.MINUTES

class WidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = ".widget.WidgetProvider.refresh"
        const val ACTION_DATA = ".widget.WidgetProvider.data"
        const val EXTRA_POST = ".widget.WidgetProvider.extra.post"
        private const val TAG_PERIODIC_WORK = "periodic"
        private const val TAG_ONETIME_WORK = "onetime"
    }

    private val customActions = listOf(ACTION_REFRESH, ACTION_DATA)

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
            setupWidgetRefreshingWork(context, widgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        options: Bundle?
    ) {
        Log.d(TAG, "onAppWidgetOptionsChanged: ")
        //        context.publishRemoteView(context.getRemoteView(widgetId), widgetId)
    }

    private fun onCustomAction(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        when (intent.action) {
            ACTION_REFRESH -> {
                getDataForWidget(context, widgetId)
            }

            ACTION_DATA -> {
                val post = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_POST, PostView::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_POST)
                }
                Log.d(TAG, "$ACTION_DATA: $post")

                if (post != null) {
                    context.publishRemoteView(
                        widgetId = widgetId,
                        remoteView = context.getRemoteView(widgetId).apply {
                            setTextViewText(R.id.txt_title, post.title)
                            setTextViewText(R.id.txt_description, post.description)
                            setTextViewText(R.id.txt_user_author, post.user?.name)
                            setTextViewText(
                                R.id.txt_time,
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            )
                            setViewVisibility(R.id.txt_description, View.VISIBLE)
                            setViewVisibility(R.id.txt_user_author, View.VISIBLE)
                        }
                    )
                } else {
                    context.publishRemoteView(
                        widgetId = widgetId,
                        remoteView = context.getRemoteView(widgetId).apply {
                            setTextViewText(R.id.txt_title, "ERROR!")
                            setViewVisibility(R.id.txt_description, View.GONE)
                            setViewVisibility(R.id.txt_user_author, View.GONE)
                        }
                    )
                }
            }
        }
    }

    private fun setupWidgetRefreshingWork(context: Context, widgetId: Int) {
        val work = PeriodicWorkRequestBuilder<PostWidgetWorker>(15, MINUTES)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
            )
            .setInputData(
                Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId).build()
            )
            .addTag(TAG_PERIODIC_WORK)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("periodic_work_$widgetId", CANCEL_AND_REENQUEUE, work)
    }

    private fun getDataForWidget(context: Context, widgetId: Int) {
        val work = OneTimeWorkRequestBuilder<PostWidgetWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
            )
            .setInputData(
                Data.Builder().putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId).build()
            )
            .addTag(TAG_ONETIME_WORK)
            .build()

        WorkManager.getInstance(context)
            .beginUniqueWork("work_$widgetId", REPLACE, work)
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
                Intent(this, PostsActivity::class.java)
                    .apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        return RemoteViews(this.packageName, R.layout.widget_default).apply {
            setOnClickPendingIntent(R.id.btn_action, btnPendingIntent)
            setOnClickPendingIntent(R.id.txt_title, bgdPendingIntent)
        }
    }
}
