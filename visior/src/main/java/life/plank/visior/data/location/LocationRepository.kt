package life.plank.visior.data.location

import io.reactivex.Flowable

interface LocationRepository {

    fun getLocationUpdates(): Flowable<LocationData>
    fun getDistanceBetweenPoints(cL: LocationData?, dL: LocationData?): Int
}