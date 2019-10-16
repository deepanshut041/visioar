package life.plank.visior.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.tbruyelle.rxpermissions2.Permission
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.combineLatest
import life.plank.visior.data.location.LocationData
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.orientation.OrientationData
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.data.view.ArPointData
import life.plank.visior.data.view.ScreenData
import life.plank.visior.util.PermissionManager

class ArViewViewModel(
    private val rotationRepository: RotationRepository,
    private val permissionManager: PermissionManager,
    private val locationRepository: LocationRepository
    ){

    private var points: List<ArPointData> = emptyList()

    private var distance = 10

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
            .map {
                (lc, oc) -> ScreenData(oc.aizmuth.toInt(), oc.pitch.toInt(), oc.roll.toInt(), lc.lat, lc.lon, emptyList())
            }.toLiveData()
    }

}