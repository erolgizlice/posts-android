package com.erolgizlice.posts.data

import kotlinx.coroutines.delay

internal class FakePostApi : PostApi {

    var dtos: List<PostDto> = emptyList()

    var error: Throwable? = null

    /** Lets a test overlap two loads. */
    var delayMillis: Long = 0

    var callCount = 0
        private set

    override suspend fun getPosts(): List<PostDto> {
        callCount++
        if (delayMillis > 0) delay(delayMillis)
        error?.let { throw it }
        return dtos
    }
}
