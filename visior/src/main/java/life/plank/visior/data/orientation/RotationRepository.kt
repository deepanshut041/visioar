package life.plank.visior.data.orientation

import io.reactivex.Flowable

interface RotationRepository {

    fun getOrientationUpdate(): Flowable<OrientationData>
}