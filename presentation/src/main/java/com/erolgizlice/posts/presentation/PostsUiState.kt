package com.erolgizlice.posts.presentation

import com.erolgizlice.posts.domain.Post

sealed interface PostsUiState {

    data object Loading : PostsUiState

    data object Error : PostsUiState

    /** An empty list is the empty state; it needs no case of its own. */
    data class Content(val posts: List<Post>) : PostsUiState
}
