package edu.umich.zhukevin.kotlinChatter

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
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
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getLastPuzzle
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.postPiece
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.postPuzzle
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Dimensions : AppCompatActivity() {

    private lateinit var view: DimBinding
    private val viewState: MainActivity.MainViewState by viewModels()
    private var pop_up: Boolean? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DimBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        viewState.imageUri = intent.getParcelableExtra("PUZZLE_URI", Uri::class.java)

        view.nextButton.setOnClickListener{
            submitPuzzle("10")
            if(pop_up==true){ //if we get 202
                val builder = AlertDialog.Builder(this)
                with(builder)
                {
                    setTitle("Image too Blurry")
                    setMessage("Go back to main to take another photo or select from storage")
                    setPositiveButton(
                        "Ok",
                        DialogInterface.OnClickListener(positiveButtonClickWelcome)
                    )
                    show()
                }
            }
            else if(pop_up==false){ //if we get 200
                startActivity(Intent(applicationContext, PieceActivity::class.java))
            }
        }

        view.backButton.setOnClickListener{
            submitPuzzle("10")
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

    //for taking a photo of the puzzle piece
    private fun takePiecePhoto() {
        val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val piece_insert = Piece(puzzle_id = getLastPuzzle(),difficulty = "0")
                /*postPiece(applicationContext,piece_insert,viewState.imageUri) { msg ->
                    runOnUiThread {
                        toast(msg)
                    }
                    finish()
                }*/
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

    private fun submitPuzzle(user_id_puzzle: String? = null) {

        val puzzle = Puzzle(user_id = user_id_puzzle)

        runBlocking {
            launch {
                pop_up = postPuzzle(applicationContext, puzzle, viewState.imageUri) { msg ->
                    runOnUiThread {
                        toast(msg)
                    }
                }
            }
        }
    }

    val positiveButtonClickWelcome = { dialog: DialogInterface, which: Int ->
        val intent1 = Intent(this,MainActivity::class.java)
        startActivity(intent1)
    }
}