package com.erolgizlice.posts.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val colorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
)

@Composable
fun PostsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, content = content)
}
