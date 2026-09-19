package com.erolgizlice.posts.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PostMapperTest {

    @Test
    fun `maps the fields the app shows and drops the rest`() {
        val dto = PostDto(id = 7, userId = 3, title = "title", body = "body")

        val post = dto.toDomain()

        assertEquals(7, post.id)
        assertEquals("title", post.title)
        assertEquals("body", post.body)
    }
}
