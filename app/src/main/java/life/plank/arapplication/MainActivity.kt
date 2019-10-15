package life.plank.arapplication

import android.content.Context
import android.hardware.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.hardware.SensorManager
import android.widget.Toast

class MainActivity : AppCompatActivity(), SensorEventListener2 {

    private var sensorManager: SensorManager? = null
    private var rotationalSensor: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rotation()
    }

    private fun rotation() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationalSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorList()
        checkSensorAvailability(Sensor.TYPE_ROTATION_VECTOR)

    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this, rotationalSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event?.values)

        val adjustedRotationMatrix: FloatArray = getAdjustedRotationMatrix(rotationMatrix)

        SensorManager.getOrientation(adjustedRotationMatrix, orientation)
        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        txtAzimuthText.text = azimuth.toString()
        txtPitchText.text = pitch.toString()
        txtRollText.text = roll.toString()
    }

    private fun getAdjustedRotationMatrix(rotationMatrix: FloatArray): FloatArray {
        val adjustedRotationMatrix = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, adjustedRotationMatrix
        )
        return adjustedRotationMatrix
    }

    override fun onFlushCompleted(sensor: Sensor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    private fun checkSensorAvailability(sensorType: Int) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
         if (sensorManager.getDefaultSensor(sensorType) != null) {
            Toast.makeText(this, "Available", Toast.LENGTH_SHORT).show()
        } else {
             Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sensorList(){
        val sensorList = sensorManager!!.getSensorList(Sensor.TYPE_ALL)

        var sensorInfo = ""
        for (s in sensorList) {
            sensorInfo = sensorInfo + s.name + "\n"
        }
        txtSensorsText.text = sensorInfo
    }

}
