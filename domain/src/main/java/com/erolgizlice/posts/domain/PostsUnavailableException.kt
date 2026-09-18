package com.erolgizlice.posts.domain

/**
 * Posts could not be fetched. The data layer maps transport failures to this type, so that
 * callers never depend on the HTTP client.
 */
class PostsUnavailableException(cause: Throwable) : Exception(cause)
