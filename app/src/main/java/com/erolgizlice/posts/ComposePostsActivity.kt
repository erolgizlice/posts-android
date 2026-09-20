package com.erolgizlice.posts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.erolgizlice.posts.presentation.PostDetailViewModel
import com.erolgizlice.posts.ui.compose.PostsRoute
import com.erolgizlice.posts.ui.compose.PostsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposePostsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostsTheme {
                PostsRoute(
                    onPostClick = { post ->
                        startActivity(
                            Intent(this, ComposePostDetailActivity::class.java)
                                .putExtra(PostDetailViewModel.ARG_POST_ID, post.id),
                        )
                    },
                )
            }
        }
    }
}
