package com.example.locationpins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.ui.component.GridPostPreview
import com.example.locationpins.ui.component.PostPreviewForNewsFeed
import com.example.locationpins.ui.screen.camera.DemoCameraScreen
import com.example.locationpins.ui.screen.newfeed.NewsFeedScreen
import com.example.locationpins.ui.theme.LocationPinsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationPinsTheme {
                DemoCameraScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocationPinsTheme {
        Greeting("Android")
    }
}