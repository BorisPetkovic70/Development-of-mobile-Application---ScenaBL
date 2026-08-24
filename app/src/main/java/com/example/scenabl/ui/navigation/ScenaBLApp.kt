package com.example.scenabl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scenabl.di.AppContainer
import com.example.scenabl.ui.screens.AuthScreen
import com.example.scenabl.ui.screens.HomeScreen
import com.example.scenabl.ui.screens.MyListsScreen
import com.example.scenabl.ui.screens.MyReservationsScreen
import com.example.scenabl.ui.screens.ProfileScreen
import com.example.scenabl.ui.screens.ReservationScreen
import com.example.scenabl.ui.screens.TitleDetailsScreen
import com.example.scenabl.viewmodel.AuthViewModel
import com.example.scenabl.viewmodel.HomeViewModel
import com.example.scenabl.viewmodel.MyListsViewModel
import com.example.scenabl.viewmodel.MyReservationsViewModel
import com.example.scenabl.viewmodel.ProfileViewModel
import com.example.scenabl.viewmodel.ReservationViewModel
import com.example.scenabl.viewmodel.TitleDetailsViewModel

@Composable
fun ScenaBLApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val startDestination =
        if (appContainer.authRepository.currentUserId != null) Screen.Main.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDestination) {
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
                isLoggedIn = appContainer.authRepository.currentUserId != null,
                onTitleClick = { titleId -> navController.navigate(Screen.TitleDetails.route(titleId)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
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
            val currentUserId = appContainer.authRepository.currentUserId
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
            val uid = appContainer.authRepository.currentUserId
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
            val uid = appContainer.authRepository.currentUserId
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
                    },
                    onMyListsClick = { navController.navigate(Screen.MyLists.route) },
                    onMyReservationsClick = { navController.navigate(Screen.MyReservations.route) }
                )
            }
        }

        composable(Screen.MyLists.route) {
            val uid = appContainer.authRepository.currentUserId
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
                    onBack = { navController.popBackStack() },
                    onTitleClick = { titleId -> navController.navigate(Screen.TitleDetails.route(titleId)) }
                )
            }
        }

        composable(Screen.MyReservations.route) {
            val uid = appContainer.authRepository.currentUserId
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
                MyReservationsScreen(
                    viewModel = myReservationsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
