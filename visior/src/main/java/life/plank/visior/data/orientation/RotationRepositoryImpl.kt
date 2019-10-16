package life.plank.visior.data.orientation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface.*
import android.view.WindowManager
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class RotationRepositoryImpl(private val sensorManager: SensorManager, private val windowManager: WindowManager) : RotationRepository {

    private var alpha = 0f
    private var lastCos = 0f
    private var lastSin = 0f

    private val orientationPublisher: Observable<SensorEvent> = Observable.create {
        val sensorEventListener = SensorListener(it)
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
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
        val newRotMatrix = getAdjustedRotationMatrix(rotMatrix)

        SensorManager.getOrientation(newRotMatrix, orientation)
        val azimuth = lowPassDegreesFilter(orientation[0])
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        return OrientationData(azimuth, pitch, roll)
    }

    private fun lowPassDegreesFilter(azimuthRadians: Float): Float {
        lastSin = alpha * lastSin + (1 - alpha) * sin(azimuthRadians)
        lastCos = alpha * lastCos + (1 - alpha) * cos(azimuthRadians)

        return ((Math.toDegrees(atan2(lastSin, lastCos).toDouble()) + 360) % 360).toFloat()
    }

    private fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val axisXY = getProperAxis()

        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, axisXY.first,
            axisXY.second, adjustedRotationMatrix
        )
        return adjustedRotationMatrix
    }

    private fun getProperAxis(): Pair<Int, Int> {
        val worldAxisX: Int
        val worldAxisY: Int
        when (windowManager.defaultDisplay?.rotation) {
            ROTATION_90 -> {
                worldAxisX = SensorManager.AXIS_Z
                worldAxisY = SensorManager.AXIS_MINUS_X
            }
            ROTATION_180 -> {
                worldAxisX = SensorManager.AXIS_MINUS_X
                worldAxisY = SensorManager.AXIS_MINUS_Z
            }
            ROTATION_270 -> {
                worldAxisX = SensorManager.AXIS_MINUS_Z
                worldAxisY = SensorManager.AXIS_X
            }
            ROTATION_0 -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
            else -> {
                worldAxisX = SensorManager.AXIS_X
                worldAxisY = SensorManager.AXIS_Z
            }
        }
        return Pair(worldAxisX, worldAxisY)
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
                if (it.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR)
                    emitter.onNext(it)
            }
        }
    }

}