package com.erolgizlice.posts.data

import com.erolgizlice.posts.domain.Post

internal fun PostDto.toDomain() = Post(
    id = id,
    title = title,
    body = body,
)
