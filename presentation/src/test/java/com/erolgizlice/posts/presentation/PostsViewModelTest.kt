package com.erolgizlice.posts.presentation

import app.cash.turbine.test
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.domain.PostsUnavailableException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PostsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePostRepository()

    @Test
    fun `starts loading and shows the posts once they arrive`() = runTest {
        val viewModel = PostsViewModel(repository)

        viewModel.uiState.test {
            assertEquals(PostsUiState.Loading, awaitItem())

            repository.emit(listOf(post(1)))

            assertEquals(PostsUiState.Content(listOf(post(1))), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows an error when the load fails and nothing is on screen`() = runTest {
        repository.failure = PostsUnavailableException(IOException("offline"))
        val viewModel = PostsViewModel(repository)

        viewModel.uiState.test {
            assertEquals(PostsUiState.Loading, awaitItem())
            assertEquals(PostsUiState.Error, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry asks the repository again`() = runTest {
        repository.failure = PostsUnavailableException(IOException("offline"))
        val viewModel = PostsViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(PostsUiState.Error, viewModel.uiState.value)

        repository.failure = null
        viewModel.retry()
        repository.emit(listOf(post(1)))
        advanceUntilIdle()

        assertEquals(2, repository.loadCount)
        assertEquals(PostsUiState.Content(listOf(post(1))), viewModel.uiState.value)
    }

    @Test
    fun `deleting a post removes it and undo puts it back`() = runTest {
        repository.emit(listOf(post(1), post(2)))
        val viewModel = PostsViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onDelete(post(2))
        advanceUntilIdle()

        assertEquals(PostsUiState.Content(listOf(post(1))), viewModel.uiState.value)

        viewModel.onUndoDelete(post(2))
        advanceUntilIdle()

        assertEquals(PostsUiState.Content(listOf(post(1), post(2))), viewModel.uiState.value)
    }

    private fun post(id: Int) = Post(id = id, title = "title $id", body = "body $id")
}
