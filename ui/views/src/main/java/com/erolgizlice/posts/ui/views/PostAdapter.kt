package com.erolgizlice.posts.ui.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.erolgizlice.posts.domain.Post
import com.erolgizlice.posts.ui.views.databinding.ItemPostBinding

internal class PostAdapter(
    private val onClick: (Post) -> Unit,
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * A payload means only the text changed, so the image is left alone: reloading it would make
     * the row flicker every time a title is edited.
     */
    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bindText(getItem(position))
        }
    }

    internal class PostViewHolder(
        private val binding: ItemPostBinding,
        private val onClick: (Post) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            bindText(post)
            binding.root.setOnClickListener { onClick(post) }
            // Keyed by post id rather than adapter position: the URL is Glide's cache key, so a
            // stable id keeps each row's image stable once rows shift after a deletion.
            Glide.with(binding.image)
                .load("https://picsum.photos/300/300?random=${post.id}&grayscale")
                .placeholder(R.drawable.image_placeholder)
                .into(binding.image)
        }

        fun bindText(post: Post) {
            binding.title.text = post.title
            binding.body.text = post.body
        }
    }

    private companion object {
        /** Marks a rebind that may skip the image. */
        val TEXT_CHANGED = Any()

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem

            // Reached only for two posts with the same id and different content, and the image
            // depends on the id alone, so the difference is always text.
            override fun getChangePayload(oldItem: Post, newItem: Post): Any = TEXT_CHANGED
        }
    }
}
