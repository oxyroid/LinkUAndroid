package com.wzk.oss.screen.list.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wzk.oss.R
import com.wzk.oss.screen.list.ListEvent

data class MenuItem(@StringRes val labelRes: Int, val event: ListEvent)

@Composable
fun ListScreenBar(
    title: String = stringResource(id = R.string.app_name),
    items: List<MenuItem>,
    onNavClick: () -> Unit,
    onItemClick: (Int, MenuItem) -> Unit,
    selectedIndex: Int
) {
    var dropdownMenuExpended by remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = Icons.Default.Menu.name
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { dropdownMenuExpended = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = Icons.Default.MoreVert.name
                    )
                }
                DropdownMenu(
                    expanded = dropdownMenuExpended,
                    onDismissRequest = { dropdownMenuExpended = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    items.forEachIndexed { index, menuItem ->
                        DropdownMenuItem(
                            onClick = {
                                onItemClick(index, menuItem)
                                dropdownMenuExpended = false
                            },
                            modifier = if (selectedIndex == index) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier
                        ) {
                            Text(
                                text = stringResource(id = menuItem.labelRes),
                                color = if (selectedIndex == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    )
}