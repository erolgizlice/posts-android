package com.erolgizlice.posts.domain

/** Transport failures are mapped to this, so callers never depend on the HTTP client. */
class PostsUnavailableException(cause: Throwable) : Exception(cause)
