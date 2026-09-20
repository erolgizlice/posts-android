package com.erolgizlice.posts.presentation

import kotlinx.coroutines.flow.SharingStarted

/** Keeps a screen's flow alive long enough to survive a configuration change. */
internal val WhileUiSubscribed = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
