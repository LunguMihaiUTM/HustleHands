package com.dog.hustlehands.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class HandLandmarkerHelper(
    context: Context,
    private val onResult: (HandLandmarkerResult) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {

    private val handLandmarker: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)
            .setResultListener { result, _ ->
                if (result != null) {
                    onResult(result)
                }
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        try {
            val mpImage: MPImage = BitmapImageBuilder(bitmap).build()
            handLandmarker.detectAsync(mpImage, timestampMs)
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun close() {
        handLandmarker.close()
    }
}
