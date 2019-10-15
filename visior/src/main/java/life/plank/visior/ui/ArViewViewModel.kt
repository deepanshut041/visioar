package life.plank.visior.ui

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import life.plank.visior.data.orientation.OrientationData
import life.plank.visior.data.orientation.RotationRepository

class ArViewViewModel(
    private val rotationRepository: RotationRepository){

    @SuppressLint("CheckResult")
    fun getOrientationData(): LiveData<OrientationData> {
        return rotationRepository.getOrientationUpdate().toLiveData()
    }
}