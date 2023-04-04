package edu.umich.zhukevin.kotlinChatter

import android.content.ContentValues
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
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.pieces
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding

class PieceActivity : AppCompatActivity() {

    private lateinit var view: ActivityPuzzlePieceBinding
    private lateinit var pieceListAdapter: PieceListAdapter
    private val viewState: PostViewState by viewModels()

    class PostViewState: ViewModel() {
        var imageUri: Uri? = null
        var filledList: Boolean = false
    }

    private val propertyObserver = object: ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) {}
        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
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
                                      itemCount: Int) { }
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
//        getPieces(intent.getParcelableExtra("puzzle_id", String::class.java))

        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {

                val piece_insert = Piece(puzzle_id = intent.getParcelableExtra("puzzle_id", String::class.java),difficulty = "0",width = "2",height = "2")
                PuzzleStore.postPiece(applicationContext, piece_insert, viewState.imageUri) { msg ->
                    runOnUiThread {
                        toast(msg)
                    }
                    finish()
                }
            } else {
                Log.d("TakePicture", "failed")
            }
        }

        view.cameraButton.setOnClickListener {
            viewState.imageUri = mediaStoreAlloc(mediaType="image/jpeg")
            takePicture.launch(viewState.imageUri)
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
        getPieces(intent.getParcelableExtra("puzzle_id", String::class.java))
        // stop the refreshing animation upon completion:
        view.refreshContainer.isRefreshing = false
    }

}
