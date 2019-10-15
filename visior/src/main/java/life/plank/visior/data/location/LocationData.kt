package life.plank.visior.data.location

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationData(val lat: Double, val lon: Double): Parcelable