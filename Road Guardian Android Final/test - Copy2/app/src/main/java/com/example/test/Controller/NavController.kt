package com.example.test.Controller

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.View.CameraPage
import com.example.test.View.HomePage
import com.example.test.View.HomeScreenPage
import com.example.test.View.LoginPage
import com.example.test.View.MainScreen
import com.example.test.View.NavigationMapPage
import com.example.test.View.RegisterPage
import com.example.test.View.ReportPage
import com.example.test.View.ResetPwEmailPage
import com.example.test.View.ResetPwPage
import com.example.test.View.TempPasswordLoginPage

// Responsible for navigation between pages
@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier, navController, authViewModel)
        }
        composable("register"){
            RegisterPage(modifier, navController, authViewModel)
        }
        composable("home"){
            HomePage(modifier, navController,authViewModel)
        }
        composable("resetPwPage"){
            ResetPwPage(modifier, navController,authViewModel)
        }
        composable("resetPwEmailPage"){
            ResetPwEmailPage(modifier, navController,authViewModel)
        }
        composable("tempPasswordLoginPage"){
            TempPasswordLoginPage(modifier, navController,authViewModel)
        }
        composable("cameraPage"){ // Reference CameraPage Composable
            CameraPage(modifier, navController)
        }
        composable("mainScreen"){
            MainScreen(modifier, navController, authViewModel)
        }
        composable("reportPage/{photoPath}") {
                backStackEntry ->
            val photoPath = backStackEntry.arguments?.getString("photoPath")
            ReportPage(modifier, photoPath = photoPath, navController = navController)
        }
        composable("homeScreenPage"){
            HomeScreenPage(modifier, navController, authViewModel)
        }
        composable("navigationMapPage"){
            NavigationMapPage(modifier, navController)
        }
    })

}