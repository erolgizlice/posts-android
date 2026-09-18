package com.erolgizlice.posts.data

import retrofit2.http.GET

internal interface PostApi {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
