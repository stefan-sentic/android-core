package com.htecgroup.coresample.presentation.post.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
import com.htecgroup.coresample.presentation.R

class PostWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @GlanceComposable
    @Composable
    private fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "11:48:39",
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(
                    textAlign = TextAlign.End,
                    fontStyle = FontStyle.Italic,
                    fontSize = 10.sp
                )
            )
            Text(
                text = "This will be title",
                maxLines = 2,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Text(
                modifier = GlanceModifier.padding(vertical = 16.dp),
                maxLines = 4,
                text = "This will be description. This will be description. This will be description. This will be description.",
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
                    text = "John Doe",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Image(
                    provider = AndroidResourceImageProvider(R.drawable.ic_refresh),
                    modifier = GlanceModifier.background(AndroidResourceImageProvider(R.drawable.bg_widget_button)),
                    contentDescription = "Refresh"
                )
            }
        }
    }
}
