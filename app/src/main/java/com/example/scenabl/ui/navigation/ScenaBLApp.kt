package com.example.scenabl.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scenabl.data.model.UserRole
import com.example.scenabl.di.AppContainer
import com.example.scenabl.ui.components.OfflineBanner
import com.example.scenabl.ui.components.rememberIsOnline
import com.example.scenabl.ui.screens.AuthScreen
import com.example.scenabl.ui.screens.HomeScreen
import com.example.scenabl.ui.screens.MyListsScreen
import com.example.scenabl.ui.screens.MyReservationsScreen
import com.example.scenabl.ui.screens.OrganizerDashboardScreen
import com.example.scenabl.ui.screens.OrganizerPerformanceFormScreen
import com.example.scenabl.ui.screens.OrganizerTitleFormScreen
import com.example.scenabl.ui.screens.ProfileScreen
import com.example.scenabl.ui.screens.ReservationScreen
import com.example.scenabl.ui.screens.TitleDetailsScreen
import com.example.scenabl.viewmodel.AuthViewModel
import com.example.scenabl.viewmodel.HomeViewModel
import com.example.scenabl.viewmodel.MyListsViewModel
import com.example.scenabl.viewmodel.MyReservationsViewModel
import com.example.scenabl.viewmodel.OrganizerPerformanceFormViewModel
import com.example.scenabl.viewmodel.OrganizerTitleFormViewModel
import com.example.scenabl.viewmodel.OrganizerViewModel
import com.example.scenabl.viewmodel.ProfileViewModel
import com.example.scenabl.viewmodel.ReservationViewModel
import com.example.scenabl.viewmodel.TitleDetailsViewModel

