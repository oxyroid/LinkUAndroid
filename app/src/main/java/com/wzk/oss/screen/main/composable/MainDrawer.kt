package com.wzk.oss.screen.main.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wzk.oss.R
import com.wzk.oss.screen.Screen
import kotlinx.coroutines.launch

private val listDrawerItems = listOf(
    MainDrawerItemDTO(R.string.account, Screen.ProfileScreen, Icons.Rounded.AccountCircle),
    MainDrawerItemDTO(R.string.info, Screen.InfoScreen, Icons.Rounded.Info)
)

@Composable
fun MainDrawer(
    drawerState: DrawerState,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerBody(
                items = listDrawerItems,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .weight(1f)
            ) {
                scope.launch { drawerState.close() }
                onNavigate(it.screen)
            }
        },
        content = content
    )
}

@Composable
fun MainDrawerBody(
    items: List<MainDrawerItemDTO>,
    modifier: Modifier,
    onItemClick: (MainDrawerItemDTO) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { MainDrawerItem(it) { onItemClick(it) } }
    }
}

@Composable
private fun MainDrawerItem(
    item: MainDrawerItemDTO,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Icon(imageVector = item.icon, contentDescription = item.icon.name)
            Text(
                text = stringResource(item.titleRes),
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }

}