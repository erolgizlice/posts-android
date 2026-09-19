package com.erolgizlice.posts.ui.views

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.presentation.PostsUiState
import com.erolgizlice.posts.presentation.PostsViewModel
import com.erolgizlice.posts.ui.views.databinding.FragmentPostsBinding
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    private val viewModel: PostsViewModel by viewModels()

    private var binding: FragmentPostsBinding? = null

    /** Set when an undo puts a post back, so the list can scroll to where it landed. */
    private var pendingScrollToId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPostsBinding.bind(view).also { this.binding = it }
        val adapter = PostAdapter(onClick = { /* Detail screen arrives with M4. */ })

        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL),
        )
        ItemTouchHelper(SwipeToDeleteCallback(binding, adapter)).attachToRecyclerView(binding.list)
        binding.retry.setOnClickListener { viewModel.retry() }

        // The list keeps its own bottom inset and draws through it, so rows scroll under the
        // navigation bar instead of stopping above it.
        ViewCompat.setOnApplyWindowInsetsListener(binding.list) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

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
            adapter.submitList(state.posts) {
                val restoredId = pendingScrollToId ?: return@submitList
                pendingScrollToId = null
                // A restored post is inserted above the anchor RecyclerView keeps while scrolling,
                // so without this the row comes back off-screen and the undo looks like a no-op.
                val index = state.posts.indexOfFirst { it.id == restoredId }
                if (index != -1) binding.list.scrollToPosition(index)
            }
        }
    }

    private fun onSwiped(binding: FragmentPostsBinding, post: Post) {
        viewModel.onDelete(post)
        Snackbar.make(binding.root, R.string.post_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.post_undo) {
                pendingScrollToId = post.id
                viewModel.onUndoDelete(post)
            }
            .show()
    }

    override fun onDestroyView() {
        binding?.list?.adapter = null
        binding = null
        super.onDestroyView()
    }

    private inner class SwipeToDeleteCallback(
        private val binding: FragmentPostsBinding,
        private val adapter: PostAdapter,
    ) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val background = Paint().apply { color = Color.parseColor("#B3261E") }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            // Captured now: once the repository removes it, this position belongs to another post.
            onSwiped(binding, adapter.currentList[position])
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
        ) {
            val row = viewHolder.itemView
            if (dX > 0) {
                canvas.drawRect(
                    row.left.toFloat(), row.top.toFloat(),
                    row.left + dX, row.bottom.toFloat(), background,
                )
            } else if (dX < 0) {
                canvas.drawRect(
                    row.right + dX, row.top.toFloat(),
                    row.right.toFloat(), row.bottom.toFloat(), background,
                )
            }
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}
