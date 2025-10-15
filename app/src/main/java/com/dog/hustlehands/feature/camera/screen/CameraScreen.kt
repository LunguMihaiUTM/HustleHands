package com.dog.hustlehands.feature.camera.screen

import android.annotation.SuppressLint
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.feature.camera.contract.CameraContract
import com.dog.hustlehands.feature.camera.data.CameraAnalyzer
import com.dog.hustlehands.feature.camera.screen.components.OverlayView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    state: CameraContract.State,
    handLandmarkerHelper: HandLandmarkerHelper,
    modifier: Modifier = Modifier,
    previewSize: Size = Size(640, 480)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }

    val overlayView = remember(context) {
        OverlayView(context)
    }

    LaunchedEffect(state.landmarks) {
        overlayView.setLandmarks(state.landmarks)
    }

    DisposableEffect(lifecycleOwner) {
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = CameraAnalyzer(handLandmarkerHelper)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(previewView.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, analyzer) }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            try {
                val provider = cameraProviderFuture.get()
                provider.unbindAll()
            } catch (_: Throwable) {
            }
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = { overlayView },
            modifier = Modifier.fillMaxSize()
        )
    }
}