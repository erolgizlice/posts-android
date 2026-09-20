package com.erolgizlice.posts.presentation

import com.erolgizlice.posts.domain.Post

sealed interface PostsUiState {

    data object Loading : PostsUiState

    data object Error : PostsUiState

    data class Content(val posts: List<Post>) : PostsUiState
}
