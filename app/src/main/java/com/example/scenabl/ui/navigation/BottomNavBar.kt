package com.example.scenabl.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomNavDestination(val route: String, val label: String, val icon: ImageVector)

private val viewerDestinations = listOf(
    BottomNavDestination(Screen.Main.route, "Repertoar", Icons.Filled.Home),
    BottomNavDestination(Screen.MyLists.route, "Moje liste", Icons.Filled.Favorite),
    BottomNavDestination(Screen.MyReservations.route, "Rezervacije", Icons.AutoMirrored.Filled.List),
    BottomNavDestination(Screen.Profile.route, "Profil", Icons.Filled.Person)
)

private val organizerDestinations = listOf(
    BottomNavDestination(Screen.Main.route, "Repertoar", Icons.Filled.Home),
    BottomNavDestination(Screen.OrganizerDashboard.route, "Moj repertoar", Icons.AutoMirrored.Filled.List),
    BottomNavDestination(Screen.Profile.route, "Profil", Icons.Filled.Person)
)

/** Top-level routes reachable from the shared bottom navigation bar (NFR-USAB-002). */
val BOTTOM_NAV_ROUTES: Set<String> = (viewerDestinations + organizerDestinations).map { it.route }.toSet()

@Composable
fun ScenaBLBottomBar(
    currentRoute: String?,
    isOrganizer: Boolean,
    onNavigate: (String) -> Unit
) {
    val destinations = if (isOrganizer) organizerDestinations else viewerDestinations
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
