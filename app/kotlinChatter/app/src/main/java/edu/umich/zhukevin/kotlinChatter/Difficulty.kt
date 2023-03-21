package edu.umich.zhukevin.kotlinChatter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.umich.zhukevin.kotlinChatter.databinding.DifficultyBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimBinding

class Difficulty : AppCompatActivity() {


    private lateinit var view: DifficultyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DifficultyBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        view.easy.setOnClickListener{
            startActivity(Intent(this, Loading::class.java))
        }

        view.hard.setOnClickListener{
            startActivity(Intent(this, Loading::class.java))
        }

    }
}