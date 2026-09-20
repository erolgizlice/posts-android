package com.erolgizlice.posts.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
)

private val darkScheme = darkColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
)

/** Mirrors the View theme: the same blue app bar in both modes. */
@Composable
fun PostsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        content = content,
    )
}
