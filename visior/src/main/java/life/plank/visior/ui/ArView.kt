package life.plank.visior.ui

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
class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private var dependencyProvider: DependencyProvider? = null

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
            getOrientationData().observe(dependencyProvider!!.getLifecycleOwner(), Observer {
                txtAzimuthText.text = it.aizmuth.toString()
                txtPitchText.text = it.pitch.toString()
                txtRollText.text = it.roll.toString()
            })

            getPermissions()?.subscribe {
                when {
                    it.granted -> {
                        getCurrentLocation()
                        startCamera()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun startCamera() {
        arCameraView.setCameraManager(cameraManager)
        arCameraView.onStart()
    }

    private fun getCurrentLocation() {
        viewModel.getLocation().observe(dependencyProvider!!.getLifecycleOwner(), Observer {
            txtLatitudeText.text = it.lat.toString()
            txtLongitudeText.text = it.lon.toString()
        })
    }

}