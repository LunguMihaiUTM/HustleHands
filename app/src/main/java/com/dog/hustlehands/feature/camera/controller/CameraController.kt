package com.dog.hustlehands.feature.camera.controller

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.data.mediapipe.toDomain
import com.dog.hustlehands.feature.camera.CameraViewModel
import com.dog.hustlehands.feature.camera.contract.CameraContract
import com.dog.hustlehands.feature.camera.screen.CameraScreen

private const val TAG = "CameraController"

@Composable
fun CameraController() {
    val vm: CameraViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val helper = remember(context) {
        HandLandmarkerHelper(
            context = context,
            onResult = { result ->
                val landmarks = result.toDomain()
                vm.sendEvent(CameraContract.Event.LandmarksUpdated(landmarks))
            },
            onError = { e ->
                vm.sendEvent(CameraContract.Event.DetectionError(e.message ?: "Hand detection error"))
            }
        )
    }

    DisposableEffect(helper) {
        onDispose {
            helper.close()
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
        handLandmarkerHelper = helper
    )
}