package life.plank.visior.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.ar_map_layout.view.*
import life.plank.visior.R
import life.plank.visior.data.view.PointsInRadius
import life.plank.visior.di.DependencyProvider
import life.plank.visior.util.SingleLiveEvent

class ArMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    init {
        View.inflate(context, R.layout.ar_map_layout, this)
    }

    private var gMap:GoogleMap? = null
    private var markerList = HashMap<String, PointsInRadius>()
    private var dependencyProvider: DependencyProvider? = null
    val pointSelected: LiveData<PointsInRadius>
        get() = _pointSelected

    private val _pointSelected = SingleLiveEvent<PointsInRadius>()

    fun onCreate(dependencyProvider: DependencyProvider){
        this.dependencyProvider = dependencyProvider
        mapView.onCreate(null)
        mapView.getMapAsync(MapViewListener())
        mapView.onStart()
    }

    fun setMarker(points: ArrayList<PointsInRadius>){
        gMap?.clear()
        markerList.clear()
        points.forEach { pointInRadius ->
            markerList[pointInRadius.label] = pointInRadius
            gMap?.addMarker(getMarker(pointInRadius))
        }
    }

    // Map util
    private fun getMarker(pointInRadius: PointsInRadius): MarkerOptions? {
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

    inner class MapViewListener: OnMapReadyCallback {
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
                    {
                        _pointSelected.value = pnt
                    }
                }?: run{
                    Toast.makeText(dependencyProvider!!.getContext(), it.title, Toast.LENGTH_SHORT).show()
                }
            }
            return true
        }

    }

    fun onPause(){
        mapView.onPause()
    }

    fun onResume(){
        mapView.onResume()
    }

    fun onDestroy(){
        mapView.onDestroy()
    }
}