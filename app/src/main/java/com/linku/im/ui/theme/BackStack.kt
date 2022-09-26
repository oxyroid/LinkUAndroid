package com.linku.im.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.bumble.appyx.navmodel.backstack.BackStack
import com.linku.im.appyx.target.NavTarget

val LocalBackStack = staticCompositionLocalOf<BackStack<NavTarget>> {
    error("No navController provided.")
}