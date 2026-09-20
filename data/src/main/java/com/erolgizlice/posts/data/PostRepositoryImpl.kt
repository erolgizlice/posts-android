package com.erolgizlice.posts.data

import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.domain.PostRepository
import com.erolgizlice.posts.domain.PostsUnavailableException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Loads once and keeps the list in memory, so local edits are not overwritten by a refetch. */
@Singleton
internal class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
) : PostRepository {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    override val posts: Flow<List<Post>> = _posts.asStateFlow()

    private val loadMutex = Mutex()
    private var loaded = false

    override suspend fun ensureLoaded() = loadMutex.withLock {
        if (loaded) return@withLock
        val fetched = try {
            api.getPosts().map { it.toDomain() }
        } catch (e: IOException) {
            throw PostsUnavailableException(e)
        } catch (e: HttpException) {
            throw PostsUnavailableException(e)
        }
        _posts.value = fetched.sortedBy { it.id }
        loaded = true
    }

    override suspend fun update(post: Post) {
        _posts.update { current -> current.map { if (it.id == post.id) post else it } }
    }

    override suspend fun delete(id: Int) {
        _posts.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun restore(post: Post) {
        _posts.update { current -> (current + post).sortedBy { it.id } }
    }
}
