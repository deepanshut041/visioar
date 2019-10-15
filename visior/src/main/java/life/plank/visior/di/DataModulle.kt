package life.plank.visior.di

import android.hardware.SensorManager
import androidx.core.content.getSystemService
import life.plank.visior.data.orientation.RotationRepository
import life.plank.visior.data.orientation.RotationRepositoryImpl
import life.plank.visior.util.PermissionManager
import org.koin.dsl.module

val sensorModule = module {

    fun provideSensorManager(dependencyProvider: DependencyProvider): SensorManager {
        return requireNotNull(dependencyProvider.getContext().getSystemService())
    }

    single {
        provideSensorManager(get())
    }
}

val permissionModule = module {
    fun providePermissionManager(dependencyProvider: DependencyProvider): PermissionManager {
        return PermissionManager(dependencyProvider.getPermissionActivity())
    }
}

val orientationModule = module {
    factory<RotationRepository>{
        RotationRepositoryImpl(get())
    }
}