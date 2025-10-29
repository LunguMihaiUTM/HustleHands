package com.dog.hustlehands.feature.camera.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.dog.hustlehands.data.mediapipe.HandLandmarkerHelper
import kotlin.math.min

class CameraAnalyzer(
    private val handLandmarkerHelper: HandLandmarkerHelper
) : ImageAnalysis.Analyzer {

    @Volatile
    var shouldSaveFrame = false //

    override fun analyze(imageProxy: ImageProxy) {
        try {
            // ✅ START: End-to-end timing measurement
            val endToEndStartTime = System.currentTimeMillis()

            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rotated = if (rotationDegrees != 0) {
                rotateBitmap(bitmap, rotationDegrees)
            } else {
                bitmap
            }
            val square = cropToSquare(rotated)

            if (shouldSaveFrame) {
                shouldSaveFrame = false
                saveBitmapToStorage(square)
            }

            // ✅ Pass the start timestamp to track complete pipeline
            handLandmarkerHelper.detectAsync(square, endToEndStartTime)

            val preprocessingTime = System.currentTimeMillis() - endToEndStartTime
            Log.d("PIPELINE_TIMING", "Image preprocessing took: ${preprocessingTime}ms")

        } catch (_: Exception) {
            Log.e("CameraAnalyzer", "Analysis failed")
        } finally {
            imageProxy.close()
        }
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        try {
            val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_PICTURES
            )
            if (!picturesDir.exists()) picturesDir.mkdirs()

            val filename = "frame_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(picturesDir, filename)
            val fos = java.io.FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()

            android.util.Log.d("CameraAnalyzer", "Frame saved at: ${file.absolutePath}")
        } catch (e: Exception) {
            android.util.Log.e("CameraAnalyzer", "Failed to save frame: ${e.message}", e)
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

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val offsetX = (bitmap.width - size) / 2
        val offsetY = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, offsetX, offsetY, size, size)
    }

}