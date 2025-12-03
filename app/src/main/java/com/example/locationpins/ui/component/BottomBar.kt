package com.example.locationpins.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.example.locationpins.ui.navigation.TopLevelDestination

@Composable
fun LocationSocialBottomBar(
    topDestinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onDestinationClick: (TopLevelDestination) -> Unit,
    onCameraClick: () -> Unit
) {
    val halfSize = topDestinations.size / 2

    Box {
        LocationSocialNavigationBar(
            modifier = Modifier
        ) {
            topDestinations.take(halfSize).forEach { destination ->
                val selected = currentDestination
                    ?.route
                    ?.startsWith(destination.baseRoute) == true

                LocationSocialNavigationBarItem(
                    selected = selected,
                    onClick = { onDestinationClick(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = stringResource(destination.iconTextId)
                        )
                    },
                    label = { Text(text = stringResource(destination.iconTextId)) },
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = stringResource(destination.iconTextId)
                        )
                    },
                    alwaysShowLabel = true
                )
            }

            // Empty space for FAB
            Spacer(modifier = Modifier.weight(1f))

            topDestinations.drop(halfSize).forEach { destination ->
                val selected = currentDestination
                    ?.route
                    ?.startsWith(destination.baseRoute) == true

                LocationSocialNavigationBarItem(
                    selected = selected,
                    onClick = { onDestinationClick(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = stringResource(destination.iconTextId)
                        )
                    },
                    label = { Text(text = stringResource(destination.iconTextId)) },
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = stringResource(destination.iconTextId)
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }

        FloatingActionButton(
            onClick = onCameraClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = androidx.compose.foundation.shape.CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = TopLevelDestination.CAMERA.selectedIcon,
                contentDescription = stringResource(TopLevelDestination.CAMERA.iconTextId),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}