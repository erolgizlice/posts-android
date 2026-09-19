package com.erolgizlice.posts.presentation

import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.domain.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Stands in for the real repository so ViewModel tests stay on the JVM and decide themselves what
 * the data layer does, including failing.
 */
class FakePostRepository : PostRepository {

    private val state = MutableStateFlow<List<Post>>(emptyList())

    override val posts: Flow<List<Post>> = state

    /** Thrown by [ensureLoaded] when set, to drive the error path. */
    var failure: Throwable? = null

    var loadCount = 0
        private set

    fun emit(posts: List<Post>) {
        state.value = posts
    }

    override suspend fun ensureLoaded() {
        loadCount++
        failure?.let { throw it }
    }

    override suspend fun update(post: Post) {
        state.update { posts -> posts.map { if (it.id == post.id) post else it } }
    }

    override suspend fun delete(id: Int) {
        state.update { posts -> posts.filterNot { it.id == id } }
    }

    override suspend fun restore(post: Post) {
        state.update { posts -> (posts + post).sortedBy { it.id } }
    }
}
