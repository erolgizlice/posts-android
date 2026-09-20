package com.erolgizlice.posts.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.erolgizlice.posts.presentation.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailRoute(
    onDone: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val post by viewModel.post.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var filled by rememberSaveable { mutableStateOf(false) }
    var titleError by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(post) {
        val current = post
        when {
            current != null && !filled -> {
                title = current.title
                body = current.body
                filled = true
            }
            current == null && filled -> onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit post") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Title") },
                isError = titleError,
                supportingText = { if (titleError) Text("Title cannot be empty") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Description") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        viewModel.save(title, body)
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Text("Save")
            }
        }
    }
}
