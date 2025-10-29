package com.dog.hustlehands.data.mediapipe

import com.dog.hustlehands.domain.model.DomainHandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

// ✅ MAPPER OPTIMIZATION: Pre-allocate reusable list to avoid repeated allocations
private val landmarkPool = ArrayList<DomainHandLandmark>()

fun HandLandmarkerResult.toDomain(): List<DomainHandLandmark> {
    // ✅ Reuse existing list instead of creating new one each time
    landmarkPool.clear()

    val hands = landmarks()
    // ✅ Pre-calculate total capacity to avoid list resizing during additions
    val totalLandmarks = hands.sumOf { it.size }
    landmarkPool.ensureCapacity(totalLandmarks)

    // ✅ OPTIMIZATION: Use direct indexing instead of withIndex() to avoid boxing overhead
    for (handIndex in hands.indices) {
        val hand = hands[handIndex]
        for (landmarkIndex in hand.indices) {
            val landmark = hand[landmarkIndex]
            landmarkPool.add(
                DomainHandLandmark(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    handIndex = handIndex,
                    landmarkIndex = landmarkIndex
                )
            )
        }
    }

    // ✅ Return a copy to avoid reference sharing issues
    return landmarkPool.toList()
}