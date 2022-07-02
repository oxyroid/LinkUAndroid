package com.linku.im.screen


sealed class Screen(
    val route: String
) {
    object MainScreen : Screen(
        route = "main_screen",
    )

    object ChatScreen : Screen(
        route = "chat_screen",
    )

    object LoginScreen : Screen(
        "login_screen",
    )

    object ProfileScreen : Screen(
        "profile_screen",
    )

    object InfoScreen : Screen(
        "info_screen",
    )
}
