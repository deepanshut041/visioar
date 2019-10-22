package life.plank.visior.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.hardware.camera2.CameraManager
import android.net.Uri
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import life.plank.visior.data.view.ArPointData
import timber.log.Timber


class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private lateinit var arFragment: ArFragment
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
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startCamera() {
//        arCameraView.setCameraManager(cameraManager)
//        arCameraView.setSceneForm(dependencyProvider!!.getPermissionActivity())
//        arCameraView.onStart()
    }

    @SuppressLint("LogNotTimber")
    private fun setSensorDataListeners() {
        dependencyProvider?.let {
            arMapView.onCreate(it)
            arFragment =
                (it.getPermissionActivity() as AppCompatActivity).supportFragmentManager.findFragmentById(
                    R.id.sceneformFragment
                ) as ArFragment

            arLayout.setOnClickListener {
                addObject(Uri.parse("pikachu.sfb"))
            }
            viewModel.getLivePoints().observe(it.getLifecycleOwner(), Observer { points ->
                arMapView.setMarker(points)
            })
            isArViewStarted = true
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
//        arCameraView.onPause()
    }

    fun onResume() {
        arMapView.onResume()
//        mapView.onResume()
//        arCameraView.onStart()
    }

    // Arcore
    private fun addObject(parse: Uri) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), parse)
                    break
                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, createAnchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, createAnchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(dependencyProvider!!.getContext())
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, createAnchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(createAnchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(R.id.arLayout)
        return Point(vw.width / 2, vw.height / 2)
    }


}