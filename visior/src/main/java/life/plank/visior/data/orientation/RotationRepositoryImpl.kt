package life.plank.visior.data.orientation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class RotationRepositoryImpl(private val sensorManager: SensorManager) : RotationRepository {

    private val orientationPublisher: Observable<SensorEvent> = Observable.create {
        val sensorEventListener = SensorListener(it)
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )
        it.setCancellable {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    override fun getOrientationUpdate(): Flowable<OrientationData> {
        return orientationPublisher
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .toFlowable(BackpressureStrategy.LATEST)
            .map { getOrientationData(it) }
    }

    private fun getOrientationData(sensorEvent: SensorEvent): OrientationData {
        val rotMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        SensorManager.getRotationMatrixFromVector(rotMatrix, sensorEvent.values)
        val newRotMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Y,
            newRotMatrix
        )

        SensorManager.getOrientation(newRotMatrix, orientation)
        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        return OrientationData(azimuth, pitch, roll)
    }


    class SensorListener(private val emitter: ObservableEmitter<SensorEvent>) :
        SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            when (sensor?.type) {
                Sensor.TYPE_ROTATION_VECTOR -> when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Timber.tag("Rotation Sensor").d("ACCURACY low")
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Timber.tag("Rotation Sensor").d("ACCURACY medium")
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Timber.tag("Rotation Sensor").d("ACCURACY high")
                    else -> Unit
                }
                else -> Unit
            }
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
                    emitter.onNext(it)
            }
        }
    }


}