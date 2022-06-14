package com.wzk.oss.screen.detail.composable

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wzk.oss.R

@Composable
fun DetailScreenBar(
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
            Text(text = stringResource(id = R.string.screen_detail_title))
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = Icons.Default.Share.name
                )
            }
        }
    )
}