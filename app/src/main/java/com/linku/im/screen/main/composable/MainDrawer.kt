package com.linku.im.screen.main.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AccountCircle
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linku.im.BuildConfig
import com.linku.im.R
import com.linku.im.extension.debug
import com.linku.im.screen.Screen
import kotlinx.coroutines.launch

private val listDrawerItems = listOf(
    MainDrawerItemDTO(R.string.account, Screen.ProfileScreen, Icons.Sharp.AccountCircle),
    MainDrawerItemDTO(R.string.info, Screen.InfoScreen, Icons.Sharp.Info)
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
            ) {
                scope.launch { drawerState.close() }
                onNavigate(it.screen)
            }
            Spacer(modifier = Modifier.weight(1f))
            debug {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        },
        drawerBackgroundColor = MaterialTheme.colorScheme.background,
        drawerContentColor = MaterialTheme.colorScheme.onBackground,
        content = content
    )
}

@Composable
fun MainDrawerBody(
    items: List<MainDrawerItemDTO>,
    modifier: Modifier,
    onItemClick: (MainDrawerItemDTO) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(120.dp))
        items.forEach {
            Spacer(modifier = Modifier.height(8.dp))
            MainDrawerItem(it) { onItemClick(it) }
        }
    }
}

@Composable
private fun MainDrawerItem(
    item: MainDrawerItemDTO,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(25)
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