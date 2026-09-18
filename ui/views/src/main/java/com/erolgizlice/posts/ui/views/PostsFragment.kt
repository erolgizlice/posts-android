package com.erolgizlice.posts.ui.views

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.erolgizlice.posts.presentation.PostsUiState
import com.erolgizlice.posts.presentation.PostsViewModel
import com.erolgizlice.posts.ui.views.databinding.FragmentPostsBinding
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val viewModel: PostsViewModel by viewModels()

    private var binding: FragmentPostsBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPostsBinding.bind(view).also { this.binding = it }
        val adapter = PostAdapter(onClick = { /* Detail screen arrives with M4. */ })

        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL),
        )
        binding.retry.setOnClickListener { viewModel.retry() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(binding, adapter, state) }
            }
        }
    }

    private fun render(
        binding: FragmentPostsBinding,
        adapter: PostAdapter,
        state: PostsUiState,
    ) {
        binding.progress.isVisible = state is PostsUiState.Loading
        binding.errorGroup.isVisible = state is PostsUiState.Error
        binding.list.isVisible = state is PostsUiState.Content
        binding.empty.isVisible = state is PostsUiState.Content && state.posts.isEmpty()
        if (state is PostsUiState.Content) {
            adapter.submitList(state.posts)
        }
    }

    override fun onDestroyView() {
        binding?.list?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
