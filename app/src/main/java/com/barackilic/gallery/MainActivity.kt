package com.barackilic.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barackilic.gallery.ui.common.GalleryNavBar
import com.barackilic.gallery.ui.navigation.Destination
import com.barackilic.gallery.ui.navigation.GalleryNavHost
import com.barackilic.gallery.ui.navigation.TopLevelTab
import com.barackilic.gallery.ui.permission.hasMediaPermissions
import com.barackilic.gallery.ui.theme.GalleryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryTheme {
                GalleryRoot()
            }
        }
    }
}

@Composable
private fun GalleryRoot() {
    val context = LocalContext.current
    // Picked once at first composition; subsequent permission changes are handled by
    // PermissionGate (inline fallback) inside content screens.
    val startDestination: Destination = remember(context) {
        if (hasMediaPermissions(context)) Destination.Photos else Destination.Permission
    }
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val hideBottomBar =
        currentDestination?.hasRoute(Destination.Viewer::class) == true ||
            currentDestination?.hasRoute(Destination.Permission::class) == true
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!hideBottomBar) {
                GalleryNavBar(
                    currentDestination = currentDestination,
                    onTabSelected = { tab -> navController.navigateToTab(tab) },
                )
            }
        },
    ) { innerPadding ->
        GalleryNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.destination) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
