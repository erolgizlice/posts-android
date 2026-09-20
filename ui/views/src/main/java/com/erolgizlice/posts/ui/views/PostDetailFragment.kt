package com.erolgizlice.posts.ui.views

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.erolgizlice.posts.presentation.PostDetailViewModel
import com.erolgizlice.posts.ui.views.databinding.FragmentPostDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private val viewModel: PostDetailViewModel by viewModels()

    private var binding: FragmentPostDetailBinding? = null

    /** Later emissions are this screen's own edits, so the fields are filled once. */
    private var fieldsFilled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPostDetailBinding.bind(view).also { this.binding = it }

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.save.setOnClickListener { save(binding) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.post.collect { post ->
                    when {
                        post == null && fieldsFilled -> parentFragmentManager.popBackStack()
                        post != null && !fieldsFilled -> {
                            binding.titleInput.setText(post.title)
                            binding.bodyInput.setText(post.body)
                            fieldsFilled = true
                        }
                    }
                }
            }
        }
    }

    private fun save(binding: FragmentPostDetailBinding) {
        val title = binding.titleInput.text?.toString().orEmpty()
        if (title.isBlank()) {
            binding.titleField.error = getString(R.string.post_title_required)
            return
        }
        binding.titleField.error = null
        viewModel.save(title, binding.bodyInput.text?.toString().orEmpty())
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(postId: Int) = PostDetailFragment().apply {
            arguments = bundleOf(PostDetailViewModel.ARG_POST_ID to postId)
        }
    }
}
