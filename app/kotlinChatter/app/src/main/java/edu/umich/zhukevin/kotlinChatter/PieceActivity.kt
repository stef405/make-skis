package edu.umich.zhukevin.kotlinChatter

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel

import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.pieces
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.LoadingBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PieceActivity : AppCompatActivity() {

    private lateinit var view: ActivityPuzzlePieceBinding
    private lateinit var pieceListAdapter: PieceListAdapter
    private val viewState: PostViewState by viewModels()
    //val puzzle_id = intent.getParcelableExtra("puzzle_id",String::class.java)

    class PostViewState: ViewModel() {
        var imageUri: Uri? = null
        var filledList: Boolean = false
    }

    private val propertyObserver = object: ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) { Log.d("on changed", "hello") }
        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { Log.d("on item range changed", "hello") }
        override fun onItemRangeInserted(
            sender: ObservableArrayList<Int>?,
            positionStart: Int,
            itemCount: Int
        ) {
            viewState.filledList = itemCount > 0
            println("onItemRangeInserted: $positionStart, $itemCount")
            runOnUiThread {
                if (viewState.filledList) {
                    view.emptyPieceImage.visibility = View.GONE
                    view.defaultHistoryText.visibility = View.GONE
                    pieceListAdapter.notifyDataSetChanged()
                }
            }
        }
        override fun onItemRangeMoved(sender: ObservableArrayList<Int>?, fromPosition: Int, toPosition: Int,
                                      itemCount: Int) { Log.d("on moved", "hello1")  }
        override fun onItemRangeRemoved(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) {
            println("onItemRangeRemoved: ${sender?.size}, $positionStart, $itemCount")
            if (sender?.size == 0) {
                runOnUiThread {
                    view.emptyPieceImage.visibility = View.VISIBLE
                    view.defaultHistoryText.visibility = View.VISIBLE
                    pieceListAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityPuzzlePieceBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        refreshTimeline()

        pieceListAdapter = PieceListAdapter(this, pieces)
        view.pieceListView.adapter = pieceListAdapter

        // setup refreshContainer here later
        view.refreshContainer.setOnRefreshListener {
            refreshTimeline()
        }
        pieces.addOnListChangedCallback(propertyObserver)
        getPieces(intent.getParcelableExtra("puzzle_id", String::class.java))

        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {
                difficultyPopup() // code below should be in this function

            } else {
                Log.d("TakePicture", "failed")
            }
        }

        val forPickedResult =
            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
                uri?.let {
                    val inStream = contentResolver.openInputStream(it) ?: return
                    viewState.imageUri = mediaStoreAlloc("image/jpeg")
                    viewState.imageUri?.let {
                        val outStream = contentResolver.openOutputStream(it) ?: return
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (inStream.read(buffer).also{ read = it } != -1) {
                            outStream.write(buffer, 0, read)
                        }
                        outStream.flush()
                        outStream.close()
                        inStream.close()
                    }

                    difficultyPopup() //code below should be in function
                    /*val piece_insert = Piece(puzzle_id = intent.getParcelableExtra("puzzle_id", String::class.java),difficulty = "0",width = "2",height = "2")
                    PuzzleStore.postPiece(applicationContext, piece_insert, viewState.imageUri) { msg ->
                        runOnUiThread {
                            toast(msg)
                        }
                        finish()
                    }*/


                } ?: run { Log.d("Pick media", "failed") }
            })

        view.cameraButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Upload from Storage")
                setMessage("Would you like to upload your puzzle entry from storage?")
                setPositiveButton("STORAGE") { _, _ -> forPickedResult.launch("*/*") }
                setNegativeButton("TAKE PHOTO") { _, _ ->
                    viewState.imageUri = mediaStoreAlloc("image/jpeg")
                    takePicture.launch(viewState.imageUri)
                }
                show()
            }
        }

        view.backArrowButton.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
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
            values)
    }

    private fun refreshTimeline() {
        if(viewState.filledList){
            view.emptyPieceImage.visibility = View.GONE
            view.defaultHistoryText.visibility = View.GONE
        }
        val piece_id = intent.getParcelableExtra("puzzle_id", String::class.java)
        getPieces(piece_id)
        // stop the refreshing animation upon completion:
        view.refreshContainer.isRefreshing = false
    }

    var pop_up : Int = 0
    private fun submitPiece(diff: String) {
        val piece_insert = Piece(puzzle_id = intent.getParcelableExtra("puzzle_id", String::class.java),difficulty = diff)

        runBlocking {
            launch {
                pop_up = PuzzleStore.postPiece(applicationContext, piece_insert, viewState.imageUri) { msg ->
                    runOnUiThread {
                        toast(msg)
                    }
                    //finish()
                }
            }
        }


    }

    private fun NoSolutionPopUp () {
        Log.d("submitPiece","response = $pop_up")

        if (pop_up == 202) {
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Image too Blurry")
                setMessage("Take another photo or select from storage")
                setPositiveButton("Ok", DialogInterface.OnClickListener(piecePopUpOk))
                show()
            }
        }
        else if (pop_up == 204) { //use response code for no solution found
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Solution Not Found")
                setMessage("Take another photo or select from storage")
                setPositiveButton("Ok", DialogInterface.OnClickListener(piecePopUpOk))
                show()
            }
        }
        else {
            viewSolution()
        }
    }

    val piecePopUpOk = { dialog: DialogInterface, which: Int ->
        refreshTimeline()
    }

    private fun difficultyPopup() {
        var popupBinding = LoadingBinding.inflate(layoutInflater)

        //create alert dialog
        var builder = AlertDialog.Builder(this)
        val puzzle_id = intent.getParcelableExtra("puzzle_id", String::class.java)
        with(builder) {
            setView(popupBinding.root)

            setTitle("Success!")
            setMessage("Select difficulty mode.\nProcessing will begin shortly after.")
            setPositiveButton("Easy ") { dialog, _ ->
                popupBinding.progressBar.visibility = View.VISIBLE
                // TODO: PERFORM OPEN CV HERE***
                submitPiece("0")
                // once the task is complete, hide progress bar
                popupBinding.progressBar.visibility = View.GONE
                NoSolutionPopUp()
                //var solution_img = PuzzleStore.getLastSolutionImg(puzzle_id)

            }
            setNegativeButton("Hard") { dialog, _ ->
                popupBinding.progressBar.visibility = View.VISIBLE
                // TODO: PERFORM OPEN CV HERE***
                submitPiece("1")
                // once the task is complete, hide progress bar
                popupBinding.progressBar.visibility = View.GONE
                NoSolutionPopUp()
            }
            show()
        }

        // create and show the dialog
//        val dialog = builder.create()
//        dialog.show()
    }

    private fun viewSolution() {
        var pieceID = ""
        var img = ""
        val puzzle_id = intent.getParcelableExtra("puzzle_id",String::class.java)
        Log.d("Puzzle ID",puzzle_id ?: "")
        runBlocking {
            launch {
                val str_arr = PuzzleStore.getLastPiece(puzzle_id)
                pieceID = str_arr[0]
                img = str_arr[1]
            }
        }

        val intent = Intent(this,ShowSolutionActivity::class.java)
        intent.putExtra("puzzle_id",puzzle_id)
        intent.putExtra("solution_img",img)
        intent.putExtra("askdelete","true")
        intent.putExtra("piece_id",pieceID)
        startActivity(intent)
    }

}
