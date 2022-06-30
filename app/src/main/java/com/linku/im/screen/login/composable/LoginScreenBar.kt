package com.linku.im.screen.login.composable

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.linku.im.R

@Composable
fun LoginScreenBar(
    onNavClick: () -> Unit
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        navigationIcon = {
            IconButton(onClick = {
                onNavClick()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = Icons.Default.ArrowBack.name
                )
            }
        },
        title = {
            Text(text = stringResource(id = R.string.screen_login_title))
        }
    )
}