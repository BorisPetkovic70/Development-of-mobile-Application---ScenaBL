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
    object OrganizerDashboard : Screen("organizerDashboard")
    object OrganizerTitleForm : Screen("organizerTitleForm/{institutionId}?titleId={titleId}") {
        fun createRoute(institutionId: String) = "organizerTitleForm/$institutionId"
        fun editRoute(institutionId: String, titleId: String) = "organizerTitleForm/$institutionId?titleId=$titleId"
    }
    object OrganizerPerformanceForm : Screen("organizerPerformanceForm/{institutionId}/{titleId}") {
        fun route(institutionId: String, titleId: String) = "organizerPerformanceForm/$institutionId/$titleId"
    }
}
