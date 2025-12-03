package com.example.locationpins.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import com.example.locationpins.ui.component.LocationSocialBottomBar
import com.example.locationpins.ui.navigation.LocationSocialNavHost
import com.example.locationpins.ui.navigation.TopLevelDestination
import com.example.locationpins.ui.theme.LocationSocialTheme

@Composable
fun LocationSocialApp(
    appState: LocationSocialAppState,
    modifier: Modifier = Modifier,
) {
    LocationSocialTheme {
        LocationSocialInternalApp(
            appState = appState,
            modifier = modifier
        )
    }
}

@Composable
internal fun LocationSocialInternalApp(
    appState: LocationSocialAppState,
    modifier: Modifier = Modifier,
) {
    val currentDestination: NavDestination? = appState.currentDestination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentDestination?.route != TopLevelDestination.CAMERA.route) {
                LocationSocialBottomBar(
                    topDestinations = appState.topLevelDestinations,
                    currentDestination = currentDestination,
                    onDestinationClick = appState::navigateToTopLevelDestination,
                    onCameraClick = appState::navigateToCamera
                )
            }
        }
    ) { innerPadding ->
        LocationSocialNavHost(
            appState = appState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}