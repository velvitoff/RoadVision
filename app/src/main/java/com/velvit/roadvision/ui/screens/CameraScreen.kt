package com.velvit.roadvision.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.velvit.roadvision.Constants.LABELS_PATH
import com.velvit.roadvision.Constants.MODEL_PATH
import com.velvit.roadvision.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen()  {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    if (!cameraPermissionState.status.isGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Camera permission is required to use the camera.")
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text(text = "Request Permission")
            }
        }
    } else {
        CameraPreview(context, lifecycleOwner)
    }
}

@Composable
fun CameraPreview(context: Context, lifecycleOwner: LifecycleOwner) {
    val previewView = remember { PreviewView(context) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            previewView.apply {
                // Initialize the camera provider and bind the lifecycle
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, lifecycleOwner, this)
                }, ContextCompat.getMainExecutor(context))
            }
        }
    )
}

private fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
) {
    val preview = Preview.Builder()
        .build()
        .also {
            it.surfaceProvider = previewView.surfaceProvider
        }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Unbind all use cases before rebinding
        cameraProvider.unbindAll()

        // Bind the camera to the lifecycle
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )
    } catch (e: Exception) {
        Log.e("CameraPreview", "Use case binding failed", e)
    }
}
