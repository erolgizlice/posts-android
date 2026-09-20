package com.erolgizlice.posts.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.domain.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val postId: Int = checkNotNull(savedStateHandle[ARG_POST_ID]) {
        "PostDetailFragment needs a $ARG_POST_ID argument"
    }

    /** The same list the posts screen observes; null once the post is gone. */
    val post: StateFlow<Post?> = repository.posts
        .map { posts -> posts.firstOrNull { it.id == postId } }
        .stateIn(viewModelScope, WhileUiSubscribed, null)

    fun save(title: String, body: String) {
        val current = post.value ?: return
        viewModelScope.launch {
            repository.update(current.copy(title = title.trim(), body = body.trim()))
        }
    }

    companion object {
        const val ARG_POST_ID = "postId"
    }
}
