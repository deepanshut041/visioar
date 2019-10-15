package life.plank.visior.ui

import androidx.lifecycle.LiveData
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.util.PermissionManager
import life.plank.visior.util.PermissionResult
import life.plank.visior.util.SingleLiveEvent

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager){

    val permissionState: LiveData<PermissionResult>
        get() = _permissionState

    private val _permissionState = SingleLiveEvent<PermissionResult>()

    fun checkPermissions(){
        if (permissionManager.areAllPermissionsGranted())
            _permissionState.value = PermissionResult.GRANTED
        else permissionManager.requestAllPermissions()
    }

    fun getOrientationData(){
        rotationRepository.getOrientationData()
    }
}