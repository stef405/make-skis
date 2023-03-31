package edu.umich.zhukevin.kotlinChatter

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import kotlinx.coroutines.delay
import edu.umich.zhukevin.kotlinChatter.databinding.DifficultyBinding
import edu.umich.zhukevin.kotlinChatter.databinding.LoadingBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.puzzles

class Loading : AppCompatActivity() {

    private lateinit var view: LoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = LoadingBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        view.loading.visibility = View.VISIBLE
        Handler().postDelayed({
            startActivity(Intent(this, PieceActivity::class.java))
        }, 5000)

//        view.progressBar.visibility = View.VISIBLE



    }
}