package com.erolgizlice.posts.presentation

import androidx.lifecycle.SavedStateHandle
import com.erolgizlice.posts.domain.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePostRepository()

    @Test
    fun `shows the post named by the argument`() = runTest {
        repository.emit(listOf(post(1), post(2)))
        val viewModel = viewModelFor(postId = 2)
        backgroundScope.launch { viewModel.post.collect {} }

        advanceUntilIdle()

        assertEquals(post(2), viewModel.post.value)
    }

    @Test
    fun `saving trims the text and writes it through the repository`() = runTest {
        repository.emit(listOf(post(1)))
        val viewModel = viewModelFor(postId = 1)
        backgroundScope.launch { viewModel.post.collect {} }
        advanceUntilIdle()

        viewModel.save("  new title  ", "  new body  ")
        advanceUntilIdle()

        assertEquals(Post(id = 1, title = "new title", body = "new body"), repository.posts.first().single())
    }

    @Test
    fun `the post turns null once it is deleted`() = runTest {
        repository.emit(listOf(post(1)))
        val viewModel = viewModelFor(postId = 1)
        backgroundScope.launch { viewModel.post.collect {} }
        advanceUntilIdle()

        repository.delete(1)
        advanceUntilIdle()

        assertNull(viewModel.post.value)
    }

    private fun viewModelFor(postId: Int) = PostDetailViewModel(
        repository = repository,
        savedStateHandle = SavedStateHandle(mapOf(PostDetailViewModel.ARG_POST_ID to postId)),
    )

    private fun post(id: Int) = Post(id = id, title = "title $id", body = "body $id")
}
