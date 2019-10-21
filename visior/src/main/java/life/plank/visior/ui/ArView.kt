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
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import life.plank.visior.data.view.ArPointData
import life.plank.visior.data.view.PointsInRadius
import timber.log.Timber
import android.graphics.Bitmap
import android.graphics.BitmapFactory


class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }


    private var gMap:GoogleMap? = null
    private var markerList = HashMap<String, PointsInRadius>()
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
            mapView.onCreate(null)
            mapView.getMapAsync(MapViewListener())
            mapView.onStart()
            viewModel.getLivePoints().observe(it.getLifecycleOwner(), Observer { points ->
                gMap?.clear()
                markerList.clear()
                points.forEach { pointInRadius ->
                    markerList[pointInRadius.label] = pointInRadius
                    gMap?.addMarker(getMarker(pointInRadius))
                }
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
//        mapView.onPause()
//        arCameraView.onPause()
    }

    fun onResume() {
//        mapView.onResume()
//        arCameraView.onStart()
    }

    // Map util
    private fun getMarker(pointInRadius:PointsInRadius): MarkerOptions? {
        val marker = MarkerOptions().position(pointInRadius.locationData).title(pointInRadius.label)
        val opt: BitmapFactory.Options = BitmapFactory.Options()
        opt.inMutable = true
        if (pointInRadius.distance > 10){
            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.marker, opt)
            val resized = Bitmap.createScaledBitmap(imageBitmap, 80, 80, true)
            marker.icon(BitmapDescriptorFactory.fromBitmap(resized))
        } else{
            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.near, opt)
            val resized = Bitmap.createScaledBitmap(imageBitmap, 150, 150, true)
            marker.icon(BitmapDescriptorFactory.fromBitmap(resized))
        }
        return marker
    }

    inner class MapViewListener: OnMapReadyCallback{
        override fun onMapReady(googleMap: GoogleMap?) {
            gMap = googleMap
            gMap?.let{
                it.isMyLocationEnabled = true
                it.uiSettings.isMyLocationButtonEnabled = true
//                it.setMapStyle(MapStyleOptions.loadRawResourceStyle(
//                    dependencyProvider!!.getPermissionActivity(), R.raw.map_json))
                it.setOnMarkerClickListener(MarkerClickListener())

                val cameraPosition = CameraPosition.builder()
                    .target(LatLng(15.5511178,73.7823974))
                    .zoom(20.0f)
                    .tilt(67.5f)
                    .bearing(314.0f)
                    .build()

                it.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            }

        }
    }

    inner class MarkerClickListener: GoogleMap.OnMarkerClickListener{
        override fun onMarkerClick(marker: Marker?): Boolean {
            marker?.let {
                val point = markerList[it.title]
                point?.let { pnt ->
                    if (pnt.distance > 10)
                        Toast.makeText(dependencyProvider!!.getContext(), "${pnt.label} is ${pnt.distance}m away", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(dependencyProvider!!.getContext(), "${pnt.label} collected", Toast.LENGTH_SHORT).show()
                }?: run{
                    Toast.makeText(dependencyProvider!!.getContext(), it.title, Toast.LENGTH_SHORT).show()
                }
            }
            return true
        }

    }

}