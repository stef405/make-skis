package edu.umich.zhukevin.kotlinChatter

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.postPuzzle
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimBinding

class Dimensions : AppCompatActivity() {

    private lateinit var view: DimBinding
    private val viewState: MainActivity.MainViewState by viewModels()

    private lateinit var height: EditText
    private lateinit var width: EditText
    private lateinit var num_count: EditText

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DimBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        height = view.height
        width = view.width
        num_count = view.numCount
        viewState.imageUri = intent.getParcelableExtra("PUZZLE_URI", Uri::class.java)

//        var image = viewState.imageUri

//        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
//        { success ->
//            if (success) {
//                startActivity(Intent(this, Difficulty::class.java))
//            } else {
//                Log.d("TakePicture", "failed")
//            }
//        }

        view.nextButton.setOnClickListener{
            var int_height: String = height.text.toString()
            var int_width: String = width.text.toString()
            var int_count: String = num_count.text.toString()
//            var string_image: String = viewState.imageUri.toString()
            submitPuzzle("10", int_count, int_height, int_width)
//            takePicture.launch(viewState.imageUri)
            startActivity(Intent(this, Difficulty::class.java))
        }

    }

    private fun mediaStoreAlloc(mediaType: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        return contentResolver.insert(
            if (mediaType.contains("video"))
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun submitPuzzle(user_id_puzzle: String? = null,
                            piece_count_puzzle: String? = null,
                            height_puzzle: String? = null,
                            width_puzzle: String? = null) {

        val puzzle = Puzzle(user_id = user_id_puzzle, piece_ct = piece_count_puzzle,
            height = height_puzzle, width = width_puzzle)

        postPuzzle(applicationContext, puzzle, viewState.imageUri) { msg ->
            runOnUiThread {
                toast(msg)
            }
            finish()
        }
    }


}