package com.linku.im.screen.main.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.DarkMode
import androidx.compose.material.icons.sharp.Inbox
import androidx.compose.material.icons.sharp.LightMode
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.Screen
import com.linku.im.ui.theme.supportDynamic
import com.linku.im.vm
import kotlinx.coroutines.launch

private val routes = listOf(
    Route(R.string.inbox, Screen.MainScreen, Icons.Sharp.Inbox),
    Route(R.string.settings, Screen.ProfileScreen, Icons.Sharp.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    drawerState: DrawerState,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                Modifier.padding(24.dp)
            ) {
                DrawerBody {
                    scope.launch { drawerState.close() }
                    onNavigate(it.screen)
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = vm.readable.isDarkMode,
                        onCheckedChange = {
                            vm.onEvent(LinkUEvent.ToggleDarkMode)
                        },
                        thumbContent = {
                            val icon = if (!vm.readable.isDarkMode) Icons.Sharp.DarkMode
                            else Icons.Sharp.LightMode
                            Icon(imageVector = icon, contentDescription = "dark theme")
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.toggle_theme),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (supportDynamic) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = vm.readable.dynamicEnabled,
                            onCheckedChange = {
                                vm.onEvent(LinkUEvent.ToggleDynamic)
                            },
                            thumbContent = {

                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.toggle_dynamic),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

        },
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        content = content,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp,
        modifier = modifier
    )
}

@Composable
private fun DrawerBody(
    modifier: Modifier = Modifier,
    onRouted: (Route) -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        routes.forEachIndexed { index, item ->
            Spacer(Modifier.height(8.dp))
            DrawerItem(selected = index == 0, item = item) {
                onRouted(item)
            }
        }
    }
}