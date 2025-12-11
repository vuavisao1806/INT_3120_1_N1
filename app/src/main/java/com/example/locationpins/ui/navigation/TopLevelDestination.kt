package com.example.locationpins.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.locationpins.R
import com.example.locationpins.ui.icon.LocationSocialIcons
import com.example.locationpins.ui.theme.LocationSocialTheme

/**
 * Declare types for the top-level destinations in the application. It contains metadata about the destination
 * that is used in the top app bar and common navigation UI.
 *
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 * @param selectedIcon The icon to be displayed in the navigation UI when this destination is
 * selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this destination is
 * not selected.
 * @param route The route to use when navigating to this destination.
 * @param baseRoute The highest ancestor of this destination. Defaults to [route], meaning that
 * there is a single destination in that section of the app (no nested destinations).
 */
enum class TopLevelDestination(
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String,
    val baseRoute: String = route,
) {
    NEWFEED(
        iconTextId = R.string.bottom_nav_newfeed_title,
        titleTextId = R.string.top_bar_newfeed_title,
        selectedIcon = LocationSocialIcons.Newfeed,
        unselectedIcon = LocationSocialIcons.NewfeedBorder,
        route = "newfeed"
    ),
    MAP(
        iconTextId = R.string.bottom_nav_map_title,
        titleTextId = R.string.top_bar_map_title,
        selectedIcon = LocationSocialIcons.MapIcon,
        unselectedIcon = LocationSocialIcons.MapBorderIcon,
        route = "map"
    ),

    CAMERA(
        iconTextId = R.string.bottom_nav_camera_title,
        titleTextId = R.string.top_bar_camera_title,
        selectedIcon = LocationSocialIcons.Camera,
        unselectedIcon = LocationSocialIcons.CameraBorder,
        route = "camera"
    ),

    GALLERY(
        iconTextId = R.string.bottom_nav_gallery_title,
        titleTextId = R.string.top_bar_gallery_title,
        selectedIcon = LocationSocialIcons.Gallery,
        unselectedIcon = LocationSocialIcons.GalleryBorder,
        route = "gallery"
    ),

    USER(
        iconTextId = R.string.bottom_nav_user_title,
        titleTextId = R.string.top_bar_user_title,
        selectedIcon = LocationSocialIcons.Person,
        unselectedIcon = LocationSocialIcons.PersonBorder,
        route = "user"
    ),
    LOGIN(
        iconTextId = R.string.bottom_nav_login_title,
        titleTextId = R.string.top_bar_login_title,
        selectedIcon = LocationSocialIcons.Login,
        unselectedIcon = LocationSocialIcons.LoginBorder,
        route = "login"
    )
}