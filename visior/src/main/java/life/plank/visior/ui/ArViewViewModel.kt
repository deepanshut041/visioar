package life.plank.visior.ui

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import life.plank.visior.data.location.LocationData
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.orientation.OrientationData
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.util.PermissionManager

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
    ){

    @SuppressLint("CheckResult")
    fun getOrientationData(): LiveData<OrientationData> {
        return rotationRepository.getOrientationUpdate().toLiveData()
    }

    @SuppressLint("CheckResult")
    fun getPermissions(): Observable<Permission>? {
        return permissionManager
            .permissionListener()
    }

    fun getLocation(): LiveData<LocationData> {
        return locationRepository.getLocationUpdates().toLiveData()
    }
}