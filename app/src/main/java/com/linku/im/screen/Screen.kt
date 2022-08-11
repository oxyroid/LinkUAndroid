package com.linku.im.screen


sealed class Screen(val route: String) {
    object MainScreen : Screen(route = "main_screen")
    object QueryScreen : Screen(route = "query_screen")
    object ChatScreen : Screen(route = "chat_screen")
    object EditScreen : Screen(route = "edit_screen")
    object LoginScreen : Screen("login_screen")
    object ProfileScreen : Screen("profile_screen")
    object PreviewScreen : Screen("preview_screen")

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
            route.startsWith(QueryScreen.route) -> QueryScreen
            route.startsWith(ChatScreen.route) -> ChatScreen
            route.startsWith(LoginScreen.route) -> LoginScreen
            route.startsWith(ProfileScreen.route) -> ProfileScreen
            route.startsWith(EditScreen.route) -> EditScreen
            route.startsWith(PreviewScreen.route) -> PreviewScreen
            else -> MainScreen
        }
    }
}