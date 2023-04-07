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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getLastPuzzle
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.postPiece
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.postPuzzle
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimBinding

class Dimensions : AppCompatActivity() {

    private lateinit var view: DimBinding
    private val viewState: MainActivity.MainViewState by viewModels()

    private lateinit var height: EditText
    private lateinit var width: EditText

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DimBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        height = view.height
        width = view.width
        viewState.imageUri = intent.getParcelableExtra("PUZZLE_URI", Uri::class.java)

        var intHeight: String = height.text.toString()
        var intWidth: String = width.text.toString()

        view.nextButton.setOnClickListener{
            submitPuzzle("10", intHeight, intWidth)

            //after submitting puzzle entry, go submit puzzle piece
            //takePiecePhoto()
            val puzzleID = getLastPuzzle()
            Log.d("Dimensions onCreate","puzzleID = $puzzleID")
            startActivity(Intent(this, PieceActivity::class.java))
        }

        view.backButton.setOnClickListener{
            submitPuzzle("10", intHeight, intWidth)

            //after submitting puzzle entry, go submit puzzle piece
            //takePiecePhoto()
            val puzzleID = getLastPuzzle()
            Log.d("Dimensions onCreate","puzzleID = $puzzleID")
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    //for taking a photo of the puzzle piece
    private fun takePiecePhoto() {
        val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val piece_insert = Piece(puzzle_id = "",difficulty = "0",width = "2",height = "2")
                postPiece(applicationContext,piece_insert,viewState.imageUri) { msg ->
                    runOnUiThread {
                        toast(msg)
                    }
                    finish()
                }

                //get to dimensions

            } else {
                Log.d("TakePicture", "failed")
            }
        }
        viewState.imageUri = mediaStoreAlloc("image/jpeg")
        takeImageResult.launch(viewState.imageUri)
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
                            height_puzzle: String? = null,
                            width_puzzle: String? = null) {

        val puzzle = Puzzle(user_id = user_id_puzzle,
            height = height_puzzle, width = width_puzzle)

        postPuzzle(applicationContext, puzzle, viewState.imageUri) { msg ->
            runOnUiThread {
                toast(msg)
            }
            finish()
        }
    }
}