package com.linku.im.screen


sealed class Screen(
    val route: String
) {
    object MainScreen : Screen(route = "main_screen")

    object ChatScreen : Screen(route = "chat_screen")

    object LoginScreen : Screen("login_screen")

    object ProfileScreen : Screen("profile_screen")

    object InfoScreen : Screen("info_screen")

    fun args(vararg args: String) = buildString {
        append(route)
        args.forEach { append("/{$it}") }
    }

    fun withArgs(vararg args: Any) = buildString {
        append(route)
        args.forEach { append("/$it") }
    }
}

fun main() {
    println(Screen.ChatScreen.args("cid"))
    println(Screen.ChatScreen.withArgs("cid"))
}
