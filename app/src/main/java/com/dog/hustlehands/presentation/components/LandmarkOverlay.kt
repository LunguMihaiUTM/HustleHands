package com.dog.hustlehands.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.dog.hustlehands.domain.model.DomainHandLandmark

@Composable
fun LandmarkOverlay(
    landmarks: List<DomainHandLandmark>,
    previewWidth: Int,
    previewHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (landmarks.isEmpty()) return@Canvas

        // Group landmarks by hand
        val handGroups = landmarks.groupBy { it.handIndex }

        handGroups.forEach { (handIndex, handLandmarks) ->
            val color = if (handIndex == 0) Color.Green else Color.Blue

            // Draw landmarks as circles
            handLandmarks.forEach { landmark ->
                val x = landmark.x * size.width
                val y = landmark.y * size.height

                drawCircle(
                    color = color,
                    radius = 8f,
                    center = Offset(x, y)
                )
            }

            // Draw connections between landmarks
            drawHandConnections(handLandmarks, color)
        }
    }
}

private fun DrawScope.drawHandConnections(
    landmarks: List<DomainHandLandmark>,
    color: Color
) {
    // Create a map for quick lookup
    val landmarkMap = landmarks.associateBy { it.landmarkIndex }

    // Hand landmark connections based on MediaPipe hand model
    val connections = listOf(
        // Thumb
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        // Index finger
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        // Middle finger
        0 to 9, 9 to 10, 10 to 11, 11 to 12,
        // Ring finger
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        // Pinky
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        // Palm connections
        5 to 9, 9 to 13, 13 to 17
    )

    connections.forEach { (from, to) ->
        val fromLandmark = landmarkMap[from]
        val toLandmark = landmarkMap[to]

        if (fromLandmark != null && toLandmark != null) {
            val startX = fromLandmark.x * size.width
            val startY = fromLandmark.y * size.height
            val endX = toLandmark.x * size.width
            val endY = toLandmark.y * size.height

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4f
            )
        }
    }
}