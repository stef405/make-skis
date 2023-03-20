package edu.umich.zhukevin.kotlinChatter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.delay
import edu.umich.zhukevin.kotlinChatter.databinding.DifficultyBinding
import edu.umich.zhukevin.kotlinChatter.databinding.LoadingBinding
import java.util.concurrent.Executors


class Loading : AppCompatActivity() {

    private lateinit var view: LoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = LoadingBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        view.loading.visibility = View.VISIBLE


        Thread.sleep(15000)

        startActivity(Intent(this, PuzzlePieceActivity::class.java))



    }
}