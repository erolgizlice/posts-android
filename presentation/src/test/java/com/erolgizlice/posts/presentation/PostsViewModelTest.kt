package com.erolgizlice.posts.presentation

import app.cash.turbine.test
import com.erolgizlice.posts.domain.Post
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

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

    private fun post(id: Int) = Post(id = id, title = "title $id", body = "body $id")
}
