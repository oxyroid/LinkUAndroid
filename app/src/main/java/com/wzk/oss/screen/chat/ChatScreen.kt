package com.wzk.oss.screen.chat

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.wzk.oss.R
import com.wzk.oss.screen.chat.composable.Bubble
import com.wzk.oss.ui.MaterialTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.toastEvent.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.connectToChat()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.disconnect()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.chatState
    val text by viewModel.messageTextState
    Scaffold(
        topBar = {
            MaterialTopBar(
                title = "",
                onNavClick = {

                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                reverseLayout = true
            ) {
                items(state.messages) {
                    Bubble(message = it, isAnother = it.uid != 0)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        viewModel.onTextChange(it)
                    },
                    Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Type here..", color = MaterialTheme.colorScheme.outline)
                    },
                    shape = Shapes.Full
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = viewModel::onMessage) {
                    Text(text = stringResource(id = R.string.send))
                }
            }
        }
    }
}