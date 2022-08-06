package com.linku.im.screen.main.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.linku.im.R
import com.linku.im.LinkUEvent
import com.linku.im.screen.Screen
import com.linku.im.ui.components.Inbox
import com.linku.im.ui.theme.supportDynamic
import com.linku.im.vm
import kotlinx.coroutines.launch

private val routes = listOf(
    Route(R.string.inbox, Screen.MainScreen, Icons.Inbox),
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
                modifier = Modifier.padding(24.dp)
            ) {
                DrawerBody {
                    scope.launch { drawerState.close() }
                    onNavigate(it.screen)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = vm.state.value.isDarkMode,
                        onCheckedChange = {
                            vm.onEvent(LinkUEvent.ToggleDarkMode)
                        },
                        thumbContent = {
                            val icon =
                                if (!vm.state.value.isDarkMode) painterResource(id = R.drawable.ic_baseline_dark_mode_24)
                                else painterResource(id = R.drawable.ic_baseline_light_mode_24)
                            Icon(painter = icon, contentDescription = "")
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(
                            id = R.string.toggle_theme
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (supportDynamic) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = vm.state.value.dynamicEnabled,
                            onCheckedChange = {
                                vm.onEvent(LinkUEvent.ToggleDynamic)
                            },
                            thumbContent = {

                            },
                            colors = SwitchDefaults.colors()
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.toggle_dynamic),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        routes.forEachIndexed { index, item ->
            Spacer(modifier = Modifier.height(8.dp))
            DrawerItem(selected = index == 0, item = item) {
                onRouted(item)
            }
        }
    }
}