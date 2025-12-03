package com.example.locationpins.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.locationpins.ui.navigation.TopLevelDestination

@Composable
fun rememberLocationSocialAppState(
    navController: NavHostController = rememberNavController()
) : LocationSocialAppState = remember(navController) { LocationSocialAppState(navController) }

@Stable
class LocationSocialAppState(
    val navController: NavHostController
) {
    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            // Collect the currentBackStackEntryFlow as a state
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }


    val topLevelDestinations: List<TopLevelDestination> = listOf(
        TopLevelDestination.NEWFEED,
        TopLevelDestination.MAP,
//        TopLevelDestination.CAMERA,
        TopLevelDestination.GALLERY,
        TopLevelDestination.USER,
    )

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to avoid building up a large stack of
                // destinations on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when re-selecting the same item
                launchSingleTop = true
                // Restore state when re-selecting a previously selected item
                restoreState = true
            }
            navController.navigate(topLevelDestination.route, topLevelNavOptions)
        }
    }

//    Using when separating the camera button on bottom navigation
    fun navigateToCamera() {
        navController.navigate(TopLevelDestination.CAMERA.route)
    }
}