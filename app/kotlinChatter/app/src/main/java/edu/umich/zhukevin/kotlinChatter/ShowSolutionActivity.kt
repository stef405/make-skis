package edu.umich.zhukevin.kotlinChatter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import coil.load
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityShowSolutionBinding
import kotlin.math.max
import kotlin.math.min


class ShowSolutionActivity : AppCompatActivity() {
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private lateinit var view: ActivityShowSolutionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var solution = intent.getParcelableExtra("solution_img", String::class.java)
        var puzzleID = intent.getParcelableExtra("puzzle_id", String::class.java)

        view = ActivityShowSolutionBinding.inflate(layoutInflater)
        setContentView(view.root)

        Log.d("solution", solution.toString())

        solution?.let {
            view.solution.setVisibility(View.VISIBLE)
            view.solution.load(it) {
                crossfade(true)
                crossfade(1000)
            }
        }

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        view.backArrowButton.setOnClickListener{
            val intent = Intent(this, PieceActivity::class.java)
            intent.putExtra("puzzle_id", puzzleID)
            this.startActivity(intent)
        }

    }
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(motionEvent)
        return true
    }
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            scaleFactor *= scaleGestureDetector.scaleFactor
            scaleFactor = max(0.1f, min(scaleFactor, 10.0f))
            view.solution.scaleX = scaleFactor
            view.solution.scaleY = scaleFactor
            return true
        }

    }


}