package life.plank.visior.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.camera2.*
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.ar_camera_layout.view.*
import life.plank.visior.R
import timber.log.Timber

class ArCameraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        View.inflate(context, R.layout.ar_camera_layout, this)
    }

    private lateinit var activity: Activity
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var cameraManager: CameraManager

    private var cameraTexture = ExternalTexture()
    private lateinit var scene: Scene
    private lateinit var modelRenderable: ModelRenderable

    private var pikachuNode: Node? = null

    var xCoordinate: Float = 1.5f
    var yCoordinate: Float = 1.5f
    var zCoordinate: Float = 1.5f
    var value2: Float = 180.0f

    fun setCameraManager(cameraManager: CameraManager){
        this.cameraManager = cameraManager
    }

    fun setSceneForm(activity: Activity){
        this.activity = activity
        scene = sceneView.scene // get current scene
        renderObject(Uri.parse("quad.sfb")) // Render the object
    }

    private fun renderObject(parse: Uri) {
        ModelRenderable.builder().setSource(activity, parse).build().thenAccept {
                it.material.setExternalTexture("cameraTexture", cameraTexture)
                addNodeToScene(it)
                startBackgroundThread()
                connectCamera()
            setUpButtonListener()
            }
            .exceptionally {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }

    }

    private fun addNodeToScene(model: ModelRenderable?) {

        model?.let {
            pikachuNode = Node().apply {
                setParent(scene)
                localPosition = Vector3(0f, -1f, -2f)
                localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
                localScale = Vector3(1.5f, 1.5f, 1.5f)
                name = "Pikachu"
                renderable = it
            }

            scene.addChild(pikachuNode)
        }
    }

    private val deviceStateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Timber.d("camera device opened")
            cameraDevice = camera
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Timber.d( "camera device disconnected")
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Timber.d( "camera device error")
        }
    }

    private fun previewSession() {
        cameraTexture.surfaceTexture.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = cameraTexture.surface
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            listOf(surface),
            object: CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Timber.e("creating capture session failed!")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

            }, null)
    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2 Kotlin").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Timber.e( e.toString())
        }
    }

    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>) :T {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)!!
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)!!
            else -> throw  IllegalArgumentException("Key not recognized")
        }
    }

    private fun cameraId(lens: Int) : String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException) {
            Timber.e( e.toString())
        }
        return deviceId[0]
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        Timber.d( "deviceId: $deviceId")
        try {
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Timber.e( e.toString())
        } catch (e: InterruptedException) {
            Timber.e( "Open camera device interrupted while opened")
        }
    }

    companion object {
        private const val MAX_PREVIEW_WIDTH = 1280
        private const val MAX_PREVIEW_HEIGHT = 720
    }

    fun onStart() {
        sceneView.resume()
    }

    fun onPause() {
        sceneView.pause()
        closeCamera()
        stopBackgroundThread()
    }

    private fun setUpButtonListener() {
        seekX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pikachuNode?.let {
                    xCoordinate = getConvertedValue(progress)
                    it.localScale = Vector3(xCoordinate, yCoordinate, zCoordinate)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pikachuNode?.let {
                    yCoordinate = getConvertedValue(progress)
                    it.localScale = Vector3(xCoordinate, yCoordinate, zCoordinate)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekZ.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pikachuNode?.let {
                    zCoordinate = getConvertedValue(progress)
                    it.localScale = Vector3(xCoordinate, yCoordinate, zCoordinate)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnLeft.setOnClickListener {
            value2 = (value2 - 20) % 360

            pikachuNode?.let {
                it.localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), value2)
            }
        }

        btnRight.setOnClickListener {
            value2 = (value2 + 20) % 360

            pikachuNode?.let {
                it.localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), value2)
            }
        }
    }

    fun getConvertedValue(interval: Int): Float {
        return 0.3f * interval
    }

}