package com.example.test.View.Elements

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.test.Controller.AuthViewModel


// Bottom navigation bar for the MainScreen
@Composable
fun BottomNavigationBar(navController: NavController, authViewModel: AuthViewModel) {
    val items = listOf(
        NavItem("Map", Icons.Default.Map, "home"),
        NavItem("Navigation", Icons.Default.DirectionsCar, "navigationMapScreen"),
        NavItem("Report", Icons.Default.Add, "report"),
        NavItem("Signout", Icons.AutoMirrored.Filled.Logout, "signout")
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {

                    if(item.route == "signout"){
                        authViewModel.logout()
                    }
                    else{
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)
