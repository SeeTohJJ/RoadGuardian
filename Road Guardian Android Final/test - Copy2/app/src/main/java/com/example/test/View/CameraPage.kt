package com.example.test.View

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Camera page for the ReportPage, Access through clicking report in the BottomNavigationBar
@Composable
fun CameraPage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    // Request permission to use the camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Launch the permission request
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // State for capturing errors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // AndroidView for the camera preview
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                previewView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Setup the camera
                cameraProviderFuture.addListener({
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Unbind all use cases before binding again
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        context as ComponentActivity,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Button to capture image
        Button(
            onClick = {
                imageCapture?.let {
                    takePicture(it, context, cameraExecutor, { errorMessage = it }, navController)
                } ?: run {
                    errorMessage = "ImageCapture is not ready"
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Capture Image")
        }

        // Show error message if available
        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }
    }


}

fun takePicture(imageCapture: ImageCapture, context: Context, executor: ExecutorService, onError: (String) -> Unit, navController: NavController) {
    val photoFile = File(context.getExternalFilesDir(null), "photo.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraXApp", "Photo capture failed: ${exception.message}", exception)
                onError("Photo capture failed: ${exception.message}")
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val photoPath = photoFile.absolutePath
                Log.d("CameraXApp", "Photo capture succeeded: $photoPath")

                // Ensure navigation happens on the main thread
                ContextCompat.getMainExecutor(context).execute {
                    navController.navigate("reportPage/${Uri.encode(photoPath)}")
                }
            }

        }
    )
}
