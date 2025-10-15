package com.dog.hustlehands.feature.camera.screen.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.dog.hustlehands.domain.model.DomainHandLandmark

class OverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var landmarks: List<DomainHandLandmark> = emptyList()
    private var canvasWidth: Float = 1f
    private var canvasHeight: Float = 1f

    private val linePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 8f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setLandmarks(newLandmarks: List<DomainHandLandmark>) {
        landmarks = newLandmarks
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvasWidth = width.toFloat()
        canvasHeight = height.toFloat()

        if (landmarks.isEmpty()) return

        val handGroups = landmarks.groupBy { it.handIndex }

        handGroups.forEach { (handIndex, handLandmarks) ->
            val color = if (handIndex == 0) Color.GREEN else Color.BLUE
            linePaint.color = color

            handLandmarks.forEach { landmark ->
                // Scale from normalized (0-1) to canvas, accounting for aspect ratio
                val scaledX = landmark.x * canvasWidth
                val scaledY = landmark.y * canvasHeight

                canvas.drawPoint(scaledX, scaledY, pointPaint)
            }

            drawHandConnections(canvas, handLandmarks, linePaint)
        }
    }

    private fun drawHandConnections(
        canvas: Canvas,
        landmarks: List<DomainHandLandmark>,
        paint: Paint
    ) {
        val landmarkMap = landmarks.associateBy { it.landmarkIndex }

        val connections = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 4,
            0 to 5, 5 to 6, 6 to 7, 7 to 8,
            0 to 9, 9 to 10, 10 to 11, 11 to 12,
            0 to 13, 13 to 14, 14 to 15, 15 to 16,
            0 to 17, 17 to 18, 18 to 19, 19 to 20,
            5 to 9, 9 to 13, 13 to 17
        )

        connections.forEach { (start, end) ->
            val startLandmark = landmarkMap[start]
            val endLandmark = landmarkMap[end]

            if (startLandmark != null && endLandmark != null) {
                canvas.drawLine(
                    startLandmark.x * canvasWidth,
                    startLandmark.y * canvasHeight,
                    endLandmark.x * canvasWidth,
                    endLandmark.y * canvasHeight,
                    paint
                )
            }
        }
    }
}