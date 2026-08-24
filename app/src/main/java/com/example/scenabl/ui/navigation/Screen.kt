package com.example.scenabl.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Main : Screen("main")
    object Profile : Screen("profile")
    object TitleDetails : Screen("titleDetails/{titleId}") {
        fun route(titleId: String) = "titleDetails/$titleId"
    }
    object Reservation : Screen("reservation/{performanceId}") {
        fun route(performanceId: String) = "reservation/$performanceId"
    }
    object MyLists : Screen("myLists")
    object MyReservations : Screen("myReservations")
}
