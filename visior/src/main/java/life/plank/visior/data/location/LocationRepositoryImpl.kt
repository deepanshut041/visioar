package life.plank.visior.data.location

import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class LocationRepositoryImpl(private val rxLocation: RxLocation) : LocationRepository {
    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 5000L
        private const val FASTEST_REQUEST_INTERVAL = 20L
        private const val SMALLEST_DISPLACEMENT_NOTICED = 1f
    }

    private val locationRequest = LocationRequest().apply {
        interval = LOCATION_REQUEST_INTERVAL
        fastestInterval = FASTEST_REQUEST_INTERVAL
        smallestDisplacement = SMALLEST_DISPLACEMENT_NOTICED
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun getLocationUpdates(): Flowable<LocationData> {
        return rxLocation.settings().checkAndHandleResolution(locationRequest)
            .toObservable()
            .flatMap { rxLocation.location().updates(locationRequest) }
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .toFlowable(BackpressureStrategy.LATEST)
            .flatMap { location ->
                Flowable.just(
                    LocationData(
                        location.latitude,
                        location.longitude
                    )
                )
            }
    }

    override fun getDistanceBetweenPoints(
        cL: LocationData?,
        dL: LocationData?
    ): Int {
        val lA = Location("A")
        val lB = Location("B")

        lA.latitude = cL?.lat ?: 0.0
        lA.longitude = cL?.lon ?: 0.0

        lB.latitude = dL?.lat ?: 0.0
        lB.longitude = dL?.lon ?: 0.0

        return lA.distanceTo(lB).roundToInt()
    }

}