package com.example.locationpins.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.locationpins.ui.screen.LocationSocialAppState
import com.example.locationpins.ui.screen.camera.CameraWithPermission
import com.example.locationpins.ui.screen.createPost.CreatePostScreen
import com.example.locationpins.ui.screen.gallery.GalleryScreen
import com.example.locationpins.ui.screen.listpostfrommapscreen.ListPostFromMapScreen
import com.example.locationpins.ui.screen.login.CurrentUser
import com.example.locationpins.ui.screen.login.LoginView
import com.example.locationpins.ui.screen.map.MapScreen
import com.example.locationpins.ui.screen.newfeed.NewsFeedScreen
import com.example.locationpins.ui.screen.postDetail.PostDetailScreen
import com.example.locationpins.ui.screen.profile.EditProfileScreen
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
        composable(
            route = "${TopLevelDestination.NEWFEED.route}?tag={tag}",
            arguments = listOf(
                navArgument("tag") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag")
            NewsFeedScreen(
                initialTag = tag,
                onPostPress = { post ->
                    navController.navigate("post_detail/${post.postId}")
                }
            )
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
                onNavigateBack = { navController.popBackStack() },
                onClickUserName = { userId -> navController.navigate("user/${userId}") },
                navController = navController
            )
        }

        // 1. MapScreen: Nơi gọi navigate
        composable(route = TopLevelDestination.MAP.route) {
            MapScreen(
                onPinPress = { pinId ->
                    navController.navigate("pin_feed/${pinId}")
                }
            )
        }

        // 2. PinFeed: Nơi khai báo và nhận dữ liệu (Giống hệt post_detail)
        composable(
            route = "pin_feed/{pinId}",
            arguments = listOf(
                navArgument("pinId") { type = NavType.IntType } // Khai báo nhận Int
            )
        ) { backStackEntry ->
            // Lấy ID từ trên đường dẫn xuống
            val pinId = backStackEntry.arguments?.getInt("pinId") ?: -1
            // Gọi cái Wrapper đã viết ở Bước 1
            ListPostFromMapScreen(
                pinId = pinId,
                onPostPress = { post ->
                    // Vẫn cho phép bấm vào bài viết để xem chi tiết
                    navController.navigate("post_detail/${post.postId}")
                },
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
                    user = CurrentUser.currentUser!!
                )
            }
        }

        composable(route = TopLevelDestination.GALLERY.route) {
            GalleryScreen(onPostPress = { post ->
                // Navigate tới PostDetail với postId
                navController.navigate("post_detail/${post.postId}")
            })
        }

        composable(
            route = "${TopLevelDestination.USER.route}/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            ProfileScreen(
                userId = userId,
                onEditClick = { navController.navigate("edit_profile") },
                onProfileClick = {userId -> navController.navigate("user/${userId}")},
                onPressPost = { post ->
                    navController.navigate("post_detail/${post.postId}")
                })
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

        composable(route = TopLevelDestination.EDIT_PROFILE.route) {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}