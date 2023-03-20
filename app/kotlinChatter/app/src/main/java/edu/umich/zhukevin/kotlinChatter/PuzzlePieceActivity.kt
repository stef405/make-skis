package edu.umich.zhukevin.kotlinChatter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityPuzzlePieceBinding

class PuzzlePieceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPuzzlePieceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPuzzlePieceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {
                // doCrop(cropIntent)
            } else {
                Log.d("TakePicture", "failed")
            }
        }
    }

    class PostViewState: ViewModel() {
        var enableSend = true
        var imageUri: Uri? = null
    }
}