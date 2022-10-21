package com.linku.im.screen.setting.theme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.repository.MimeType
import com.linku.im.R
import com.linku.im.ktx.compose.ui.graphics.animated
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.ui.components.Snacker
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalTheme
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: ThemeSettingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onEvent(SettingEvent.Themes.Init)
    }
    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var exportedTid: Int? by remember {
        mutableStateOf(null)
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(MimeType.Txt.value)
    ) {
        it?.let {
            exportedTid?.let { exportedTid ->
                viewModel.onEvent(SettingEvent.Themes.WriteThemeToUri(it, exportedTid))
            }
        }
    }
    val importer = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        it?.let {
            viewModel.onEvent(SettingEvent.Themes.Import(it))
        }
    }
    LaunchedEffect(viewModel.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            Snacker(
                state = it,
                modifier = Modifier.fillMaxWidth()
            )
        },
        topBar = {
            ToolBar(
                actions = {},
                text = stringResource(id = R.string.profile_settings_theme),
                backgroundColor = LocalTheme.current.topBar.animated(),
                contentColor = LocalTheme.current.onTopBar.animated()
            )
        },
        modifier = modifier,
        backgroundColor = LocalTheme.current.background.animated(),
        contentColor = LocalTheme.current.onBackground.animated()
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            val themes by viewModel.allTheme.collectAsState(initial = emptyList())
            LazyRow(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(themes) {
                    ThemeSelection(
                        theme = it,
                        currentTid = state.currentTheme,
                        modifier = Modifier.height(96.dp),
                        onClick = {
                            val msg = context.getString(R.string.warn_premium_theme)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(msg)
                            }
                            viewModel.onEvent(SettingEvent.Themes.SelectThemes(it.id))
                        },
                        onLongClick = {
                            exportedTid = it.id
                            exporter.launch("Theme_${System.currentTimeMillis()}.txt")
                        }
                    )
                }
                item {
                    ThemeAddSelection(
                        modifier = Modifier.height(96.dp)
                    ) {
                        importer.launch(arrayOf(MimeType.Txt.value))
                    }
                }
            }
        }
    }
}
