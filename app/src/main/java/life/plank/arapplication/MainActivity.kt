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
                ArPointData(15.5511178, 73.7823974, "Pokemon"),
                ArPointData(15.5511825, 73.7822005,  "Pokemon 2"),
                ArPointData(15.5511855, 73.7821555,  "Pokemon 3"),
                ArPointData(15.5511385, 73.7821325, "Pokemon 4"),
                ArPointData(15.5510785, 73.7821395,  "Pokemon 5"),
                ArPointData(15.5510505, 73.7821715,  "Pokemon 6"),
                ArPointData(15.5510545, 73.7822285, "Pokemon 7"),
                ArPointData(15.5510995, 73.7822585,  "Pokemon 8")
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

    override fun onDestroy() {
        super.onDestroy()
        if (arView.isArViewStarted){
            arView.onDestroy()
        }
    }

}
