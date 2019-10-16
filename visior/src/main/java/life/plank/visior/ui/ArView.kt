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
        arCameraView.onStart()
    }

    @SuppressLint("LogNotTimber")
    private fun setSensorDataListeners() {
        viewModel.getLocation().observe(dependencyProvider!!.getLifecycleOwner(), Observer {
            txtLatitudeText.text = it.lat.toString()
            txtLongitudeText.text = it.lon.toString()
        })

        viewModel.getOrientationData().observe(dependencyProvider!!.getLifecycleOwner(), Observer {
            txtAzimuthText.text = it.aizmuth.toString()
            txtPitchText.text = it.pitch.toString()
            txtRollText.text = it.roll.toString()
        })

        startCamera()
        isArViewStarted = true
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

}