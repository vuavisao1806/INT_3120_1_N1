package com.example.locationpins.ui.navigation

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.locationpins.data.model.User
import com.example.locationpins.ui.screen.LocationSocialAppState
import com.example.locationpins.ui.screen.camera.DemoCameraScreen
import com.example.locationpins.ui.screen.map.MapScreen
import com.example.locationpins.ui.screen.newfeed.NewsFeedScreen
import com.example.locationpins.ui.screen.profile.ProfileMode
import com.example.locationpins.ui.screen.profile.ProfileScreen

@Composable
fun LocationSocialNavHost(
    appState: LocationSocialAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController

    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.NEWFEED.route,
        modifier = modifier
    ) {
        composable(route = TopLevelDestination.NEWFEED.route) {
            NewsFeedScreen()
        }

        composable(route = TopLevelDestination.MAP.route) {
            MapScreen()
        }

        composable(route = TopLevelDestination.CAMERA.route) {
            DemoCameraScreen()
        }

        composable(route = TopLevelDestination.GALLERY.route) {
            Log.i("GALLERY", "Test gallery")
            Text("See you soon")
        }

        composable(route = TopLevelDestination.USER.route) {
            ProfileScreen(
                user = User(
                    userId = "001",
                    userName = "tin chuan",
                    location = "Viet Nam",
                    avatarUrl = "di dau",
                    quote = "tin rat chuan",
                    name = "xin chao",
                    quantityPin = 1,
                    quantityReact = 1,
                    quantityComment = 1,
                    quantityContact = 1,
                    userEmail = "aaa@gmail.com",
                    phoneNum = "124151423",
                    website = "www.google.com"
                ),
                profileMode = ProfileMode.Self
//                viewModel = TODO(),
//                modifier = TODO()
            )
        }
    }
}