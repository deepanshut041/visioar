package life.plank.visior.ui

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.ar_layout.view.*
import life.plank.visior.di.*
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import life.plank.visior.data.view.ArPointData
import timber.log.Timber
import android.os.Handler
import life.plank.visior.R
import life.plank.visior.data.view.PointsInRadius


class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private var dependencyProvider: DependencyProvider? = null
    var isArViewStarted: Boolean = false
    var isArCoreSupported:Boolean = false

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
        checkPermission()

        backButton.setOnClickListener {
            // arCoreView.onDestroy()
            arCameraView.onPause()
            arCameraView.onDestroy()
            arCameraView.visibility = View.GONE
            it.visibility = View.GONE
        }
    }

    private fun checkPermission() {
        with(viewModel) {
            getPermissions()?.subscribe {
                when {
                    it.granted -> onPermissionGranted()
                    else -> Toast.makeText(dependencyProvider!!.getContext(), "Please Allow Permissions", Toast.LENGTH_SHORT).show()
                }
            }
        }

        isArCoreAvaliable()
    }

    private fun isArCoreAvaliable(){
        val availability = ArCoreApk.getInstance().checkAvailability(dependencyProvider!!.getContext())

        if (availability.isTransient){
            Handler().postDelayed(Runnable { isArCoreAvaliable() }, 200)
        }
        isArCoreSupported = availability.isSupported
    }

    private fun onPermissionGranted() {
        dependencyProvider?.let {
            arMapView.onCreate(it)
            viewModel.getLivePoints().observe(it.getLifecycleOwner(), Observer { points ->
                arMapView.setMarker(points)
            })
            isArViewStarted = true

            arMapView.pointSelected.observe(it.getLifecycleOwner(), Observer { pointsInRadius ->
                renderArView(pointsInRadius)
            })
        }
    }

    private fun renderArView(pointsInRadius: PointsInRadius) {
        backButton.visibility = View.VISIBLE
        if (isArCoreSupported){
            arCoreView.visibility = View.VISIBLE
            arCoreView.onCreate(dependencyProvider!!)
            arCoreView.onStart()
        } else{
            arCameraView.visibility = View.VISIBLE
            arCameraView.setCameraManager(cameraManager)
            arCameraView.setSceneForm(dependencyProvider!!.getPermissionActivity())
            arCameraView.onStart()
            arCameraView.setPokemon(pointsInRadius)
        }
    }

    fun setArPoints(points: List<ArPointData>) {
        viewModel.setPoints(points)
    }


    fun setNearestDistance(distance: Int) {
        viewModel.setDistance(distance)
    }

    fun onPause() {
        arMapView.onPause()
        arCameraView.onPause()
    }

    fun onResume() {
        arMapView.onResume()
        arCameraView.onResume()
    }

    fun onDestroy(){
        arCameraView.onDestroy()
        arMapView.onDestroy()
    }


}