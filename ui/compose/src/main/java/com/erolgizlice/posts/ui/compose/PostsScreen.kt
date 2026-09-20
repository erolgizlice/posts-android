package com.erolgizlice.posts.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.presentation.PostsUiState
import com.erolgizlice.posts.presentation.PostsViewModel
import kotlinx.coroutines.launch

@Composable
fun PostsRoute(
    onPostClick: (Post) -> Unit,
    viewModel: PostsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PostsScreen(
        state = state,
        onPostClick = onPostClick,
        onDelete = viewModel::onDelete,
        onUndoDelete = viewModel::onUndoDelete,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostsScreen(
    state: PostsUiState,
    onPostClick: (Post) -> Unit,
    onDelete: (Post) -> Unit,
    onUndoDelete: (Post) -> Unit,
    onRetry: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var restoredId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (state) {
                PostsUiState.Loading -> CircularProgressIndicator()

                PostsUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Posts could not be loaded.")
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }

                is PostsUiState.Content -> if (state.posts.isEmpty()) {
                    Text("No posts left.")
                } else {
                    LaunchedEffect(state.posts, restoredId) {
                        val id = restoredId ?: return@LaunchedEffect
                        val index = state.posts.indexOfFirst { it.id == id }
                        if (index == -1) return@LaunchedEffect
                        // Only when it landed above the viewport; anywhere else the reader's
                        // position is worth more than showing the row again.
                        if (index < listState.firstVisibleItemIndex) listState.scrollToItem(index)
                        restoredId = null
                    }
                    PostList(
                        listState = listState,
                        posts = state.posts,
                        onPostClick = onPostClick,
                        onSwiped = { post ->
                            onDelete(post)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Post deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    restoredId = post.id
                                    onUndoDelete(post)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostList(
    listState: LazyListState,
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onSwiped: (Post) -> Unit,
) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(posts, key = { it.id }) { post ->
            // Plain remember: the saveable state would bring a restored post back still dismissed.
            val threshold = SwipeToDismissBoxDefaults.positionalThreshold
            val dismissState = remember(post.id) {
                SwipeToDismissBoxState(
                    initialValue = SwipeToDismissBoxValue.Settled,
                    positionalThreshold = threshold,
                )
            }
            Column(Modifier.animateItem()) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFB3261E)))
                    },
                    onDismiss = { onSwiped(post) },
                ) {
                    PostRow(post = post, onClick = { onPostClick(post) })
                }
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PostRow(post: Post, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlideImage(
            // Keyed by id, not position: the URL is the image cache key.
            model = "https://picsum.photos/300/300?random=${post.id}&grayscale",
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(CircleShape),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
