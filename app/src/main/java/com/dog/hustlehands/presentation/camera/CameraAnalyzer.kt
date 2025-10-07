package com.dog.hustlehands.presentation.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper

/**
 * Converts CameraX frames into Bitmaps and sends them to HandLandmarkerHelper.
 */
class CameraAnalyzer(
    private val handLandmarkerHelper: HandLandmarkerHelper
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap() ?: return

            // Rotate and mirror the bitmap to match preview orientation
            val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

            val timestamp = System.currentTimeMillis()
            handLandmarkerHelper.detectAsync(rotatedBitmap, timestamp)

            if (rotatedBitmap != bitmap) {
                rotatedBitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())

        // Mirror horizontally for front camera
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            width,
            height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, width, height),
            100,
            out
        )
        val jpegBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }
}