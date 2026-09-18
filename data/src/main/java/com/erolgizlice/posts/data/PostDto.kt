package com.erolgizlice.posts.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PostDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
)
