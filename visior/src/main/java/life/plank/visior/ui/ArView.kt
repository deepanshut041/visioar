package life.plank.visior.ui

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.ar_layout.view.*
import life.plank.visior.R
import life.plank.visior.di.*
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import android.hardware.camera2.CameraDevice
import android.util.Log
import android.widget.Toast
import life.plank.visior.data.view.ArPointData
import timber.log.Timber

class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private var dependencyProvider: DependencyProvider? = null
    var isArViewStarted: Boolean = false


    // Setting up Koin
    private var myLocalKoinInstance = koinApplication {}

    override fun getKoin(): Koin = myLocalKoinInstance.koin

    private val viewModel: ArViewViewModel by inject()

    private val cameraManager: CameraManager by inject()

    fun onCreate(dependencyProvider: DependencyProvider) {
        Timber.plant(Timber.DebugTree())
        this.dependencyProvider = dependencyProvider
        myLocalKoinInstance.modules(
            listOf(
                module { single { dependencyProvider } },
                sensorModule,
                permissionModule,
                rxLocationModule,
                orientationModule,
                viewModelModule,
                locationModule,
                cameraModule
            )
        )
        bindToViewModel()
    }

    private fun bindToViewModel() {
        with(viewModel) {
            getPermissions()?.subscribe {
                when {
                    it.granted -> setSensorDataListeners()
                    else -> Toast.makeText(
                        dependencyProvider!!.getContext(),
                        "Please Allow Permissions",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startCamera() {
        arCameraView.setCameraManager(cameraManager)
        arCameraView.setSceneForm(dependencyProvider!!.getPermissionActivity())
        arCameraView.onStart()
    }

    @SuppressLint("LogNotTimber")
    private fun setSensorDataListeners() {
        dependencyProvider?.let {
            startCamera()
            viewModel.getLivePoints().observe(it.getLifecycleOwner(), Observer { screenData ->
                txtAzimuthText.text = screenData.azimuth.toString()
                txtRollText.text = screenData.roll.toString()
                txtPitchText.text = screenData.pitch.toString()
                txtLongitudeText.text = screenData.lon.toString()
                txtLatitudeText.text = screenData.lat.toString()
                var s = ""
                for (point in screenData.points)
                    s =  point.label + ", "

                txtPointsText.text = s
            })
            isArViewStarted = true
        }
    }

    fun setArPoints(points: List<ArPointData>){
        viewModel.setPoints(points)
    }


    fun setNearestDistance(distance: Int){
        viewModel.setDistance(distance)
    }

    fun onPause() {
        arCameraView.onPause()
    }

    fun onResume() {
        arCameraView.onStart()
    }

    companion object {
        private const val MAXIMUM_ANGLE = 360
    }


}