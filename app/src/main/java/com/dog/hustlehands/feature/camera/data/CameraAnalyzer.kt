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
    var shouldSaveFrame = false

    //Pre-allocate Matrix
    private val transformMatrix = Matrix()

    override fun analyze(imageProxy: ImageProxy) {

        Log.d("ImageProxy", "Size of input image: ${imageProxy.height}x${imageProxy.width}")
        try {
            val endToEndStartTime = System.currentTimeMillis()

            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            //Combined rotation + cropping + resizing in ONE operation
            val optimized = transformToOptimalSize(bitmap, rotationDegrees, 224)

            if (shouldSaveFrame) {
                shouldSaveFrame = false
                saveBitmapToStorage(optimized)
            }

            // Pass optimal 224x224 bitmap to MediaPipe
            handLandmarkerHelper.detectAsync(optimized, endToEndStartTime)

            val preprocessingTime = System.currentTimeMillis() - endToEndStartTime
            Log.d(
                "PIPELINE_TIMING",
                "Image preprocessing took: ${preprocessingTime}ms (COMBINED + 224x224)"
            )

        } catch (_: Exception) {
            Log.e("CameraAnalyzer", "Analysis failed")
        } finally {
            imageProxy.close()
        }
    }

    private fun transformToOptimalSize(
        bitmap: Bitmap,
        rotationDegrees: Int,
        targetSize: Int
    ): Bitmap {
        //Reset matrix for reuse
        transformMatrix.reset()

        //Add rotation if needed
        if (rotationDegrees != 0) {
            transformMatrix.postRotate(rotationDegrees.toFloat())
            transformMatrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }

        //calculate crop to square (center crop)
        val size = min(bitmap.width, bitmap.height)
        val offsetX = (bitmap.width - size) / 2
        val offsetY = (bitmap.height - size) / 2

        val scale = targetSize.toFloat() / size.toFloat()
        transformMatrix.postScale(scale, scale)

        val result = Bitmap.createBitmap(
            bitmap,
            offsetX, offsetY, size, size,  // Crop to square
            transformMatrix,               // Apply rotation + scaling
            true
        )

        // Clean up original bitmap
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }

        Log.d(
            "BITMAP_SIZE",
            "The final size is: ${result.height}x${result.width}"
        )

        return result
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


    //Old
    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val m = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        bitmap.recycle()
        return rotated
    }

    //Old
    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val offsetX = (bitmap.width - size) / 2
        val offsetY = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, offsetX, offsetY, size, size)
    }

}