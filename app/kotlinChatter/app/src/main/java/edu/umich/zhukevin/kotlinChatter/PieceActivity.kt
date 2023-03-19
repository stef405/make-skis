package edu.umich.zhukevin.kotlinChatter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.umich.zhukevin.kotlinChatter.databinding.ActivityMainBinding

class PieceActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding
    private lateinit var chattListAdapter: ChattListAdapter
    private val viewState: MainViewState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        view.root.setBackgroundColor(Color.parseColor("#FFFFFF"))
        setContentView(R.layout.activity_puzzle_piece_history)


        //
        chattListAdapter = ChattListAdapter(this, ChattStore.chatts)
        view.chattListView.setAdapter(chattListAdapter)
        // setup refreshContainer here later
        view.refreshContainer.setOnRefreshListener {
            ChattStore.getChatts()
        }
        refreshTimeline()
        ChattStore.chatts.addOnListChangedCallback(propertyObserver)

        //should have permission already
        var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture())
        { success ->
            if (success) {
                // doCrop(cropIntent)
            } else {
                Log.d("TakePicture", "failed")
            }
        }



    }
}

