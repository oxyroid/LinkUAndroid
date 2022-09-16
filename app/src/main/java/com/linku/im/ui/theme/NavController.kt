package com.linku.im.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavHostController

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No navController provided.")
}