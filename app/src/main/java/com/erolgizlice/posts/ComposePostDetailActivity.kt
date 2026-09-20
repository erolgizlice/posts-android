package com.erolgizlice.posts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.erolgizlice.posts.ui.compose.PostDetailRoute
import com.erolgizlice.posts.ui.compose.PostsTheme
import dagger.hilt.android.AndroidEntryPoint

/** The post id arrives as an intent extra, which ComponentActivity feeds to the SavedStateHandle. */
@AndroidEntryPoint
class ComposePostDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostsTheme {
                PostDetailRoute(onDone = { finish() })
            }
        }
    }
}
