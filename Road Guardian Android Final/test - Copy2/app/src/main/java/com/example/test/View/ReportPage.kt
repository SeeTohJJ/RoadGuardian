package com.example.test.View

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.test.View.Elements.HazardTypeDropdown
import com.example.test.View.Elements.LocationTextField
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// Report Hazard page for the app, where users can submit reports of hazards and send information to firebase
@SuppressLint("MissingPermission")
@Composable
fun ReportPage(modifier: Modifier = Modifier, navController: NavController, photoPath: String?) {
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return // Return if user is not authenticated

    var locationInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var typeInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    // Latitude and longitude state variables
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Error state for empty fields
    var locationError by remember { mutableStateOf(false) }
    var typeError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    // Fetch current location for location suggestion
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                locationInput = "Lat: $latitude, Lon: $longitude"
            }
        }
    }

    // Determine if the submit button should be enabled
    val isSubmitEnabled = locationInput.isNotBlank() && typeInput.isNotBlank() && descriptionInput.length<101

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Submit Report", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Image preview or placeholder
        Box(modifier = Modifier.wrapContentHeight()) {
            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Captured Image",
                    modifier = Modifier.size(300.dp).clip(RoundedCornerShape(16.dp))
                )
            } else {
                Text("No Image Available")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hazard type dropdown with error checking
        HazardTypeDropdown(onTypeSelected = { type ->
            typeInput = type
            typeError = type.isBlank()
        })
        if (typeError) Text("Please select a hazard type", color = androidx.compose.ui.graphics.Color.Red)

        Spacer(modifier = Modifier.height(16.dp))

        // Location input with error checking
        LocationTextField(onLocationSelected = { location ->
            locationInput = location
            locationError = location.isBlank()
        })
        if (locationError) Text("Please enter a location", color = androidx.compose.ui.graphics.Color.Red)

        Spacer(modifier = Modifier.height(16.dp))

        // Description TextField with error checking
        TextField(
            value = descriptionInput,
            onValueChange = {
                descriptionInput = it
                descriptionError = it.length > 100
            },
            label = { Text("Brief Description of Hazard") },
            isError = descriptionError,
            modifier = Modifier.fillMaxWidth()
        )
        if (descriptionError) Text("Description must not exceed 100 characters", color = androidx.compose.ui.graphics.Color.Red)

        Spacer(modifier = Modifier.height(16.dp))

        //Check if location is in Singapore
        fun isLocationWithinBounds(latitude: Double?, longitude: Double?): Boolean {
            if (latitude != null && longitude != null) {
                return (latitude in 1.1500..1.4833 && longitude in 103.6000..104.4167)
            }
            else return false
        }

        // Submit button with enabled state and error validation on click
        Button(
            onClick = {
                // Toast.makeText(context, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show() <- For Debugging Purposes
                if (locationInput.isBlank()) locationError = true
                if (typeInput.isBlank()) typeError = true
                if (descriptionInput.length > 100) descriptionError = true
                if (!isLocationWithinBounds(latitude,longitude)){
                    Toast.makeText(context, "Location cannot be outside of Singapore", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isSubmitEnabled) {
                    val storageRef = storage.reference.child("images/${userId}/${System.currentTimeMillis()}")

                    if (photoPath != null) {
                        val fileUri = Uri.fromFile(File(photoPath))
                        storageRef.putFile(fileUri)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    submitReportToRealtimeDatabase(
                                        locationInput,
                                        uri.toString(),
                                        typeInput,
                                        descriptionInput,
                                        latitude,
                                        longitude
                                    )
                                    Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                    navController.navigate("mainScreen")
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else if (selectedImageUri != null) {
                        storageRef.putFile(selectedImageUri!!)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    submitReportToRealtimeDatabase(
                                        locationInput,
                                        uri.toString(),
                                        typeInput,
                                        descriptionInput,
                                        latitude,
                                        longitude
                                    )
                                    Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        submitReportToRealtimeDatabase(
                            locationInput,
                            null,
                            typeInput,
                            descriptionInput,
                            latitude,
                            longitude
                        )
                        Toast.makeText(context, "Report submitted without image", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = isSubmitEnabled,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Submit")
        }
    }
}



fun submitReportToRealtimeDatabase(
    location: String,
    imageUrl: String?,
    typeInput: String,
    descriptionInput: String?,
    latitude: Double?,
    longitude: Double?
) {
    val database = FirebaseDatabase.getInstance()
    val reportsRef = database.getReference("Reports")

    // Format the current date and time
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentTime = dateFormat.format(Date())

    val reportData = mapOf(
        "Location" to location,
        "Photo" to imageUrl,
        "Timestamp" to currentTime, // Use formatted date and time
        "HazardType" to typeInput,
        "Description" to descriptionInput,
        "Latitude" to latitude,
        "Longitude" to longitude
    )

    // Generate a new key for each report
    val newReportRef = reportsRef.push()

    // Set the data at this new location
    newReportRef.setValue(reportData)
        .addOnSuccessListener {
            // Report successfully submitted
            println("Realtime Database Report Success")
        }
        .addOnFailureListener { e ->
            // Failed to submit report
            println("Realtime Database Report Failure: ${e.message}")
        }
}