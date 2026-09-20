package com.erolgizlice.posts.domain

import kotlinx.coroutines.flow.Flow

/** Single source of truth for posts: reads are observed, writes are commands. */
interface PostRepository {

    /** The posts, always ordered by [Post.id]. */
    val posts: Flow<List<Post>>

    /** @throws PostsUnavailableException when the posts cannot be fetched. */
    suspend fun ensureLoaded()

    suspend fun update(post: Post)

    suspend fun delete(id: Int)

    /** Puts [post] back at its place in the [Post.id] order. */
    suspend fun restore(post: Post)
}
