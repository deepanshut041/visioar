package life.plank.visior.di

import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.patloew.rxlocation.RxLocation
import life.plank.visior.data.location.LocationRepository
import life.plank.visior.data.location.LocationRepositoryImpl
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

    single{
        PermissionManager(get())
    }
}

val rxLocationModule = module {
    fun provideRxLocationProvider(dependencyProvider: DependencyProvider): RxLocation {
        return RxLocation(dependencyProvider.getContext())
    }

    single {
        provideRxLocationProvider(get())
    }
}


val orientationModule = module {
    factory<RotationRepository>{
        RotationRepositoryImpl(get())
    }
}

val locationModule = module {
    factory<LocationRepository>{
        LocationRepositoryImpl(get())
    }
}