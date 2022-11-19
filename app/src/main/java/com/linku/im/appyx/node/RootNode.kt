package com.linku.im.appyx.node

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import com.linku.im.appyx.target.NavTarget
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.chat.ChatViewModel
import com.linku.im.screen.edit.EditScreen
import com.linku.im.screen.introduce.IntroduceScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.setting.data.DataStorageSettingScreen
import com.linku.im.screen.setting.language.LanguageSettingScreen
import com.linku.im.screen.setting.notification.NotificationSettingScreen
import com.linku.im.screen.setting.safe.SafeSettingScreen
import com.linku.im.screen.setting.theme.ThemeSettingScreen
import com.linku.im.screen.sign.SignScreen
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration

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
            Children(
                navModel = backStack,
                transitionHandler = rememberBackstackSlider(
                    transitionSpec = { tween(LocalDuration.current.medium) }
                ),
                modifier = modifier
            )
        }
    }

    override fun resolve(
        navTarget: NavTarget,
        buildContext: BuildContext
    ): Node = when (navTarget) {
        NavTarget.Main -> node(buildContext) {
            MainScreen(
                modifier = it
            )
        }

        is NavTarget.ChatTarget -> node(buildContext) {
            val viewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                cid = navTarget.cid,
                viewModel = viewModel,
                modifier = it
            )
        }

        is NavTarget.Introduce -> node(buildContext) {
            IntroduceScreen(
                uid = navTarget.uid,
                modifier = it
            )
        }

        is NavTarget.Edit -> node(buildContext) {
            EditScreen(
                type = navTarget.type,
                modifier = it
            )
        }

        NavTarget.Sign -> node(buildContext) {
            SignScreen(modifier = it)
        }

        is NavTarget.Setting -> when (navTarget) {
            NavTarget.Setting.Theme -> node(buildContext) {
                ThemeSettingScreen(modifier = it)
            }

            NavTarget.Setting.DataSource -> node(buildContext) {
                DataStorageSettingScreen(modifier = it)
            }

            NavTarget.Setting.Language -> node(buildContext) {
                LanguageSettingScreen(modifier = it)
            }
            NavTarget.Setting.Notification -> node(buildContext) {
                NotificationSettingScreen(modifier = it)
            }
            NavTarget.Setting.Safe -> node(buildContext) {
                SafeSettingScreen(modifier = it)
            }
        }
    }
}
