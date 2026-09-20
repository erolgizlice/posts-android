package com.erolgizlice.posts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.domain.PostRepository
import com.erolgizlice.posts.domain.PostsUnavailableException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val loading = MutableStateFlow(true)
    private val failed = MutableStateFlow(false)

    val uiState: StateFlow<PostsUiState> =
        combine(repository.posts, loading, failed) { posts, loading, failed ->
            when {
                posts.isNotEmpty() -> PostsUiState.Content(posts)
                loading -> PostsUiState.Loading
                failed -> PostsUiState.Error
                else -> PostsUiState.Content(posts)
            }
        }.stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = PostsUiState.Loading,
        )

    init {
        load()
    }

    fun retry() = load()

    fun onDelete(post: Post) {
        viewModelScope.launch { repository.delete(post.id) }
    }

    fun onUndoDelete(post: Post) {
        viewModelScope.launch { repository.restore(post) }
    }

    private fun load() {
        viewModelScope.launch {
            loading.value = true
            try {
                repository.ensureLoaded()
                failed.value = false
            } catch (e: PostsUnavailableException) {
                failed.value = true
            } finally {
                loading.value = false
            }
        }
    }

}
