package com.dog.hustlehands

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import com.dog.hustlehands.domain.model.DomainHandLandmark
import com.dog.hustlehands.presentation.camera.CameraScreen
import com.dog.hustlehands.ui.theme.HustleHandsTheme

class MainActivity : ComponentActivity() {

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val landmarksState = mutableStateOf<List<DomainHandLandmark>>(emptyList())

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            onResult = { result ->
                val handLists = result.landmarks() // Correct method name
                val landmarks = handLists.flatMapIndexed { handIndex, handLandmarks ->
                    handLandmarks.mapIndexed { landmarkIndex, lm ->
                        DomainHandLandmark(
                            x = lm.x(),  // Use x() method
                            y = lm.y(),  // Use y() method
                            z = lm.z(),  // Use z() method
                            handIndex = handIndex,
                            landmarkIndex = landmarkIndex
                        )
                    }
                }
                landmarksState.value = landmarks
            },
            onError = { error ->
                error.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Hand detection error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        setContent {
            HustleHandsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(
                        handLandmarkerHelper = handLandmarkerHelper,
                        landmarksState = landmarksState
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handLandmarkerHelper.close()
    }
}