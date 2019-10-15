package life.plank.visior.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleObserver
import life.plank.visior.R
import life.plank.visior.di.*
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import java.lang.reflect.Array.get

class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {


    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private val viewModel: ArViewViewModel by inject()

    fun onCreate(dependencyProvider: DependencyProvider){
        loadKoinModules(listOf(provideDependency(dependencyProvider), sensorModule, permissionModule, orientationModule, viewModelModule))
    }

    private fun provideDependency(dependencyProvider: DependencyProvider): Module {
        return module { single { dependencyProvider } }
    }

    fun OnDestroy(){
        unloadKoinModules(listOf( sensorModule, permissionModule, orientationModule, viewModelModule))
    }

}