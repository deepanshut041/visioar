package life.plank.visior.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable
import life.plank.visior.data.location.LocationData
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.orientation.OrientationData
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.data.view.ArPointData
import life.plank.visior.util.PermissionManager

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
    ){

    private var points: List<ArPointData> = emptyList()

    private var distance = 10

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

    fun setPoints(points: List<ArPointData>){
        this.points = points
    }

    fun setDistance(distance:Int){
        this.distance = distance
    }

    fun getLivePoints(): List<ArPointData>{
        val showPoints = ArrayList<ArPointData>()
        val currentLocation = getLocation().value
        val currentOrientation = getOrientationData().value
        for (point in points){
            val dis = locationRepository.getDistanceBetweenPoints(currentLocation, LocationData(point.lat, point.lon))
            Log.i("Distance", "Distance: ${dis}")
            if (dis > distance ){
                showPoints.add(point)
            }
        }

        return showPoints
    }

    private fun isItemOnCamera(oData: OrientationData, point: ArPointData): Boolean{
//        if ((oData.aizmuth - 45 < point.azimuth) && ( point.azimuth < oData.aizmuth + 45))
//            return true
        return true
    }

    private fun isBetween(minAngle: Double, maxAngle: Double, azimuth: Double): Boolean {
        if (minAngle > maxAngle) {
            if (isBetween(0.0, maxAngle, azimuth) && isBetween(minAngle, 360.0, azimuth))
                return true
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true
        }
        return false
    }

}