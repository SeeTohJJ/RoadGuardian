package com.example.test.View

import HazardProximityManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.Model.HazardViewModel
import com.google.android.gms.location.*
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

// Navigation map page for the app for drivers to see their current location
@Composable
fun NavigationMapPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    hazardViewModel: HazardViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val cameraPositionState = rememberCameraPositionState()
    // Observe hazard locations from the ViewModel
    val hazardLocations by hazardViewModel.hazardLocations.collectAsState()

    // State to hold the user's current location
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Set up location request
    val locationRequest = LocationRequest.create().apply {
        interval = 10000 // 10 seconds
        fastestInterval = 5000 // 5 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Check for location permissions
    LaunchedEffect(Unit) {
        hasLocationPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    // Get the last location from the result
                    locationResult.lastLocation?.let {
                        userLocation = LatLng(it.latitude, it.longitude)
                        // Update camera position to the new location
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 18f)
                    }
                }
            }, null)
        }

        hazardViewModel.fetchHazardData()
    }

    // Stop location updates when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            if (hasLocationPermission) {
                fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
            }
        }
    }

    // Call HazardProximityManager to monitor the user’s proximity to hazards
    HazardProximityManager(viewModel = hazardViewModel)

    Scaffold() { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            // Display markers for each hazard location
            hazardLocations.forEach { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Hazard Report",
                    snippet = "Hazard at ${location.latitude}, ${location.longitude}"
                )
            }


        }
    }
}
