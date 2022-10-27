package com.linku.im.screen.setting.theme

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linku.domain.repository.MimeType
import com.linku.im.R
import com.linku.im.ktx.compose.ui.graphics.animated
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.ui.components.BottomSheetContent
import com.linku.im.ui.components.Scrim
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.notify.NotifyHolder
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
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

    fun onDismiss() {
        viewModel.onEvent(SettingEvent.Themes.PressedCancel)
    }

    LaunchedEffect(viewModel.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            NotifyHolder(
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
            val themes by viewModel.allTheme.collectAsStateWithLifecycle(emptyList())
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
                            val msg = context.getString(R.string.theme_warn_premium)
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(msg)
                            }
                            viewModel.onEvent(SettingEvent.Themes.SelectThemes(it.id))
                        },
                        onLongClick = {
                            viewModel.onEvent(SettingEvent.Themes.Press(it.id))
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
    Scrim(
        color = Color.Black * 0.35f,
        onDismiss = ::onDismiss,
        visible = state.currentPressedTheme != -1
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        BottomSheetContent(
            visible = state.currentPressedTheme != -1,
            onDismiss = ::onDismiss,
            maxHeight = false
        ) {
            Column {
                ListItem(
                    headlineText = {
                        Text(text = stringResource(R.string.theme_export))
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(LocalSpacing.current.medium))
                        .fillMaxWidth()
                        .clickable {
                            exportedTid = state.currentPressedTheme
                            exporter.launch("Theme_${System.currentTimeMillis()}.txt")
                            onDismiss()
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = LocalTheme.current.background,
                        headlineColor = LocalTheme.current.onBackground,
                        leadingIconColor = LocalTheme.current.onBackground,
                        overlineColor = LocalTheme.current.onBackground
                    )
                )
                ListItem(
                    headlineText = {
                        Text(text = stringResource(R.string.theme_delete))
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(LocalSpacing.current.medium))
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onEvent(SettingEvent.Themes.DeletePressedTheme)
                            onDismiss()
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = LocalTheme.current.background,
                        headlineColor = LocalTheme.current.onBackground,
                        leadingIconColor = LocalTheme.current.onBackground,
                        overlineColor = LocalTheme.current.onBackground
                    )
                )
            }
        }
    }

    BackHandler(state.currentPressedTheme != -1, ::onDismiss)
}
