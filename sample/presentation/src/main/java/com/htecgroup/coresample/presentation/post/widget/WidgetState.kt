package com.htecgroup.coresample.presentation.post.widget

import com.htecgroup.coresample.presentation.post.CommentView
import com.htecgroup.coresample.presentation.post.PostView
import kotlinx.serialization.Serializable

@Serializable
data class WidgetState(
    val widgetId: String,
    val loading: Boolean = true,
    val post: PostView? = null,
    val comments: List<CommentView> = emptyList(),
    val time: String = ""
)
