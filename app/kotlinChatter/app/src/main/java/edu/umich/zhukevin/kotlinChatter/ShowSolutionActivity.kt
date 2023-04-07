package edu.umich.zhukevin.kotlinChatter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityShowSolutionBinding

class ShowSolutionActivity : AppCompatActivity() {

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

        view.backArrowButton.setOnClickListener{
            val intent = Intent(this, PieceActivity::class.java)
            intent.putExtra("puzzle_id", puzzleID)
            this.startActivity(intent)
        }

    }


}