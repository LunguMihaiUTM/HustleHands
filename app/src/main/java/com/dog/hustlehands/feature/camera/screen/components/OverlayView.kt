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
    private var endToEndStartTime = 0L


    private var imageWidth = 1f
    private var imageHeight = 1f
    private var verticalOffset = 0f

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

    fun setTransform(viewWidth: Float, imageHeight: Float, offsetY: Float) {
        this.imageWidth = viewWidth
        this.imageHeight = imageHeight
        this.verticalOffset = offsetY
        invalidate()
    }

    fun setLandmarks(newLandmarks: List<DomainHandLandmark>, startTime: Long = 0L) {
        landmarks = newLandmarks
        endToEndStartTime = startTime

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val drawStartTime = System.currentTimeMillis()


        if (landmarks.isEmpty()) return

        val handGroups = landmarks.groupBy { it.handIndex }

        handGroups.forEach { (handIndex, handLandmarks) ->
            val color = if (handIndex == 0) Color.GREEN else Color.BLUE
            linePaint.color = color

            // raw points with corrected scale and offset
            handLandmarks.forEach { landmark ->
                val scaledX = landmark.x * imageWidth
                val scaledY = landmark.y * imageHeight + verticalOffset
                canvas.drawPoint(scaledX, scaledY, pointPaint)
            }

            //draw lines with the same transform
            drawHandConnections(canvas, handLandmarks, linePaint)
        }

        val drawTime = System.currentTimeMillis() - drawStartTime
        Log.d("PIPELINE_TIMING", "Drawing took: ${drawTime}ms")

        if (endToEndStartTime > 0) {
            val totalEndToEndTime = System.currentTimeMillis() - endToEndStartTime
            Log.w(
                "END_TO_END_TIMING",
                "🔥 COMPLETE PIPELINE: Image→Model→Screen took: ${totalEndToEndTime}ms"
            )
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
            val a = landmarkMap[start]
            val b = landmarkMap[end]
            if (a != null && b != null) {
                val startX = a.x * imageWidth
                val startY = a.y * imageHeight + verticalOffset
                val endX = b.x * imageWidth
                val endY = b.y * imageHeight + verticalOffset

                canvas.drawLine(startX, startY, endX, endY, paint)
            }
        }
    }

}