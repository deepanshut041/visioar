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
                ArPointData(15.5511347, 73.7822677, "Point 1"),
                ArPointData(15.5511307, 73.7822607,  "Point 2"),
                ArPointData(15.5511387, 73.7822657,  "Point 3")
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
