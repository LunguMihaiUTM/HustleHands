package com.dog.hustlehands.feature.camera.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper

class CameraAnalyzer(
    private val handLandmarkerHelper: HandLandmarkerHelper
) : ImageAnalysis.Analyzer {

//    private var frameCount = 0
//    private var lastLogTime = System.currentTimeMillis()


    override fun analyze(imageProxy: ImageProxy) {
//        frameCount++
//        val now = System.currentTimeMillis()
//        if (now - lastLogTime >= 1000) {
//            Log.d("FPS", "Camera frames per second: $frameCount")
//            frameCount = 0
//            lastLogTime = now
//        }
        try {
            val bitmap = imageProxy.toBitmap() ?: return
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rotated = if (rotationDegrees != 0) {
                rotateBitmap(bitmap, rotationDegrees)
            } else {
                bitmap
            }

            handLandmarkerHelper.detectAsync(rotated, System.currentTimeMillis())
        } catch (_: Exception) {
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val m = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        bitmap.recycle()
        return rotated
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            // Create bitmap in ARGB_8888 format (same as the camera output)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            // Direct buffer copy - NO JPEG compression, NO YUV conversion
            bitmap.copyPixelsFromBuffer(planes[0].buffer)
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}