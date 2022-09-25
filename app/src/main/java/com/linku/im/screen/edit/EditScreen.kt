package com.linku.im.screen.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.linku.im.screen.introduce.IntroduceEvent
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalTheme

@Composable
fun EditScreen(
    type: IntroduceEvent.Edit.Type?
) {
    val navController = LocalBackStack.current
    LaunchedEffect(Unit) { type ?: run { navController.pop() } }

    Scaffold(
        topBar = { ToolBar(onNavClick = { /*TODO*/ }, actions = {}, text = "") },
        contentColor = LocalTheme.current.onBackground,
        backgroundColor = LocalTheme.current.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {

        }
    }

}