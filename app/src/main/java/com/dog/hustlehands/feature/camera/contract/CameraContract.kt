package com.dog.hustlehands.feature.camera.contract

import com.dog.hustlehands.domain.model.DomainHandLandmark

interface CameraContract {
    data class State(
        val isLoading: Boolean = false,
        val landmarks: List<DomainHandLandmark> = emptyList(),
        val error: String? = null
    )

    sealed interface Event {
        data object Initialize : Event
        data class LandmarksUpdated(val landmarks: List<DomainHandLandmark>) : Event
        data class DetectionError(val message: String) : Event
    }

    sealed interface Action {
        data class ShowToast(val message: String) : Action
    }
}
