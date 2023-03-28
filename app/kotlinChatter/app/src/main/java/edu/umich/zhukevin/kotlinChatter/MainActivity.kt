package edu.umich.zhukevin.kotlinChatter

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.getPuzzles
import edu.umich.zhukevin.kotlinChatter.PuzzleStore.puzzles
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding
    private lateinit var puzzleListAdapter: PuzzleListAdapter
    private val viewState: MainViewState by viewModels()
    class MainViewState: ViewModel() {
        var imageUri: Uri? = null
        var filledList: Boolean = false
    }

    private val propertyObserver = object: ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) {
        }
        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
        override fun onItemRangeInserted(
            sender: ObservableArrayList<Int>?,
            positionStart: Int,
            itemCount: Int
        ) {
            viewState.filledList = itemCount > 0
            println("onItemRangeInserted: $positionStart, $itemCount")
            runOnUiThread {
                if(viewState.filledList){
                    view.emptyPuzzlePiece.visibility = View.GONE
                    view.defaultHistoryText.visibility = View.GONE
                }
                puzzleListAdapter.notifyDataSetChanged()
            }
        }
        override fun onItemRangeMoved(sender: ObservableArrayList<Int>?, fromPosition: Int, toPosition: Int,
                                      itemCount: Int) { }
        override fun onItemRangeRemoved(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        puzzleListAdapter = PuzzleListAdapter(this, puzzles)
        view.puzzleListView.adapter = puzzleListAdapter

        // setup refreshContainer here later
        view.refreshContainer.setOnRefreshListener {
            refreshTimeline()
        }
        puzzles.addOnListChangedCallback(propertyObserver)
        getPuzzles()

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
        val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val intent = Intent(this, Dimensions::class.java)
                intent.putExtra("PUZZLE_URI", viewState.imageUri)
                this.startActivity(intent)
            } else {
                Log.d("TakePicture", "failed")
            }
        }

        view.plusButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder)
            {
                setTitle("Upload from Storage")
                setMessage("Would you like to upload your puzzle entry from storage?")
                setPositiveButton("STORAGE") { _, _ -> forPickedResult.launch("*/*") }
                setNegativeButton("TAKE PHOTO") { _, _ ->
                    viewState.imageUri = mediaStoreAlloc("image/jpeg")
                    takeImageResult.launch(viewState.imageUri)
                }
                show()
            }
        }
    }

    // refresh content
    private fun refreshTimeline() {
        if(viewState.filledList){
            view.emptyPuzzlePiece.visibility = View.GONE
            view.defaultHistoryText.visibility = View.GONE
        }
        getPuzzles()
        // stop the refreshing animation upon completion:
        view.refreshContainer.isRefreshing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        //Possibly change but for now keep like this
        puzzles.removeOnListChangedCallback(propertyObserver)
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
    fun retakeAlertMessage(view: View){
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(getString(R.string.fail_title))
            setMessage(getString(R.string.error_message))
            setPositiveButton(getString(R.string.retake_photo), DialogInterface.OnClickListener(positiveButtonClick))
            setNegativeButton(getString(R.string.exit_alert_dialog), negativeButtonClick)
            show()
        }
    }

    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.yes, Toast.LENGTH_SHORT).show()
    }

    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(applicationContext,
            android.R.string.no, Toast.LENGTH_SHORT).show()
    }
}
