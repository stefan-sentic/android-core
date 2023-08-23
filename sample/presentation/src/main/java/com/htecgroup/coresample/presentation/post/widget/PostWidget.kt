package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.htecgroup.coresample.presentation.R
import com.htecgroup.coresample.presentation.post.PostsActivity

class PostWidget : GlanceAppWidget() {

    companion object {
        const val KEY_POST_TITLE = "post_title"
        const val KEY_POST_DESC = "post_description"
        const val KEY_POST_AUTHOR = "post_author"
        const val KEY_TIME = "time"
    }

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        PostGlanceWorker.runOnce(context)

        provideContent {
            val state = currentState<Preferences>()
            val postTitle = state[stringPreferencesKey(KEY_POST_TITLE)].orEmpty()
            val postDesc = state[stringPreferencesKey(KEY_POST_DESC)].orEmpty()
            val postAuthor = state[stringPreferencesKey(KEY_POST_AUTHOR)].orEmpty()
            val time = state[stringPreferencesKey(KEY_TIME)].orEmpty()

            GlanceTheme {
                WidgetContent(postTitle, postDesc, postAuthor, time)
            }
        }
    }

    @GlanceComposable
    @Composable
    private fun WidgetContent(
        title: String,
        description: String,
        postAuthor: String,
        time: String
    ) {
        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = time,
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(
                    textAlign = TextAlign.End,
                    fontStyle = FontStyle.Italic,
                    fontSize = 10.sp
                )
            )
            Text(
                text = title,
                maxLines = 2,
                modifier = GlanceModifier.clickable(actionStartActivity<PostsActivity>()),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Text(
                modifier = GlanceModifier.padding(vertical = 16.dp),
                maxLines = 4,
                text = description,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.secondary
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    text = postAuthor,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = AndroidResourceImageProvider(R.drawable.ic_refresh),
                    modifier = GlanceModifier
                        .background(AndroidResourceImageProvider(R.drawable.bg_widget_button))
                        .clickable(actionRunCallback<RefreshAction>()),
                    contentDescription = "Refresh"
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        PostGlanceWorker.runOnce(context)
    }
}
