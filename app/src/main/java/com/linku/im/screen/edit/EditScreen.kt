package com.linku.im.screen.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.linku.im.R
import com.linku.im.screen.introduce.IntroduceEvent
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.notify.NotifyCompat
import com.linku.im.ui.theme.LocalTheme

@Composable
fun EditScreen(
    type: IntroduceEvent.Edit.Type,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { NotifyCompat(state = it) },
        topBar = {
            ToolBar(
                actions = {},
                text = when (type) {
                    IntroduceEvent.Edit.Type.Description -> stringResource(R.string.profile_data_description)
                    IntroduceEvent.Edit.Type.Name -> stringResource(R.string.profile_data_realName)
                    IntroduceEvent.Edit.Type.NickName -> stringResource(R.string.profile_data_name)
                }
            )
        },
        contentColor = LocalTheme.current.onBackground,
        backgroundColor = LocalTheme.current.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {

        }
    }

}
