package life.plank.arapplication

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*
import life.plank.visior.data.view.ArPointData
import life.plank.visior.di.DependencyProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arView.onCreate(DependencyProviderImpl(applicationContext, this, this))
        setArPoints()
    }

    private fun setArPoints(){
        arView.setArPoints(
            listOf(
                ArPointData(15.551178, 73.782250, "Pokemon 1"),
                ArPointData(15.551182, 73.782200,  "Pokemon 2"),
                ArPointData(15.551185, 73.782155,  "Pokemon 3"),
                ArPointData(15.551138, 73.782132, "Pokemon 4"),
                ArPointData(15.551078, 73.782139,  "Pokemon 5"),
                ArPointData(15.551050, 73.782171,  "Pokemon 6"),
                ArPointData(15.551054, 73.782228, "Pokemon 7"),
                ArPointData(15.551099, 73.782258,  "Pokemon 8")
            )
        )
    }

    class DependencyProviderImpl(
        private val context: Context,
        private val lifecycleOwner: LifecycleOwner,
        private val activity: Activity
    ) : DependencyProvider {
        override fun getContext(): Context = context
        override fun getLifecycleOwner(): LifecycleOwner = lifecycleOwner
        override fun getPermissionActivity(): Activity = activity
    }

    override fun onPause() {
        super.onPause()
        if (arView.isArViewStarted){
            arView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (arView.isArViewStarted){
            arView.onResume()
        }
    }

}
