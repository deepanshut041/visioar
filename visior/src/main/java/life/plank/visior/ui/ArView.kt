package life.plank.visior.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
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

class ArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver, KoinComponent {

    init {
        View.inflate(context, R.layout.ar_layout, this)
    }

    private var dependencyProvider: DependencyProvider? = null

    private var myLocalKoinInstance = koinApplication {}

    override fun getKoin(): Koin = myLocalKoinInstance.koin

    private val viewModel: ArViewViewModel by inject()

    fun onCreate(dependencyProvider: DependencyProvider) {

        this.dependencyProvider = dependencyProvider
        myLocalKoinInstance.modules(
            listOf(
                module { single { dependencyProvider } },
                sensorModule,
                permissionModule,
                orientationModule,
                viewModelModule
            )
        )
        bindToViewModel()
    }

    private fun bindToViewModel() {

        with(viewModel){
            getOrientationData().observe(dependencyProvider!!.getLifecycleOwner(), Observer {
                txtAzimuthText.text = it.aizmuth.toString()
                txtPitchText.text = it.pitch.toString()
                txtRollText.text = it.roll.toString()
            })

            getPermissions()?.subscribe{
                when {
                    it.granted -> Toast.makeText(dependencyProvider!!.getContext(), "All Permissions are granted", Toast.LENGTH_SHORT).show()
                    it.shouldShowRequestPermissionRationale -> Toast.makeText(dependencyProvider!!.getContext(), "One permission is not allowed please allow it", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(dependencyProvider!!.getContext(), "One permission is not allowed please allow it from Settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}