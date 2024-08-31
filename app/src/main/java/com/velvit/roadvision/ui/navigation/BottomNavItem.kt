package com.velvit.roadvision.ui.navigation

import com.velvit.roadvision.R

sealed class BottomNavItem(var title: String, var icon: Int, var screenRoute: String) {
    data object Home : BottomNavItem("Home", R.drawable.baseline_home_24, "home")
    data object Camera : BottomNavItem("Camera", R.drawable.baseline_videocam_24, "camera")
    data object Settings : BottomNavItem("Settings", R.drawable.baseline_settings_24, "settings")
}