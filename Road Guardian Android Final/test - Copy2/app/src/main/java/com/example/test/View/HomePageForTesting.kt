package com.example.test.View


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.Controller.AuthState
import com.example.test.Controller.AuthViewModel

@Composable
// WON'T HAVE ACCESS TO THIS IN FINAL BUILT
// FOR NAVIGATION TESTING PURPOSES ONLY
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

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

    Column(

        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text(text = "Home Page", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate("login")
            authViewModel.logout()
        }) {
            Text(text = "Logout")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("cameraPage")
        },
        ) {
            Text(text = "Camera")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("homeScreenPage")
        },
        ) {
            Text(text = "home screen page")
        }


    }
}