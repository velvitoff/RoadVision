package com.velvit.roadvision.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.velvit.roadvision.ui.screens.camera_screen.CameraScreen
import com.velvit.roadvision.ui.screens.HomeScreen
import com.velvit.roadvision.ui.screens.SettingsScreen

@Composable
fun NavigationGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.screenRoute,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(BottomNavItem.Home.screenRoute) {
            // Home screen composable
            HomeScreen()
        }
        composable(BottomNavItem.Camera.screenRoute) {
            // Search screen composable
            CameraScreen()
        }
        composable(BottomNavItem.Settings.screenRoute) {
            // Profile screen composable
            SettingsScreen()
        }
    }
}

