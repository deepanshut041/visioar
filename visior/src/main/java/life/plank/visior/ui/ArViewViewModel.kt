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
import kotlin.math.*

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
) {

    private var points: List<ArPointData> = emptyList()

    private var distance = 100

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

    fun getLivePoints(): LiveData<ScreenData> {
        return locationRepository
            .getLocationUpdates()
            .combineLatest(rotationRepository.getOrientationUpdate())
            .map { (lc, oc) ->
                val selectedPoints = ArrayList<ArSelectedPoint>()
                for (point in points) {
                    handleDestination(
                        lc,
                        LocationData(point.lat, point.lon),
                        oc.aizmuth,
                        point.label
                    )?.let {
                        selectedPoints.add(it)
                    }
                }
                ScreenData(
                    oc.aizmuth.toInt(),
                    oc.pitch.toInt(),
                    oc.roll.toInt(),
                    lc.lat,
                    lc.lon,
                    selectedPoints
                )

            }.toLiveData()
    }

    private fun handleDestination(
        currentLocation: LocationData,
        destinationLocation: LocationData,
        currentAzimuth: Float,
        label: String
    ): ArSelectedPoint? {
        val headingAngle = calculateTheoreticalAzimuth(currentLocation, destinationLocation)

        val distanceToDestination = locationRepository.getDistanceBetweenPoints(
            currentLocation,
            destinationLocation
        )

        if (distanceToDestination > 10)
            return null

        if  (!isPointOnCamera(currentAzimuth.toDouble(), headingAngle))
            return null

        return ArSelectedPoint(
            headingAngle.toInt(),
            distanceToDestination,
            label
        )
    }

    private fun isPointOnCamera(currentAzimuth: Double, pointAzimuth: Double): Boolean {
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

    private fun calculateTheoreticalAzimuth(cL: LocationData, dL: LocationData): Double {
        val dX = dL.lat - cL.lat
        val dY = dL.lon - cL.lon

        var phiAngle: Double
        val tanPhi: Double

        tanPhi = abs(dY / dX)
        phiAngle = atan(tanPhi)
        phiAngle = Math.toDegrees(phiAngle)

        if (dX > 0 && dY > 0) { // I quater
            return phiAngle
        } else if (dX < 0 && dY > 0) { // II
            return 180 - phiAngle
        } else if (dX < 0 && dY < 0) { // III
            return 180 + phiAngle
        } else if (dX > 0 && dY < 0) { // IV
            return 360 - phiAngle
        }
        return phiAngle
    }

}