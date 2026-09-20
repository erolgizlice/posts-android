package com.erolgizlice.posts.data

import com.erolgizlice.posts.domain.PostsUnavailableException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

internal class PostRepositoryImplTest {

    private val api = FakePostApi()
    private val repository = PostRepositoryImpl(api)

    @Test
    fun `posts are ordered by id`() = runTest {
        api.dtos = listOf(dto(3), dto(1), dto(2))

        repository.ensureLoaded()

        assertEquals(listOf(1, 2, 3), repository.posts.first().map { it.id })
    }

    @Test
    fun `a deleted post comes back where it was`() = runTest {
        api.dtos = listOf(dto(1), dto(2), dto(3))
        repository.ensureLoaded()
        val before = repository.posts.first()
        val deleted = before[1]

        repository.delete(deleted.id)

        assertEquals(listOf(1, 3), repository.posts.first().map { it.id })

        repository.restore(deleted)

        assertEquals(before, repository.posts.first())
    }

    @Test
    fun `posts are fetched once`() = runTest {
        api.dtos = listOf(dto(1))

        repository.ensureLoaded()
        repository.ensureLoaded()

        assertEquals(1, api.callCount)
    }

    @Test
    fun `a failed load is retried on the next call`() = runTest {
        api.error = IOException("offline")
        try {
            repository.ensureLoaded()
        } catch (expected: PostsUnavailableException) {
        }

        api.error = null
        api.dtos = listOf(dto(1))
        repository.ensureLoaded()

        assertEquals(2, api.callCount)
        assertEquals(listOf(1), repository.posts.first().map { it.id })
    }

    @Test(expected = PostsUnavailableException::class)
    fun `a network failure surfaces as a domain error`() = runTest {
        api.error = IOException("offline")

        repository.ensureLoaded()
    }

    @Test
    fun `update replaces the post with the same id and keeps the order`() = runTest {
        api.dtos = listOf(dto(1), dto(2), dto(3))
        repository.ensureLoaded()
        val edited = repository.posts.first()[1].copy(title = "edited")

        repository.update(edited)

        val posts = repository.posts.first()
        assertEquals(listOf(1, 2, 3), posts.map { it.id })
        assertEquals("edited", posts[1].title)
        assertEquals("title 1", posts[0].title)
    }

    @Test
    fun `overlapping loads hit the network once`() = runTest {
        api.dtos = listOf(dto(1))
        api.delayMillis = 10

        listOf(
            launch { repository.ensureLoaded() },
            launch { repository.ensureLoaded() },
        ).joinAll()

        assertEquals(1, api.callCount)
    }

    private fun dto(id: Int) = PostDto(id = id, userId = 1, title = "title $id", body = "body $id")
}
