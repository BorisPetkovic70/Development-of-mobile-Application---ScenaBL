package com.example.scenabl.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Main : Screen("main")
    object Profile : Screen("profile")
}