@Composable
fun ScenaBLApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val startDestination =
        if (appContainer.authRepository.currentUserId != null) Screen.Main.route else Screen.Auth.route

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentUserId = appContainer.authRepository.currentUserId

    // Role never changes after account creation (REQ-AUTH-003), so a one-shot fetch per login is enough.
    val userRole by produceState<String?>(initialValue = null, currentUserId) {
        value = currentUserId?.let { appContainer.userRepository.getUser(it).getOrNull()?.role }
    }

    val showBottomBar = currentUserId != null && currentRoute in BOTTOM_NAV_ROUTES
    val isOnline = rememberIsOnline()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ScenaBLBottomBar(
                    currentRoute = currentRoute,
                    isOrganizer = userRole == UserRole.ORGANIZER,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            if (!isOnline) OfflineBanner()

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Auth.route) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { AuthViewModel(appContainer.authRepository, appContainer.userRepository) }
                        }
                    )
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthenticated = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        },
                        onGuestContinue = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Main.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                HomeViewModel(
                                    appContainer.performanceRepository,
                                    appContainer.titleRepository,
                                    appContainer.userRepository,
                                    appContainer.reviewRepository
                                )
                            }
                        }
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        isLoggedIn = currentUserId != null,
                        onTitleClick = { titleId -> navController.navigate(Screen.TitleDetails.route(titleId)) },
                        onLoginClick = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.TitleDetails.route,
                    arguments = listOf(navArgument("titleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val titleId = backStackEntry.arguments?.getString("titleId").orEmpty()
                    val titleDetailsViewModel: TitleDetailsViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                TitleDetailsViewModel(
                                    titleId,
                                    currentUserId,
                                    appContainer.titleRepository,
                                    appContainer.performanceRepository,
                                    appContainer.reviewRepository,
                                    appContainer.userRepository,
                                    appContainer.userListRepository
                                )
                            }
                        }
                    )
                    TitleDetailsScreen(
                        viewModel = titleDetailsViewModel,
                        isLoggedIn = currentUserId != null,
                        onBack = { navController.popBackStack() },
                        onReserveClick = { performanceId -> navController.navigate(Screen.Reservation.route(performanceId)) },
                        onLoginRequired = { navController.navigate(Screen.Auth.route) }
                    )
                }

                composable(
                    route = Screen.Reservation.route,
                    arguments = listOf(navArgument("performanceId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val performanceId = backStackEntry.arguments?.getString("performanceId").orEmpty()
                    val uid = currentUserId
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        val reservationViewModel: ReservationViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer {
                                    ReservationViewModel(
                                        performanceId,
                                        uid,
                                        appContainer.reservationRepository,
                                        appContainer.performanceRepository,
                                        appContainer.titleRepository
                                    )
                                }
                            }
                        )
                        ReservationScreen(
                            viewModel = reservationViewModel,
                            onBack = { navController.popBackStack() },
                            onReserved = { navController.popBackStack() }
                        )
                    }
                }

                composable(Screen.Profile.route) {
                    val uid = currentUserId
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        val profileViewModel: ProfileViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer {
                                    ProfileViewModel(uid, appContainer.userRepository, appContainer.authRepository)
                                }
                            }
                        )
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onLoggedOut = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable(Screen.MyLists.route) {
                    val uid = currentUserId
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        val myListsViewModel: MyListsViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { MyListsViewModel(uid, appContainer.userListRepository, appContainer.titleRepository) }
                            }
                        )
                        MyListsScreen(
                            viewModel = myListsViewModel,
                            onTitleClick = { titleId -> navController.navigate(Screen.TitleDetails.route(titleId)) }
                        )
                    }
                }

                composable(Screen.MyReservations.route) {
                    val uid = currentUserId
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        val myReservationsViewModel: MyReservationsViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer {
                                    MyReservationsViewModel(
                                        uid,
                                        appContainer.reservationRepository,
                                        appContainer.performanceRepository,
                                        appContainer.titleRepository
                                    )
                                }
                            }
                        )
                        MyReservationsScreen(viewModel = myReservationsViewModel)
                    }
                }

                composable(Screen.OrganizerDashboard.route) {
                    val uid = currentUserId
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        val organizerViewModel: OrganizerViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer {
                                    OrganizerViewModel(
                                        uid,
                                        appContainer.userRepository,
                                        appContainer.titleRepository,
                                        appContainer.performanceRepository
                                    )
                                }
                            }
                        )
                        OrganizerDashboardScreen(
                            viewModel = organizerViewModel,
                            onCreateTitle = { institutionId -> navController.navigate(Screen.OrganizerTitleForm.createRoute(institutionId)) },
                            onEditTitle = { institutionId, titleId ->
                                navController.navigate(Screen.OrganizerTitleForm.editRoute(institutionId, titleId))
                            },
                            onAddPerformance = { institutionId, titleId ->
                                navController.navigate(Screen.OrganizerPerformanceForm.route(institutionId, titleId))
                            }
                        )
                    }
                }

                composable(
                    route = Screen.OrganizerTitleForm.route,
                    arguments = listOf(
                        navArgument("institutionId") { type = NavType.StringType },
                        navArgument("titleId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val institutionId = backStackEntry.arguments?.getString("institutionId").orEmpty()
                    val titleId = backStackEntry.arguments?.getString("titleId")
                    val titleFormViewModel: OrganizerTitleFormViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { OrganizerTitleFormViewModel(titleId, institutionId, appContainer.titleRepository) }
                        }
                    )
                    OrganizerTitleFormScreen(
                        viewModel = titleFormViewModel,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.OrganizerPerformanceForm.route,
                    arguments = listOf(
                        navArgument("institutionId") { type = NavType.StringType },
                        navArgument("titleId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val institutionId = backStackEntry.arguments?.getString("institutionId").orEmpty()
                    val titleId = backStackEntry.arguments?.getString("titleId").orEmpty()
                    val performanceFormViewModel: OrganizerPerformanceFormViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                OrganizerPerformanceFormViewModel(titleId, institutionId, appContainer.performanceRepository)
                            }
                        }
                    )
                    OrganizerPerformanceFormScreen(
                        viewModel = performanceFormViewModel,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
