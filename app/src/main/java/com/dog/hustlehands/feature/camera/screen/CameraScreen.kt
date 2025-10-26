package com.dog.hustlehands.feature.camera.screen

import android.annotation.SuppressLint
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.feature.camera.contract.CameraContract
import com.dog.hustlehands.feature.camera.data.CameraAnalyzer
import com.dog.hustlehands.feature.camera.data.CameraManager
import com.dog.hustlehands.feature.camera.screen.components.OverlayView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    state: CameraContract.State,
    cameraManager: CameraManager,
    lifecycleOwner: LifecycleOwner,
    onCameraReady: () -> Unit,
    onCaptureFrame: () -> Unit,
    onOverlayReady: (OverlayView) -> Unit,  // ✅ New callback
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    val overlayView = remember {
        OverlayView(context).also { onOverlayReady(it) }  // ✅ Notify controller
    }

    // ❌ Remove this - no longer needed
    // LaunchedEffect(state.landmarks) {
    //     overlayView.setLandmarks(state.landmarks)
    // }

    LaunchedEffect(Unit) {
        cameraManager.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onError = { /* handle error */ }
        )
        onCameraReady()
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                (previewView.parent as? ViewGroup)?.removeView(previewView)
                previewView
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                it.viewTreeObserver.addOnGlobalLayoutListener {
                    val previewWidth = it.width.toFloat()
                    val previewHeight = it.height.toFloat()
                    val streamRatio = 1f / 1f

                    val visibleImageHeight = previewWidth / streamRatio
                    val verticalPadding = (previewHeight - visibleImageHeight) / 2f

                    overlayView.setTransform(
                        previewWidth,
                        visibleImageHeight,
                        verticalPadding
                    )
                }
            }
        )

        AndroidView(
            factory = { ctx ->
                (overlayView.parent as? ViewGroup)?.removeView(overlayView)
                overlayView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onCaptureFrame,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Capture Frame")
        }
    }
}