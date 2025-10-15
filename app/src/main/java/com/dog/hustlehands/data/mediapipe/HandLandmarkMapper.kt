package com.dog.hustlehands.data.mediapipe

import com.dog.hustlehands.domain.model.DomainHandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

fun HandLandmarkerResult.toDomain(): List<DomainHandLandmark> {
    val landmarks = mutableListOf<DomainHandLandmark>()

    for ((handIndex, hand) in landmarks().withIndex()) {
        for ((landmarkIndex, landmark) in hand.withIndex()) {
            landmarks.add(
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
    return landmarks
}