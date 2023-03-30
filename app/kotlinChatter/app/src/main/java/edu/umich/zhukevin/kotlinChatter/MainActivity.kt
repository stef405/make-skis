package edu.umich.zhukevin.kotlinChatter

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog.show
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPieces
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.pieces
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimensionsPopupLayoutBinding
import edu.umich.zhukevin.kotlinChatter.databinding.LoadingBinding


class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding
    private lateinit var pieceListAdapter: PieceListAdapter
    private val viewState: MainViewState by viewModels()

    //dimensions
    private lateinit var binding: DimensionsPopupLayoutBinding
    private var puzzleDim: PuzzleDim? = null


    private lateinit var other_view: ActivityPuzzlePieceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        pieceListAdapter = PieceListAdapter(this, pieces)
        view.puzzleListView.setAdapter(pieceListAdapter)

        // setup refreshContainer here later
        view.refreshContainer.setOnRefreshListener {
            getPieces()
        }

        refreshTimeline()
        pieces.addOnListChangedCallback(propertyObserver)

        val forPickedResult =
            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
                uri?.let {
                    if (it.toString().contains("video")) {

                    } else {
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
                    }
                    startActivity(Intent(this, Dimensions::class.java))
                } ?: run { Log.d("Pick media", "failed") }
            })

        // register camera contract
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach {
                if (!it.value) {
                    toast("${it.key} access denied")
                    finish()
                }
            }
        }.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES))

        // error message for no camera
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast("Device has no camera!")
            return
        }

        // camera chosen
        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {
                dimensionsPopup()
            } else {
                Log.d("Take picture", "failed")
                // retakeAlertMessage(takePicture)
            }
        }
        view.plusButton.setOnClickListener {
            viewState.imageUri = mediaStoreAlloc(mediaType="image/jpeg")
            plusButton(takePicture, forPickedResult)
        }
    }

    fun showHistory() {


    }

//    fun startPost(view: View?) = startActivity(Intent(this, PostActivity::class.java))

    // refresh content
    private fun refreshTimeline() {
        getPieces()

        // stop the refreshing animation upon completion:
        view.refreshContainer.isRefreshing = false
    }

    private val propertyObserver = object: ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) { }
        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
        override fun onItemRangeInserted(
            sender: ObservableArrayList<Int>?,
            positionStart: Int,
            itemCount: Int
        ) {
            println("onItemRangeInserted: $positionStart, $itemCount")
            runOnUiThread {
                pieceListAdapter.notifyDataSetChanged()
            }
        }
        override fun onItemRangeMoved(sender: ObservableArrayList<Int>?, fromPosition: Int, toPosition: Int,
                                      itemCount: Int) { }
        override fun onItemRangeRemoved(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
    }

    override fun onDestroy() {
        super.onDestroy()

//        chatts.removeOnListChangedCallback(propertyObserver)
    }

    // store camera image
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

    // plus button
    private fun plusButton(takePicture: ActivityResultLauncher<Uri>, forPickedResult: ActivityResultLauncher<String>) {
        // setup the alert builder
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Upload from Storage")
            setMessage("Would you like to upload your puzzle entry from storage?")
            setPositiveButton("STORAGE") { _, _ -> forPickedResult.launch("*/*") }
            setNegativeButton("TAKE PHOTO") { _, _ -> takePicture.launch(viewState.imageUri) }
            show()
        }
    }

    //dimensions
    private fun dimensionsPopup() {
        var popupBinding = DimensionsPopupLayoutBinding.inflate(layoutInflater)

        //create alert dialog
        var builder = AlertDialog.Builder(this)
        with(builder) {
            setView(popupBinding.root)
            setTitle("Enter puzzle dimensions and piece count")
            setMessage("Provide puzzle image dimensions in order to help you")
            setPositiveButton("Ok") { dialog, _ ->
                // store input values in PuzzleDim object
                puzzleDim = PuzzleDim(
                    height = popupBinding.puzzleHeight.text.toString().toInt(),
                    width = popupBinding.puzzleWidth.text.toString().toInt(),
                    num_count = popupBinding.pieceCount.text.toString().toInt()
                )
                // do something with the input values??
                // TODO: need to connect this to taking puzzle piece picture
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        }

        // create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }
    // TODO: NEED TO ADD THIS TO MAIN ACTIVITY OnCreate()
    //difficulty
    private fun difficultyPopup() {
        var popupBinding = LoadingBinding.inflate(layoutInflater)

        //create alert dialog
        var builder = AlertDialog.Builder(this)
        with(builder) {
            setView(popupBinding.root)
            setTitle("Success!")
            setMessage("Select difficulty mode.\nProcessing will begin shortly after.")
            setPositiveButton("Easy ") { dialog, _ ->
                popupBinding.progressBar.visibility = View.VISIBLE
                // TODO: PERFORM OPEN CV HERE***
                // once the task is complete, hide progress bar
                popupBinding.progressBar.visibility = View.GONE
            }
            setNegativeButton("Hard") { dialog, _ ->
                popupBinding.progressBar.visibility = View.VISIBLE
                // TODO: PERFORM OPEN CV HERE***
                // once the task is complete, hide progress bar
                popupBinding.progressBar.visibility = View.GONE
            }
            show()
        }

        // create and show the dialog
        val dialog = builder.create()
        dialog.show()
    }
    // TODO: NEED TO ADD THIS TO MAIN ACTIVITY OnCreate()
    // currently, this is linked to take a picture, but unsure how to take a picture again
    //retake
    private fun retakeAlertMessage(takePicture: ActivityResultLauncher<Uri>){
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(getString(R.string.fail_title))
            setMessage(getString(R.string.error_message))
            setPositiveButton("RETAKE") { _, _ -> takePicture.launch(viewState.imageUri) }
            setNegativeButton("EXIT") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    /*
    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.yes, Toast.LENGTH_SHORT).show()
    }

    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.no, Toast.LENGTH_SHORT).show()
    }
     */
}

class MainViewState: ViewModel() {
    var enableSend = true
    var imageUri: Uri? = null
}
