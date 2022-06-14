package com.wzk.oss.screen.detail.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wzk.oss.R

@Composable
fun DetailIntroduce(
    introduce: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(
            vertical = 36.dp,
            horizontal = 36.dp
        ),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.tertiary
    ) {
        Column {
            Text(
                text = stringResource(R.string.screen_detail_introduce),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(
                    top = 32.dp,
                    bottom = 12.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                color = MaterialTheme.colorScheme.onTertiary
            )
            Divider(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 0.5.dp
            )
            Text(
                text = introduce ?: "",
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.padding(
                    top = 12.dp,
                    bottom = 32.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif
            )
        }

    }
}