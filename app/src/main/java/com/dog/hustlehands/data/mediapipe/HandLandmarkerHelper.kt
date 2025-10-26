package com.dog.hustlehands.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean

class HandLandmarkerHelper(
    context: Context,
    private val onResult: (HandLandmarkerResult) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {

    private val handLandmarker: HandLandmarker
    private val isProcessing = AtomicBoolean(false)

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)
            .setResultListener { result, _ ->
                isProcessing.set(false)
                if (result != null) onResult(result)
            }
            .setErrorListener { e ->
                isProcessing.set(false)
                onError(Exception(e))
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        if (!isProcessing.compareAndSet(false, true)) {
            // Don't recycle here; let caller manage it
            return
        }
        try {
            val mpImage: MPImage = BitmapImageBuilder(bitmap).build()
            handLandmarker.detectAsync(mpImage, timestampMs)
            // DON'T recycle bitmap here - MediaPipe is still processing it asynchronously
        } catch (e: Exception) {
            isProcessing.set(false)
            onError(e)
        }
    }

    fun close() {
        try {
            handLandmarker.close()
        } catch (_: Throwable) {
        }
    }
}