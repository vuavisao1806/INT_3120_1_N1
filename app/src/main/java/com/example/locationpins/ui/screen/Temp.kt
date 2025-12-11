package com.example.locationpins.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.flow.map

@Composable
fun rememberLocationSocialAppState(
    navController: NavHostController = rememberNavController(),
): LocationSocialAppState = remember(navController) {
    LocationSocialAppState(navController)
}

@Stable
class LocationSocialAppState(
    val navController: NavHostController,
) {
    // Các tab thật sự trên bottom nav: NEWFEED, MAP, (GALLERY sau), USER
    val topLevelDestinations: List<TopLevelDestination> = listOf(
        TopLevelDestination.NEWFEED,
        TopLevelDestination.MAP,
        // TopLevelDestination.GALLERY, // khi nào có màn thì bật lên
        TopLevelDestination.USER,
    )

    val currentDestination: NavDestination?
        @Composable get() =
            navController.currentBackStackEntryFlow
                .collectAsState(initial = navController.currentBackStackEntry)
                .value
                ?.destination

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val options = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        navController.navigate(destination.route, options)
    }

    fun navigateToCamera() {
        navController.navigate(TopLevelDestination.CAMERA.route)
    }
}
