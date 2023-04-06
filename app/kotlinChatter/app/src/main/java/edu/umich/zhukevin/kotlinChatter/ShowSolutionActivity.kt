package edu.umich.zhukevin.kotlinChatter

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityShowSolutionBinding

class ShowSolutionActivity : AppCompatActivity() {

    private lateinit var view: ActivityShowSolutionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var solution = intent.getParcelableExtra("puzzle_solution_image", String::class.java)

        view = ActivityShowSolutionBinding.inflate(layoutInflater)
        setContentView(view.root)

        solution?.let {
            view.solution.setVisibility(View.VISIBLE)
            view.solution.load(it) {
                crossfade(true)
                crossfade(1000)
            }
        }

        view.backArrowButton.setOnClickListener{
            startActivity(Intent(this, PieceActivity::class.java))
        }

    }


}