package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.htecgroup.coresample.presentation.R.drawable
import com.htecgroup.coresample.presentation.post.CommentView
import com.htecgroup.coresample.presentation.post.PostView
import com.htecgroup.coresample.presentation.post.PostsActivity
import com.htecgroup.coresample.presentation.theme.WidgetTheme

class PostWidget : GlanceAppWidget() {

    companion object {
        const val KEY_WIDGET_ID = "widget_id"

        private val SIZE_SMALL = DpSize(150.dp, 150.dp)
        private val SIZE_MEDIUM = DpSize(250.dp, 250.dp)
        private val SIZE_LARGE = DpSize(350.dp, 350.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE))

    override val stateDefinition = PostWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        logWidget("provideGlance $id")
        PostGlanceWorker.runOnce(context, id.toString())

        provideContent {
            logWidget("provideContent $id")
            val state = currentState<WidgetState>()

            WidgetTheme {
                WidgetContent(state)
            }
        }
    }

    @GlanceComposable
    @Composable
    private fun WidgetContent(state: WidgetState) {
        if (state.loading) {
            logWidget("WidgetContent: loading")
            WidgetLoading()
        } else if (state.post == null) {
            logWidget("WidgetContent: error")
            WidgetError()
        } else {
            logWidget("WidgetContent: data (${state.post.title})")
            WidgetData(state.post, state.comments, state.time)
        }
    }

    @Composable
    fun WidgetLoading() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier.fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun WidgetError() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier.fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.errorContainer)
        ) {
            Text(
                text = "Unable to load data",
                style = TextStyle(color = GlanceTheme.colors.error, fontSize = 20.sp)
            )
        }
    }

    @Composable
    private fun WidgetData(post: PostView, postComments: List<CommentView>, time: String) {
        val size = LocalSize.current

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
                text = post.title,
                maxLines = if (size.height < SIZE_MEDIUM.height) 1 else 2,
                modifier = GlanceModifier.clickable(actionStartActivity<PostsActivity>()),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Text(
                modifier = GlanceModifier.padding(vertical = 16.dp),
                maxLines = if (size.height < SIZE_MEDIUM.height) 3 else 5,
                text = post.description,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.secondary
                )
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    text = post.user?.name.orEmpty(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = AndroidResourceImageProvider(drawable.ic_refresh),
                    modifier = GlanceModifier
                        .background(AndroidResourceImageProvider(drawable.bg_widget_button))
                        .clickable(actionRunCallback<RefreshAction>()),
                    contentDescription = "Refresh"
                )
            }

            if (size.height >= SIZE_MEDIUM.height) {
                Text(
                    text = "Comments:",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                LazyColumn {
                    items(postComments) { comment ->
                        Column(modifier = GlanceModifier.padding(8.dp)) {
                            Text(
                                text = "${comment.email}:",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = comment.body,
                                maxLines = 2,
                                style = TextStyle(
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
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
        PostGlanceWorker.runOnce(context, glanceId.toString())
    }
}
