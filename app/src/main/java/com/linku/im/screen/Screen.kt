package com.linku.im.screen


sealed class Screen(val route: String) {
    object MainScreen : Screen(route = "main_screen")
    object ChatScreen : Screen(route = "chat_screen")
    object LoginScreen : Screen("login_screen")
    object ProfileScreen : Screen("profile_screen")
    object InfoScreen : Screen("info_screen")

    fun buildArgs(vararg args: String) = buildString {
        append(route)
        args.forEach { append("/{$it}") }
    }

    fun withArgs(vararg args: Any) = buildString {
        append(route)
        args.forEach { append("/$it") }
    }

    companion object {
        fun valueOf(route: String) = when {
            route.startsWith(MainScreen.route) -> MainScreen
            route.startsWith(ChatScreen.route) -> ChatScreen
            route.startsWith(LoginScreen.route) -> LoginScreen
            route.startsWith(ProfileScreen.route) -> ProfileScreen
            route.startsWith(InfoScreen.route) -> InfoScreen
            else -> MainScreen
        }
    }
}