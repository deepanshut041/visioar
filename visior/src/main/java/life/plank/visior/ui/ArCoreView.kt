package life.plank.visior.ui

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.ar_core_view.view.*
import life.plank.visior.R
import life.plank.visior.di.DependencyProvider

class ArCoreView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var arFragment: ArFragment
    init {
        View.inflate(context, R.layout.ar_core_view, this)
    }

    fun onCreate(dependencyProvider: DependencyProvider){
        this.dependencyProvider = dependencyProvider
    }

    fun onStart(){
        dependencyProvider?.let {
            arFragment =
                (it.getPermissionActivity() as AppCompatActivity).supportFragmentManager.findFragmentById(
                    R.id.sceneformFragment
                ) as ArFragment
            fabIcon.setOnClickListener {
                addObject(Uri.parse("pikachu.sfb"))
            }
        }
    }

    private fun addObject(parse: Uri) {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        if (frame != null) {
            val hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeObject(arFragment, hit.createAnchor(), parse)
                    break
                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, createAnchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                addNodeToScene(fragment, createAnchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(dependencyProvider!!.getContext())
                builder.setMessage(it.message)
                    .setTitle("error!")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, createAnchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(createAnchor)
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }

    private fun getScreenCenter(): Point {
        val vw = findViewById<View>(R.id.arLayout)
        return Point(vw.width / 2, vw.height / 2)
    }
}