package com.example.test.View

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.Controller.AuthState
import com.example.test.Controller.AuthViewModel


@Composable
fun ResetPwPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState.value as AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()}
            else -> {Unit}
        }
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Reset PW Page", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") })

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password2,
            onValueChange = { password2 = it },
            label = { Text("Confirm Password") })

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {                              //Reset password
            println(password)
            println(password2)
            if (password != password2) {
                Toast.makeText(
                    context,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                authViewModel.resetPassword(password, context)
                //navController.navigate("login")
                //println("loginState: " + authState.value)
            }
        }) {
            Text(text = "Reset Password")
        }

    }
}