package com.example.locationpins.ui.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.locationpins.data.model.User
import com.example.locationpins.data.model.UserMock
import com.example.locationpins.ui.screen.LocationSocialAppState
import com.example.locationpins.ui.screen.camera.CameraWithPermission
import com.example.locationpins.ui.screen.createPost.CreatePostScreen
import com.example.locationpins.ui.screen.gallery.GalleryScreen
import com.example.locationpins.ui.screen.login.LoginScreen
import com.example.locationpins.ui.screen.login.LoginView
import com.example.locationpins.ui.screen.map.MapScreen
import com.example.locationpins.ui.screen.newfeed.NewsFeedScreen
import com.example.locationpins.ui.screen.postDetail.PostDetailScreen
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
        startDestination = TopLevelDestination.LOGIN.route,
        modifier = modifier
    ) {
        composable(route = TopLevelDestination.NEWFEED.route) {
            NewsFeedScreen(
                onPostPress = { post ->
                    // Navigate tới PostDetail với postId
                    navController.navigate("post_detail/${post.postId}")
                }
            )
        }

        composable(route = TopLevelDestination.MAP.route) {
            MapScreen()
        }
        composable(
            route = "post_detail/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
            )
        }



        composable(route = TopLevelDestination.CAMERA.route) {
            var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

            if (capturedImageUri == null) {
                CameraWithPermission(
                    onImageCaptured = { uri -> capturedImageUri = uri },
                    onCancel = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CreatePostScreen(
                    initialImageUri = capturedImageUri,
                    onNavigateBack = { navController.popBackStack() },
                    user = UserMock.sampleUser.first(),
                    pinId = 5,
                )
            }
        }

        composable(route = TopLevelDestination.GALLERY.route) {
            GalleryScreen(onPostPress = { post ->
                // Navigate tới PostDetail với postId
                navController.navigate("post_detail/${post.postId}")
            })
        }

        composable(route = TopLevelDestination.USER.route) {
            ProfileScreen(
                user = User(
                    userId = 1,
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

        composable(route = TopLevelDestination.LOGIN.route) {
            LoginView(
                onLoginSuccess = {
                    navController.navigate("newfeed") {
                        popUpTo(TopLevelDestination.LOGIN.route) {
                            inclusive = true
                        }
                    }
                })
        }
    }
}