package com.dog.hustlehands.feature.camera.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val memoizedLandmarks = remember(landmarks) { landmarks }
    val memoizedHandGroups = remember(memoizedLandmarks) {
        memoizedLandmarks.groupBy { it.handIndex }
    }

    Canvas(modifier = modifier) {
        if (memoizedLandmarks.isEmpty()) return@Canvas

        memoizedHandGroups.forEach { (idx, list) ->
            val color = if (idx == 0) Color.Green else Color.Blue

            // Draw circles for each landmark
            list.forEach { l ->
                drawCircle(
                    color = color,
                    radius = 8f,
                    center = Offset(l.x * size.width, l.y * size.height)
                )
            }

            // Draw connections between landmarks
            drawHandConnections(list, color)
        }
    }
}

private fun DrawScope.drawHandConnections(
    landmarks: List<DomainHandLandmark>,
    color: Color
) {
    val map = landmarks.associateBy { it.landmarkIndex }

    val connections = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        0 to 9, 9 to 10, 10 to 11, 11 to 12,
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        5 to 9, 9 to 13, 13 to 17
    )

    connections.forEach { (from, to) ->
        val a = map[from]
        val b = map[to]

        if (a != null && b != null) {
            drawLine(
                color = color,
                start = Offset(a.x * size.width, a.y * size.height),
                end = Offset(b.x * size.width, b.y * size.height),
                strokeWidth = 4f
            )
        }
    }
}