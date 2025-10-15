package com.dog.hustlehands.feature.camera

import com.dog.hustlehands.base.BaseViewModel
import com.dog.hustlehands.feature.camera.contract.CameraContract

class CameraViewModel :
    BaseViewModel<CameraContract.State, CameraContract.Event, CameraContract.Action>(
        CameraContract.State()
    ) {

    override suspend fun handleEvent(event: CameraContract.Event) {
        when (event) {
            is CameraContract.Event.Initialize -> {
                updateState { it.copy(isLoading = false, error = null) }
            }
            is CameraContract.Event.LandmarksUpdated -> {
                updateState { it.copy(landmarks = event.landmarks, error = null) }
            }
            is CameraContract.Event.DetectionError -> {
                updateState { it.copy(error = event.message) }
                sendAction(CameraContract.Action.ShowToast(event.message))
            }
        }
    }
}
