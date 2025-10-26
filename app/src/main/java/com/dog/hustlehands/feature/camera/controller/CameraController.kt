package com.dog.hustlehands.feature.camera.controller

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.data.mediapipe.toDomain
import com.dog.hustlehands.feature.camera.CameraViewModel
import com.dog.hustlehands.feature.camera.contract.CameraContract
import com.dog.hustlehands.feature.camera.data.CameraManager
import com.dog.hustlehands.feature.camera.screen.CameraScreen
import com.dog.hustlehands.feature.camera.screen.components.OverlayView

private const val TAG = "CameraController"

// CameraController.kt
@Composable
fun CameraController() {
    val vm: CameraViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ Create overlay reference that can be updated directly
    val overlayViewRef = remember { mutableStateOf<OverlayView?>(null) }

    val helper = remember(context) {
        HandLandmarkerHelper(
            context = context,
            onResult = { result ->
                // ✅ FAST PATH: Update overlay directly without ViewModel
                overlayViewRef.value?.let { overlay ->
                    val landmarks = result.toDomain()
                    overlay.post { overlay.setLandmarks(landmarks) }
                }
            },
            onError = { e ->
                vm.sendEvent(CameraContract.Event.DetectionError(e.message ?: "Hand detection error"))
            }
        )
    }

    val cameraManager = remember(context) {
        CameraManager(context, helper)
    }

    DisposableEffect(helper) {
        onDispose {
            helper.close()
            cameraManager.cleanup()
        }
    }

    LaunchedEffect(Unit) {
        vm.sendEvent(CameraContract.Event.Initialize)
    }

    LaunchedEffect(vm.action) {
        vm.action.collect { action ->
            when (action) {
                is CameraContract.Action.ShowToast ->
                    Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    CameraScreen(
        state = state,
        cameraManager = cameraManager,
        lifecycleOwner = lifecycleOwner,
        onCameraReady = { vm.sendEvent(CameraContract.Event.CameraReady) },
        onCaptureFrame = { vm.sendEvent(CameraContract.Event.CaptureFrame) },
        onOverlayReady = { overlayViewRef.value = it }  // ✅ Pass overlay reference
    )
}