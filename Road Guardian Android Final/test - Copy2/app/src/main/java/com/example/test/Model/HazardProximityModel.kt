import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test.Model.HazardViewModel
import com.example.test.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

private val notifiedHazardLocations = mutableSetOf<LatLng>() // Track notified hazard locations

@Composable
fun HazardProximityManager(
    modifier: Modifier = Modifier,
    viewModel: HazardViewModel = hiltViewModel()
) {
    Log.d("HazardProximityManager", "Composable function started.")

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hazardLocations by viewModel.hazardLocations.collectAsState()

    // Check location permission
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Check for notification permission on Android 13+
        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasNotificationPermission) {
            Log.d("HazardProximityManager", "Notification permission not granted.")
        }
    }

    LaunchedEffect(hasLocationPermission, hazardLocations) {
        Log.d("HazardProximityManager", "Has location permission: $hasLocationPermission")
        if (hasLocationPermission) {
            Log.d("HazardProximityManager", "Starting location updates.")
            startLocationUpdates(fusedLocationClient, context) { userLocation ->
                Log.d("HazardProximityManager", "User location updated.")
                hazardLocations.forEach { hazardLocation ->
                    Log.d("HazardProximityManager", "Checking proximity for hazard: $hazardLocation")
                    checkProximityToHazard(userLocation, hazardLocation, context)
                }
            }
        } else {
            Log.d("HazardProximityManager", "Location permission not granted.")
            // You may want to prompt the user for location permission here
        }
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationChanged: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 10000 /* 10 seconds */
    ).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d("LocationCallback", "Location update received")
            for (location in locationResult.locations) {
                Log.d("LocationCallback", "User location: ${location.latitude}, ${location.longitude}")
                onLocationChanged(location)
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        Log.d("startLocationUpdates", "Location updates requested.")
    } else {
        Log.d("startLocationUpdates", "Location permission not granted in startLocationUpdates.")
    }
}

private fun checkProximityToHazard(
    userLocation: Location,
    hazardLocation: LatLng,
    context: Context
) {
    val hazardLocationObj = Location("").apply {
        latitude = hazardLocation.latitude
        longitude = hazardLocation.longitude
    }
    val distanceToHazard = userLocation.distanceTo(hazardLocationObj)
    val proximityThreshold = 500f // Adjust as needed

    Log.d("ProximityCheck", "User location: ${userLocation.latitude}, ${userLocation.longitude}")
    Log.d("ProximityCheck", "Hazard location: ${hazardLocation.latitude}, ${hazardLocation.longitude}")
    Log.d("ProximityCheck", "Distance to hazard: $distanceToHazard meters")

    if (distanceToHazard <= proximityThreshold) {
        Log.d("ProximityCheck", "User is within proximity threshold")
        if (!notifiedHazardLocations.contains(hazardLocation)) {
            sendNotification(context, "You are close to a hazard location!")
            notifiedHazardLocations.add(hazardLocation) // Add to notified set
        }
    } else {
        Log.d("ProximityCheck", "User is outside proximity threshold")
    }
}

@SuppressLint("MissingPermission")
private fun sendNotification(context: Context, message: String) {
    val channelId = "hazard_proximity_channel"
    val notificationId = (System.currentTimeMillis() % 10000).toInt() // Unique notification ID

    // Step 1: Create a notification channel (for Android 8.0 and above)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelName = "Hazard Proximity Alerts"
        val channelDescription = "Alerts when you are near a hazard location"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("Notification", "Notification channel created.")
    }

    // Step 2: Build the notification
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Hazard Alert")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH) // For Android 7.1 and lower
        .setAutoCancel(true) // Dismiss notification when tapped

    // Step 3: Display the notification
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, notificationBuilder.build())
    }

    Log.d("Notification", "Notification sent: $message")
}
