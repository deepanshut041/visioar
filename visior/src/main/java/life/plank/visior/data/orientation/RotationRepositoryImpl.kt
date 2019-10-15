package life.plank.visior.data.orientation

import android.hardware.SensorManager
import android.util.Log

class RotationRepositoryImpl(private val sensorManager: SensorManager): RotationRepository {
    override fun getOrientationData() {
        Log.i("Visior", "Using Library")
    }


}