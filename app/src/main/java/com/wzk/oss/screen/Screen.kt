package com.wzk.oss.screen

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object ChatScreen : Screen("chat_screen")
    object ListScreen : Screen("list_screen")
    object DetailScreen : Screen("detail_screen")
    object LoginScreen : Screen("login_screen")
    object ProfileScreen : Screen("profile_screen")
    object CartScreen : Screen("cart_screen")
    object InfoScreen : Screen("info_screen")
}
