package com.velvit.roadvision.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.velvit.roadvision.Constants.LABELS_PATH
import com.velvit.roadvision.Constants.MODEL_PATH
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.velvit.roadvision.util.detector.BoundingBox
import com.velvit.roadvision.util.detector.Detector

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen()  {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    var time by remember { mutableStateOf("0 ms") }

    val detectorListener = object : Detector.DetectorListener {
        override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
            time = "$inferenceTime ms"
            //boundingBoxes = boundingBoxesList
        }

        override fun onEmptyDetect() {
            //boundingBoxes = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("I228","LAUNCH CAMERA SCREEN")
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
        Box(
            modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            CameraPreview(context, lifecycleOwner, detectorListener)
            Text(
                text = time,
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.TopEnd),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    detectorListener: Detector.DetectorListener,
) {
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var detector by remember { mutableStateOf<Detector?>(null) }

    LaunchedEffect(Unit) {
        detector = Detector(context, MODEL_PATH, LABELS_PATH, detectorListener) {
            showToast(context, it)
        }
        Log.d("I228", "detector is initialized $detector")
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()
        onDispose {
            cameraProvider.unbindAll()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            detector?.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            previewView.apply {
                // Initialize the camera provider and bind the lifecycle
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, lifecycleOwner, this, cameraExecutor, detector)
                }, ContextCompat.getMainExecutor(context))
            }
        }
    )
}

private fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraExecutor: ExecutorService,
    detector: Detector?
) {
    val preview = Preview.Builder()
        .build()
        .also {
            it.surfaceProvider = previewView.surfaceProvider
        }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        Log.d("I228", "Start try")
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        Log.d("I228", "Analyzer built")
        Log.d("I228", "detector is $detector")

        if(detector != null) {
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                Log.d("I228", "Image received for analysis")
                val bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
                imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

                val matrix = Matrix().apply {
                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                }

                val rotatedBitmap = Bitmap.createBitmap(
                    bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
                )

                detector.detect(rotatedBitmap)
                imageProxy.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    } catch (e: Exception) {
        Log.e("CameraPreview", "Use case binding failed", e)
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}