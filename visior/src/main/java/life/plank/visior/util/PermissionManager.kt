package life.plank.visior.util

import android.Manifest
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import life.plank.visior.di.DependencyProvider

class PermissionManager(private val dependencyProvider: DependencyProvider) {

    private val rxPermissions = RxPermissions(dependencyProvider.getPermissionActivity() as FragmentActivity )


    fun permissionListener(): Observable<Permission>? {
        return rxPermissions.requestEachCombined( *PERMISSIONS)
    }

    companion object {
        private const val ESSENTIAL_PERMISSIONS_REQUEST_CODE = 123
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
    }



}