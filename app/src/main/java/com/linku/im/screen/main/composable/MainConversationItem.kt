package com.linku.im.screen.main.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.linku.domain.entity.Conversation
import com.linku.im.extension.times

@Composable
fun MainConversationItem(
    conversation: Conversation,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(100),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxHeight()
                .aspectRatio(1f)
        ) {
            SubcomposeAsyncImage(
                model = { conversation.avatar },
                contentDescription = conversation.name,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                },
                error = {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conversation.name,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = conversation.description,
                color = MaterialTheme.colorScheme.onSurface * 0.8f,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (unreadCount != 0) {
            Surface(
                shape = RoundedCornerShape(100),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}