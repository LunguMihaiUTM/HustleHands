package com.dog.hustlehands.presentation.camera

import android.annotation.SuppressLint
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.domain.model.DomainHandLandmark
import com.dog.hustlehands.presentation.components.LandmarkOverlay
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    handLandmarkerHelper: HandLandmarkerHelper,
    landmarksState: MutableState<List<DomainHandLandmark>>,
    previewSize: Size = Size(1280, 720)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->
                val previewView = androidx.camera.view.PreviewView(ctx)

                // Set scale type to fill and implementation mode
                previewView.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                previewView.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.COMPATIBLE

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val analyzer = CameraAnalyzer(handLandmarkerHelper)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(cameraExecutor, analyzer) }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        LandmarkOverlay(
            landmarks = landmarksState.value,
            previewWidth = previewSize.width,
            previewHeight = previewSize.height,
            modifier = Modifier.fillMaxSize()
        )
    }
}