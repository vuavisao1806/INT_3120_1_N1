package com.example.locationpins.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import com.example.locationpins.ui.navigation.TopLevelDestination
import kotlinx.coroutines.selects.select

@Composable
fun LocationSocialBottomBar(
    topDestinations: List<TopLevelDestination>,
    currentDestination: NavDestination?,
    onDestinationClick: (TopLevelDestination) -> Unit,
    onCameraClick: () -> Unit
) {
    Box {
        LocationSocialNavigationBar(
            modifier = Modifier.height(72.dp) // temporary
        ) {
            topDestinations.forEach { destination ->
                val selected = currentDestination
                    ?.route
                    ?.startsWith(destination.baseRoute) == true // use for nested navigation


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
                .align(alignment = Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(72.dp)
        ) {
            Icon(
                imageVector = TopLevelDestination.CAMERA.selectedIcon,
                contentDescription = stringResource(TopLevelDestination.CAMERA.iconTextId)
            )
        }
    }
}