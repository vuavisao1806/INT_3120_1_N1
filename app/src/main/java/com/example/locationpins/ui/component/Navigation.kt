package com.example.locationpins.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationpins.ui.icon.LocationSocialIcons
import com.example.locationpins.ui.theme.LocationSocialTheme

/**
 * Location Social navigation bar item with icon and label content slots.
 *
 * @param selected Whether this item is selected.
 * @param onClick The callback to be invoked when this item is selected.
 * @param icon The item icon content.
 * @param modifier Modifier to be applied to this item.
 * @param label The item text label content.
 * @param selectedIcon The item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun RowScope.LocationSocialNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = LocationSocialNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = LocationSocialNavigationDefaults.navigationContentColor(),
            selectedTextColor = LocationSocialNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = LocationSocialNavigationDefaults.navigationContentColor(),
            indicatorColor = LocationSocialNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

/**
 * Location Social navigation bar is the wrapper of Wraps Material 3 [NavigationBar] with content slot.
 *
 * @param modifier Modifier to be applied to the navigation bar.
 * @param content Destinations inside the navigation bar.
 */
@Composable
fun LocationSocialNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        contentColor = LocationSocialNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

@Preview
@Composable
fun LocationSocialNavigationBarPreview() {
    val items = listOf("Newfeed", "Map", "Gallery", "User")
    val icons = listOf(
        LocationSocialIcons.NewfeedBorder,
        LocationSocialIcons.MapBorderIcon,
        LocationSocialIcons.GalleryBorder,
        LocationSocialIcons.PersonBorder,
    )
    val selectedIcons = listOf(
        LocationSocialIcons.Newfeed,
        LocationSocialIcons.MapIcon,
        LocationSocialIcons.Gallery,
        LocationSocialIcons.Person,
    )

    LocationSocialTheme {
        LocationSocialNavigationBar {
            items.forEachIndexed { index, item ->
                LocationSocialNavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

/**
 * Location Social navigation default values.
 */
object LocationSocialNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}