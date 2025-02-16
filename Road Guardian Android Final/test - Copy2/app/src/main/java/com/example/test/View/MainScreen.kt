package com.example.test.View


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.Controller.AuthViewModel
import com.example.test.Controller.AuthState
import com.example.test.View.Elements.BottomNavigationBar

//import com.example.test.pages.ReportPage
//import com.example.test.pages.ProfilePage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val internalNavController = rememberNavController()

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                println("Navigate: " + authState.value)
                navController.navigate("login")
            }
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(internalNavController, authViewModel)
        }
    ) { innerPadding ->
        NavHost(
            navController = internalNavController,
            startDestination = "home",
            Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreenPage(modifier, navController, authViewModel) }
            composable("report") { CameraPage(modifier, navController) }
            composable("navigationMapScreen") { NavigationMapPage(modifier, navController) }
        }
    }
}
