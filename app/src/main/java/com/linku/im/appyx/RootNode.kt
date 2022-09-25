package com.linku.im.appyx

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.chat.ChatViewModel
import com.linku.im.screen.create.CreateScreen
import com.linku.im.screen.edit.EditScreen
import com.linku.im.screen.introduce.IntroduceScreen
import com.linku.im.screen.introduce.IntroduceViewModel
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.main.MainViewModel
import com.linku.im.screen.query.QueryScreen
import com.linku.im.screen.query.QueryViewModel
import com.linku.im.screen.sign.SignScreen
import com.linku.im.screen.sign.SignViewModel
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalTheme

class RootNode(
    buildContext: BuildContext,
    private val backStack: BackStack<NavTarget> = BackStack(
        initialElement = NavTarget.Main,
        savedStateMap = buildContext.savedStateMap
    )
) : ParentNode<NavTarget>(
    navModel = backStack,
    buildContext = buildContext
) {
    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalBackStack provides backStack
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(LocalTheme.current.background)
                    .navigationBarsPadding()
            ) {
                Children(
                    navModel = backStack,
                    transitionHandler = rememberBackstackSlider(
                        transitionSpec = { tween(LocalDuration.current.medium) }
                    )
                )
            }
        }

    }

    override fun resolve(
        navTarget: NavTarget,
        buildContext: BuildContext
    ): Node {
        return when (navTarget) {
            NavTarget.Main -> node(buildContext) {
                val vm by (LocalContext.current as AppCompatActivity).viewModels<MainViewModel>()
                MainScreen(viewModel = vm)
            }
            is NavTarget.Chat -> node(buildContext) {
                val vm by (LocalContext.current as AppCompatActivity).viewModels<ChatViewModel>()
                ChatScreen(
                    cid = navTarget.cid,
                    viewModel = vm
                )
            }
            is NavTarget.Introduce -> node(buildContext) {
                val vm by (LocalContext.current as AppCompatActivity).viewModels<IntroduceViewModel>()
                IntroduceScreen(
                    uid = navTarget.uid,
                    viewModel = vm
                )
            }
            NavTarget.Create -> node(buildContext) {
//                val vm by (LocalContext.current as AppCompatActivity).viewModels<ChatViewModel>()
                CreateScreen()
            }
            is NavTarget.Edit -> node(buildContext) {
//                val vm by (LocalContext.current as AppCompatActivity).viewModels<ChatViewModel>()
                EditScreen(type = navTarget.type)
            }
            NavTarget.Query -> node(buildContext) {
                val vm by (LocalContext.current as AppCompatActivity).viewModels<QueryViewModel>()
                QueryScreen(viewModel = vm)
            }
            NavTarget.Sign -> node(buildContext) {
                val vm by (LocalContext.current as AppCompatActivity).viewModels<SignViewModel>()
                SignScreen(viewModel = vm)
            }
        }
    }
}