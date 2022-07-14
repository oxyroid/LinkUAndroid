package com.linku.im.screen.main.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AccountCircle
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.linku.im.BuildConfig
import com.linku.im.R
import com.linku.im.extension.debug
import com.linku.im.extension.times
import com.linku.im.screen.Screen
import com.linku.im.ui.Inbox
import com.linku.im.ui.theme.Typography
import kotlinx.coroutines.launch

private val listDrawerItems = listOf(
    MainDrawerItemDTO(R.string.inbox, Screen.ProfileScreen, Icons.Inbox),
    MainDrawerItemDTO(R.string.account, Screen.ProfileScreen, Icons.Sharp.AccountCircle),
    MainDrawerItemDTO(R.string.settings, Screen.ProfileScreen, Icons.Sharp.Settings),
    MainDrawerItemDTO(R.string.info, Screen.InfoScreen, Icons.Sharp.Info)
)

@Composable
fun MainDrawer(
    title: String?,
    drawerState: DrawerState,
    onNavigate: (Screen) -> Unit,
    onHeaderClick: ()->Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.surfaceTint,
                shape = RoundedCornerShape(
                    topEnd = 18.dp,
                    bottomEnd = 18.dp,
                ),
                modifier = Modifier.padding(
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp,
                ),
                elevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    ClickableText(
                        text = AnnotatedString(title?:""),
                        onClick = { onHeaderClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .placeholder(
                                visible = title == null,
                                color = MaterialTheme.colorScheme.outline * 0.3f,
                                shape = RoundedCornerShape(4.dp),
                                highlight = PlaceholderHighlight.shimmer(
                                    highlightColor = Color.White
                                )
                            ),
                        style = Typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.surfaceTint
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MainDrawerBody(
                        items = listDrawerItems,
                        selectedIndex = 0,
                        modifier = Modifier
                    ) {
                        scope.launch { drawerState.close() }
                        onNavigate(it.screen)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    debug {
                        Text(
                            text = "version " + BuildConfig.VERSION_NAME,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        drawerBackgroundColor = Color.Unspecified,
        drawerContentColor = Color.Unspecified,
        content = content,
        drawerElevation = 0.dp
    )
}

@Composable
fun MainDrawerBody(
    items: List<MainDrawerItemDTO>,
    selectedIndex: Int,
    modifier: Modifier,
    onItemClick: (MainDrawerItemDTO) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            Spacer(modifier = Modifier.height(8.dp))
            MainDrawerItem(selected = index == selectedIndex, item = item) { onItemClick(item) }
        }
    }
}

@Composable
private fun MainDrawerItem(
    item: MainDrawerItemDTO,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Unspecified,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(25)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    vertical = 12.dp,
                    horizontal = 18.dp
                )
        ) {
            Icon(imageVector = item.icon, contentDescription = item.icon.name)
            Text(
                text = stringResource(item.titleRes),
                modifier = Modifier.padding(start = 12.dp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

}