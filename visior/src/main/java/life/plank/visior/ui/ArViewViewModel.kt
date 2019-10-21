package life.plank.visior.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.google.android.gms.maps.model.LatLng
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable
import life.plank.visior.data.location.LocationData
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.view.ArPointData
import life.plank.visior.data.view.PointsInRadius
import life.plank.visior.util.PermissionManager

class ArViewViewModel(
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
) {

    private var points: List<ArPointData> = emptyList()

    private var distance = 10

    @SuppressLint("CheckResult")
    fun getPermissions(): Observable<Permission>? {
        return permissionManager
            .permissionListener()
    }


    fun setPoints(points: List<ArPointData>) {
        this.points = points
    }

    fun setDistance(distance: Int) {
        this.distance = distance
    }

    fun getLivePoints(): LiveData<ArrayList<PointsInRadius>> {
        return locationRepository
            .getLocationUpdates()
            .map {lc ->
                val selectedPoints = ArrayList<PointsInRadius>()
                for (point in points) {
                    handleDestination(lc, LocationData(point.lat, point.lon), point.label)?.let {
                        selectedPoints.add(it)
                    }
                }
                Log.i("points", selectedPoints.toString())
                selectedPoints
            }.toLiveData()
    }

    private fun handleDestination(
        currentLocation: LocationData,
        destinationLocation: LocationData,
        label: String
    ): PointsInRadius? {

        val distanceToDestination = locationRepository.getDistanceBetweenPoints(
            currentLocation,
            destinationLocation
        )

        if (distanceToDestination > 100)
            return null

        return PointsInRadius(
            LatLng(destinationLocation.lat, destinationLocation.lon),
            distanceToDestination,
            label
        )
    }

}