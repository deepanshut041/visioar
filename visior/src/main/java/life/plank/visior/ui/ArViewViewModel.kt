package life.plank.visior.ui

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Observable
import io.reactivex.rxkotlin.combineLatest
import life.plank.visior.data.location.LocationData
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.data.view.ArPointData
import life.plank.visior.data.view.ArSelectedPoint
import life.plank.visior.data.view.ScreenData
import life.plank.visior.util.PermissionManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
    ){

    private var points: List<ArPointData> = emptyList()

    private var distance = 100

    @SuppressLint("CheckResult")
    fun getPermissions(): Observable<Permission>? {
        return permissionManager
            .permissionListener()
    }


    fun setPoints(points: List<ArPointData>){
        this.points = points
    }

    fun setDistance(distance:Int){
        this.distance = distance
    }

    fun getLivePoints(): LiveData<ScreenData> {
        return locationRepository
            .getLocationUpdates()
            .combineLatest(rotationRepository.getOrientationUpdate())
            .map { (lc, oc) ->
                val selectedPoints = ArrayList<ArSelectedPoint>()
                for (point in points){
                    handleDestination(lc, LocationData(point.lat, point.lon), oc.aizmuth, point.label)?.let {
                        selectedPoints.add(it)
                    }
                }
                ScreenData(oc.aizmuth.toInt(), oc.pitch.toInt(), oc.roll.toInt(), lc.lat, lc.lon, selectedPoints)

            }.toLiveData()
    }

    private fun handleDestination(
        currentLocation: LocationData,
        destinationLocation: LocationData,
        currentAzimuth: Float,
        label: String
    ): ArSelectedPoint? {
        val headingAngle = calculateHeadingAngle(currentLocation, destinationLocation)

        val currentDestinationAzimuth =
            (headingAngle - currentAzimuth + 360) % 360

        val distanceToDestination = locationRepository.getDistanceBetweenPoints(
            currentLocation,
            destinationLocation
        )

        if (distanceToDestination > 100 && (!isPointOnCamera(currentAzimuth.toDouble(), currentDestinationAzimuth.toDouble())))
            return null

        return ArSelectedPoint(
            currentDestinationAzimuth.toInt(),
            distanceToDestination,
            label
        )
    }

    private fun isPointOnCamera(currentAzimuth: Double, pointAzimuth: Double): Boolean{
        var minAngle = currentAzimuth - 40
        var maxAngle = currentAzimuth + 40

        if (minAngle < 0)
            minAngle += 360

        if (maxAngle >= 360)
            maxAngle -= 360

        return isBetween(minAngle, maxAngle, pointAzimuth)
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

    private fun calculateHeadingAngle(currentLocation: LocationData, destinationLocation: LocationData): Float {
        val currentLatitudeRadians = Math.toRadians(currentLocation.lat)
        val destinationLatitudeRadians = Math.toRadians(destinationLocation.lat)
        val deltaLongitude = Math.toRadians(destinationLocation.lon - currentLocation.lon)

        val y = cos(currentLatitudeRadians) * sin(destinationLatitudeRadians) -
                sin(currentLatitudeRadians) * cos(destinationLatitudeRadians) * cos(deltaLongitude)
        val x = sin(deltaLongitude) * cos(destinationLatitudeRadians)
        val headingAngle = Math.toDegrees(atan2(x, y)).toFloat()

        return (headingAngle + 360) % 360
    }

}