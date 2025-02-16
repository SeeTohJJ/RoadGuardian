package com.example.test.View.Elements

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn


// Location Textfield and GPS button for the HazardReportingPage
@Composable
fun LocationTextField(modifier: Modifier = Modifier, onLocationSelected: (String) -> Unit) {
    var locationText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher to request location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getCurrentLocation(context, coroutineScope) { address ->
                    locationText = address
                    onLocationSelected(address)
                }
            }
        }
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = locationText,
            onValueChange = { locationText = it },
            modifier = Modifier.weight(1f),
            label = { Text("Location") }
        )
        Button(onClick = {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }) {
            Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Get Location")

        }
    }
}

@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    coroutineScope: CoroutineScope,
    onAddressReceived: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(2000)
        .build()

    fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location? = locationResult.lastLocation
            if (location != null) {
                fetchAddressFromLocation(context, location.latitude, location.longitude, coroutineScope) { address ->
                    onAddressReceived(address)
                }
            } else {
                onAddressReceived("Location not available")
            }
            fusedLocationClient.removeLocationUpdates(this)
        }
    }, Looper.getMainLooper())
}

private fun fetchAddressFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    coroutineScope: CoroutineScope,
    onAddressReceived: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())

    coroutineScope.launch {
        try {
            // Use Dispatchers.IO to move the blocking call off the main thread
            val addresses = withContext(Dispatchers.IO) {
                geocoder.getFromLocation(latitude, longitude, 1)
            }
            val address = if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Unknown address"
            } else {
                "Unknown address"
            }
            onAddressReceived(address)
        } catch (e: Exception) {
            onAddressReceived("Failed to fetch address")
        }
    }
}
