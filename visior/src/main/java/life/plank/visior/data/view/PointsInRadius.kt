package life.plank.visior.data.view

import com.google.android.gms.maps.model.LatLng

data class PointsInRadius (val locationData: LatLng, val distance: Int, val label: String)