package edu.umich.zhukevin.kotlinChatter

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding
import edu.umich.zhukevin.kotlinChatter.databinding.DimBinding

class PuzzleDim(
    height: Int? = null,
    width: Int? = null,
    num_count: Int? = null
                )

class Dimensions : AppCompatActivity() {

    private lateinit var view: DimBinding
    private lateinit var pieceListAdapter: PieceListAdapter
    private val viewState: MainViewState by viewModels()

    private lateinit var height: EditText
    private lateinit var width: EditText
    private lateinit var num_count: EditText
    private lateinit var other_view: ActivityPuzzlePieceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = DimBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(view.root)

        height = findViewById(R.id.height)
        width = findViewById(R.id.width)
        num_count = findViewById(R.id.num_count)

        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {
                startActivity(Intent(this, Difficulty::class.java))
            } else {
                Log.d("TakePicture", "failed")
            }
        }

        view.nextButton.setOnClickListener{
            viewState.imageUri = mediaStoreAlloc(mediaType="image/jpeg")
            takePicture.launch(viewState.imageUri)
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


}