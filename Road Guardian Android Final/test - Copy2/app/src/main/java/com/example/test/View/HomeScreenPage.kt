package com.example.test.View

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.Controller.AuthState
import com.example.test.Controller.AuthViewModel
import com.example.test.Model.HazardViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


// Home screen page for the app with the map of Singapore
@Composable
fun HomeScreenPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    hazardViewModel: HazardViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    val hazardLocations by hazardViewModel.hazardLocations.collectAsState()


    // Check for authentication state and navigate to login if unauthenticated
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
        hazardViewModel.fetchHazardData()
    }

    // Define an initial location for the map
    val singapore = LatLng(1.3521, 103.8198)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 12f)
    }



    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Road Guardian", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(10.dp))
        // Display Google Map
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            cameraPositionState = cameraPositionState,
        ) {
            // Optional: Add a marker
            hazardLocations.forEach { location ->
                Marker(
                    state = MarkerState(location),
                    title = "Hazard Report",
                    snippet = "Hazard at ${location.latitude}, ${location.longitude}"
                )
            }
        }
    }
}

@Composable
fun RequestLocationPermissions() {
    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            fineLocationPermission
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            coarseLocationPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission || !hasCoarseLocationPermission) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(fineLocationPermission, coarseLocationPermission),
                0
            )
        }
    }
}
