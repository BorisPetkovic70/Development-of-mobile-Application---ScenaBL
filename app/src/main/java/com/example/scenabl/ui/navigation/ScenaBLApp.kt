package com.example.scenabl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scenabl.di.AppContainer
import com.example.scenabl.ui.screens.AuthScreen
import com.example.scenabl.ui.screens.MainScreen
import com.example.scenabl.ui.screens.ProfileScreen
import com.example.scenabl.viewmodel.AuthViewModel
import com.example.scenabl.viewmodel.ProfileViewModel

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
            MainScreen(
                authRepository = appContainer.authRepository,
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onLoginClick = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
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
                    }
                )
            }
        }
    }
}
