package com.linku.im.screen.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.linku.im.LinkUEvent
import com.linku.im.screen.introduce.IntroduceEvent
import com.linku.im.ui.components.ToolBar
import com.linku.im.vm

@Composable
fun EditScreen(
    type: IntroduceEvent.Edit.Type?
) {
    LaunchedEffect(Unit) { type ?: run { vm.onEvent(LinkUEvent.PopBackStack) } }

    Scaffold(
        topBar = { ToolBar(onNavClick = { /*TODO*/ }, actions = {}, text = "") },
        contentColor = MaterialTheme.colorScheme.onBackground,
        backgroundColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {

        }
    }

}